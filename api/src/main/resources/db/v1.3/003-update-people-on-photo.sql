-- @formatter:off
ALTER TYPE photo_event_type ADD VALUE 'update_people';

CREATE TABLE photo_person
(
    photo_id  uuid NOT NULL REFERENCES photo ON DELETE CASCADE,
    person_id uuid NOT NULL REFERENCES person ON DELETE CASCADE,
    PRIMARY KEY (photo_id, person_id)
);

CREATE PROCEDURE update_photo_people(event record)
    LANGUAGE plpgsql AS
$$
DECLARE
    _person_id varchar;
BEGIN
    FOR _person_id IN SELECT json_array_elements_text((event.data #>> '{content,peopleToAdd}')::json) LOOP
        INSERT INTO photo_person
        VALUES
        (
            event.subject_id,
            _person_id::uuid
        );
    END LOOP;
    FOR _person_id IN SELECT json_array_elements_text((event.data #>> '{content,peopleToRemove}')::json) LOOP
        DELETE FROM photo_person
        WHERE photo_id = event.subject_id AND person_id = _person_id::uuid;
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
        WHEN new.type = 'update' THEN CALL update_photo(new);
        WHEN new.type = 'update_permissions' THEN CALL update_photo_permissions(new);
        WHEN new.type = 'update_people' THEN CALL update_photo_people(new);
        ELSE
        END CASE;
    RETURN new;
END;
$$;
