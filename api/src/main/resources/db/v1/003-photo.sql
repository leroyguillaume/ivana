-- @formatter:off
CREATE TYPE photo_type AS enum ('jpg', 'png');

CREATE TYPE photo_event_type AS enum ('upload', 'visualization', 'deletion');

CREATE TABLE photo_event
(
    date       timestamp WITH TIME ZONE NOT NULL DEFAULT now(),
    subject_id uuid                     NOT NULL,
    number     bigint                   NOT NULL,
    type       photo_event_type         NOT NULL,
    data       jsonb                    NOT NULL,
    PRIMARY KEY (subject_id, number)
);

CREATE TABLE photo
(
    id          uuid                     NOT NULL PRIMARY KEY,
    owner_id    uuid                     NOT NULL REFERENCES "user" ON DELETE CASCADE,
    upload_date timestamp WITH TIME ZONE NOT NULL,
    type        photo_type               NOT NULL,
    hash        varchar(40)              NOT NULL,
    no          serial                   NOT NULL
);

CREATE FUNCTION next_photo_event_number(id uuid) RETURNS bigint
    LANGUAGE SQL AS
$$
SELECT COALESCE(MAX(number), 0) + 1
FROM photo_event
WHERE subject_id = $1;
$$;

CREATE PROCEDURE insert_photo_from_upload_event(event record)
    LANGUAGE plpgsql AS
$$
BEGIN
    INSERT INTO photo
    VALUES
    (
        event.subject_id,
        (event.data #>> '{source,id}')::uuid,
        event.date,
        (event.data #>> '{content,type}')::photo_type,
        event.data #>> '{content,hash}'
    );
END;
$$;

CREATE FUNCTION photo_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE WHEN new.type = 'upload' THEN CALL insert_photo_from_upload_event(new);
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
