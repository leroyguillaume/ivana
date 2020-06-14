-- @formatter:off
CREATE TYPE person_event_type AS enum ('creation', 'update', 'deletion');

CREATE SEQUENCE person_event_number_seq;

CREATE TABLE person_event
(
    date       timestamp WITH TIME ZONE NOT NULL DEFAULT now(),
    subject_id uuid                     NOT NULL,
    number     bigint                   NOT NULL DEFAULT nextval('person_event_number_seq'),
    type       person_event_type        NOT NULL,
    data       jsonb                    NOT NULL,
    PRIMARY KEY (subject_id, number)
);

CREATE TABLE person
(
    id         uuid        NOT NULL PRIMARY KEY,
    last_name  varchar(50) NOT NULL,
    first_name varchar(50) NOT NULL,
    UNIQUE (first_name, last_name)
);

CREATE PROCEDURE insert_person(event record)
    LANGUAGE plpgsql AS
$$
BEGIN
    INSERT INTO person
    VALUES
    (
        event.subject_id,
        event.data #>> '{content,lastName}',
        event.data #>> '{content,firstName}'
    );
END;
$$;

CREATE PROCEDURE update_person(event record)
    LANGUAGE plpgsql AS
$$
BEGIN
    UPDATE person
    SET last_name = event.data #>> '{content,lastName}',
        first_name = event.data #>> '{content,firstName}'
    WHERE id = event.subject_id;
END;
$$;

CREATE FUNCTION person_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE
        WHEN new.type = 'creation' THEN CALL insert_person(new);
        WHEN new.type = 'update' THEN CALL update_person(new);
        WHEN new.type = 'deletion' THEN DELETE FROM person WHERE id = new.subject_id;
        ELSE
        END CASE;
    RETURN new;
END;
$$;

CREATE TRIGGER person_update
    BEFORE INSERT
    ON person_event
    FOR EACH ROW
EXECUTE PROCEDURE person_update();
