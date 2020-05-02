-- @formatter:off
ALTER TYPE photo_event_type ADD VALUE 'update_permissions';

ALTER TABLE user_photo_authorization ADD COLUMN can_update_permissions bool NOT NULL DEFAULT false;
UPDATE user_photo_authorization
SET can_update_permissions = true
FROM (SELECT id, owner_id FROM photo) AS p
WHERE photo_id = p.id AND user_id = p.owner_id;

CREATE OR REPLACE PROCEDURE insert_photo(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    _user_id uuid;
BEGIN
    _user_id = (event.data #>> '{source,id}')::uuid;
    INSERT INTO photo
    VALUES
    (
        event.subject_id,
        _user_id,
        event.date,
        (event.data #>> '{content,type}')::photo_type,
        event.data #>> '{content,hash}'
    );
    INSERT INTO user_photo_authorization
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

CREATE PROCEDURE update_photo_permissions(event record)
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
        INSERT INTO user_photo_authorization
        VALUES
        (
            _user_id,
            event.subject_id,
            _user_permissions ? 'read',
            _user_permissions ? 'update',
            _user_permissions ? 'delete',
            _user_permissions ? 'update_permissions'
        )
        ON CONFLICT (user_id, photo_id) DO UPDATE
        SET can_read = _user_permissions ? 'read',
            can_update = _user_permissions ? 'update',
            can_delete = _user_permissions ? 'delete',
            can_update_permissions = _user_permissions ? 'update_permissions';
    END LOOP;
    FOR _permission IN SELECT json_array_elements((event.data #>> '{content,permissionsToRemove}')::json) LOOP
        _user_id = (_permission ->> 'subjectId')::uuid;
        _user_permissions = (_permission ->> 'permissions')::jsonb;
        INSERT INTO user_photo_authorization
        VALUES
        (
            _user_id,
            event.subject_id,
            NOT (_user_permissions ? 'read'),
            NOT (_user_permissions ? 'update'),
            NOT (_user_permissions ? 'delete'),
            NOT (_user_permissions ? 'update_permissions')
        )
        ON CONFLICT (user_id, photo_id) DO UPDATE
            SET can_read = NOT(_user_permissions ? 'read'),
                can_update = NOT(_user_permissions ? 'update'),
                can_delete = NOT(_user_permissions ? 'delete'),
                can_update_permissions = NOT(_user_permissions ? 'update_permissions');
    END LOOP;
END;
$$;

CREATE OR REPLACE FUNCTION photo_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE
        WHEN new.type = 'upload' THEN CALL insert_photo(new);
        WHEN new.type = 'deletion' THEN DELETE FROM photo WHERE id = new.subject_id;
        WHEN new.type = 'transform' THEN CALL increment_photo_version(new);
        WHEN new.type = 'update_permissions' THEN CALL update_photo_permissions(new);
        ELSE
        END CASE;
    RETURN new;
END;
$$;
