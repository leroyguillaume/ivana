-- @formatter:off
ALTER TYPE album_event_type ADD VALUE 'update_permissions';

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

CREATE PROCEDURE update_album_permissions(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    _user_id          uuid;
    _permission       jsonb;
    _user_permissions jsonb;
BEGIN
    FOR _permission IN SELECT json_array_elements((event.data #>> '{content,permissionsToAdd}')::json) LOOP
            _user_id = (_permission ->> 'subjectId')::uuid;
            _user_permissions = (_permission ->> 'permissions')::jsonb;
            INSERT INTO user_album_authorization
            VALUES
            (
                _user_id,
                event.subject_id,
                _user_permissions ? 'read',
                _user_permissions ? 'update',
                _user_permissions ? 'delete',
                _user_permissions ? 'update_permissions'
            )
            ON CONFLICT (user_id, album_id) DO UPDATE
                SET can_read = _user_permissions ? 'read',
                    can_update = _user_permissions ? 'update',
                    can_delete = _user_permissions ? 'delete',
                    can_update_permissions = _user_permissions ? 'update_permissions';
        END LOOP;
    FOR _permission IN SELECT json_array_elements((event.data #>> '{content,permissionsToRemove}')::json) LOOP
            _user_id = (_permission ->> 'subjectId')::uuid;
            _user_permissions = (_permission ->> 'permissions')::jsonb;
            INSERT INTO user_album_authorization
            VALUES
            (
                _user_id,
                event.subject_id,
                false,
                false,
                false,
                false
            )
            ON CONFLICT (user_id, album_id) DO UPDATE
                SET can_read = CASE WHEN _user_permissions ? 'read' THEN false ELSE user_album_authorization.can_read END,
                    can_update = CASE WHEN _user_permissions ? 'update' THEN false ELSE user_album_authorization.can_update END,
                    can_delete = CASE WHEN _user_permissions ? 'delete' THEN false ELSE user_album_authorization.can_delete END,
                    can_update_permissions = CASE WHEN _user_permissions ? 'update_permissions' THEN false ELSE user_album_authorization.can_update_permissions END;
        END LOOP;
END;
$$;

CREATE OR REPLACE FUNCTION album_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE
        WHEN new.type = 'creation' THEN CALL insert_album(new);
        WHEN new.type = 'deletion' THEN DELETE FROM album WHERE id = new.subject_id;
        WHEN new.type = 'update' THEN CALL update_album(new);
        WHEN new.type = 'update_permissions' THEN CALL update_album_permissions(new);
        ELSE
        END CASE;
    RETURN new;
END;
$$;
