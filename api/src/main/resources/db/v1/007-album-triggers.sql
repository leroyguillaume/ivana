-- @formatter:off
CREATE PROCEDURE insert_album(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    user_id uuid;
BEGIN
    user_id = (event.data #>> '{source,id}')::uuid;
    INSERT INTO album
    VALUES
    (
        event.subject_id,
        user_id,
        event.data #>> '{content,name}',
        event.date
    );
    INSERT INTO user_album_authorization
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

CREATE FUNCTION album_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE
        WHEN new.type = 'creation' THEN CALL insert_album(new);
        WHEN new.type = 'deletion' THEN DELETE FROM album WHERE id = new.subject_id;
        ELSE
        END CASE;
    RETURN new;
END;
$$;

CREATE TRIGGER album_update
    BEFORE INSERT
    ON album_event
    FOR EACH ROW
EXECUTE PROCEDURE album_update();
