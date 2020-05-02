-- @formatter:off
ALTER TABLE user_album_authorization ADD COLUMN can_update_permissions bool NOT NULL DEFAULT false;
UPDATE user_album_authorization
SET can_update_permissions = true
FROM (SELECT id, owner_id FROM album) AS a
WHERE album_id = a.id AND user_id = a.owner_id;

CREATE OR REPLACE PROCEDURE insert_album(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    _user_id uuid;
BEGIN
    _user_id = (event.data #>> '{source,id}')::uuid;
    INSERT INTO album
    VALUES
    (
        event.subject_id,
        _user_id,
        event.data #>> '{content,name}',
        event.date
    );
    INSERT INTO user_album_authorization
    VALUES
    (
        _user_id,
        event.subject_id,
        true,
        true,
        true,
        true
    );
END;
$$;
