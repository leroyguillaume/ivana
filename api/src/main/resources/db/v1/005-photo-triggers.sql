-- @formatter:off
CREATE PROCEDURE insert_photo(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    user_id uuid;
BEGIN
    user_id = (event.data #>> '{source,id}')::uuid;
    INSERT INTO photo
    VALUES
    (
        event.subject_id,
        user_id,
        event.date,
        (event.data #>> '{content,type}')::photo_type,
        event.data #>> '{content,hash}'
    );
    INSERT INTO user_photo_authorization
    VALUES
    (
        user_id,
        event.subject_id,
        true,
        true,
        true
    );
END;
$$;

CREATE FUNCTION photo_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE WHEN new.type = 'upload' THEN CALL insert_photo(new);
        WHEN new.type = 'deletion' THEN DELETE FROM photo WHERE id = new.subject_id;
        ELSE
        END CASE;
    RETURN new;
END;
$$;

CREATE TRIGGER photo_update
    BEFORE INSERT
    ON photo_event
    FOR EACH ROW
EXECUTE PROCEDURE photo_update();
