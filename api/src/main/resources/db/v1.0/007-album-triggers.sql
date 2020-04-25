-- @formatter:off
CREATE PROCEDURE insert_album(event record)
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
        true
    );
END;
$$;

CREATE PROCEDURE update_album(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    _photo_id uuid;
BEGIN
    UPDATE album
    SET name = event.data #>> '{content,name}'
    WHERE id = event.subject_id;
    FOR _photo_id IN SELECT json_array_elements_text((event.data #>> '{content,photosToAdd}')::json) LOOP
        INSERT INTO album_photo VALUES (event.subject_id, _photo_id, next_album_photo_order(event.subject_id));
    END LOOP;
    FOR _photo_id IN SELECT json_array_elements_text((event.data #>> '{content,photosToRemove}')::json) LOOP
        DELETE FROM album_photo WHERE album_id = event.subject_id AND photo_id = _photo_id;
    END LOOP;
END;
$$;

CREATE FUNCTION album_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE
        WHEN new.type = 'creation' THEN CALL insert_album(new);
        WHEN new.type = 'deletion' THEN DELETE FROM album WHERE id = new.subject_id;
        WHEN new.type = 'update' THEN CALL update_album(new);
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
