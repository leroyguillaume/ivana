-- @formatter:off
ALTER TYPE photo_event_type ADD VALUE 'update';

ALTER TABLE photo ADD COLUMN shooting_date date;

CREATE PROCEDURE update_photo(event record)
    LANGUAGE plpgsql AS
$$
BEGIN
    UPDATE photo
    SET shooting_date = (event.data #>> '{content,shootingDate}')::date
    WHERE id = event.subject_id;
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
        WHEN new.type = 'update' THEN CALL update_photo(new);
        WHEN new.type = 'update_permissions' THEN CALL update_photo_permissions(new);
        ELSE
        END CASE;
    RETURN new;
END;
$$;
