-- @formatter:off
CREATE TYPE user_event_type AS enum ('creation', 'login', 'password_update');

CREATE TYPE user_role AS enum ('user', 'admin', 'super_admin');

CREATE TABLE user_event
(
    date       timestamp WITH TIME ZONE NOT NULL DEFAULT now(),
    subject_id uuid                     NOT NULL,
    number     bigint                   NOT NULL,
    type       user_event_type          NOT NULL,
    data       jsonb                    NOT NULL,
    PRIMARY KEY (subject_id, number)
);

CREATE TABLE "user"
(
    id            uuid                     NOT NULL PRIMARY KEY,
    name          varchar(50)              NOT NULL UNIQUE,
    password      varchar(100)             NOT NULL,
    role          user_role                NOT NULL,
    creation_date timestamp WITH TIME ZONE NOT NULL
);

CREATE FUNCTION next_user_event_number(id uuid) RETURNS bigint
    LANGUAGE SQL AS
$$
SELECT COALESCE(MAX(number), 0) + 1
FROM user_event
WHERE subject_id = $1;
$$;

CREATE PROCEDURE insert_user(event record)
    LANGUAGE plpgsql AS
$$
BEGIN
    INSERT INTO "user"
    VALUES
    (
        event.subject_id,
        event.data #>> '{content,name}',
        event.data #>> '{content,hashedPwd}',
        (event.data #>> '{content,role}')::user_role,
        event.date
    );
END;
$$;

CREATE PROCEDURE update_user_password(event record)
    LANGUAGE plpgsql AS
$$
BEGIN
    UPDATE "user"
    SET password = event.data #>> '{content,newHashedPwd}'
    WHERE id = event.subject_id;
END;
$$;

CREATE FUNCTION user_update() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    CASE
        WHEN new.type = 'creation' THEN CALL insert_user(new);
        WHEN new.type = 'password_update' THEN CALL update_user_password(new);
        ELSE
    END CASE;
    RETURN new;
END;
$$;

CREATE TRIGGER user_update
    BEFORE INSERT
    ON user_event
    FOR EACH ROW
EXECUTE PROCEDURE user_update();

INSERT INTO user_event (subject_id, number, type, data)
VALUES
(
    public.uuid_generate_v4(),
    1,
    'creation',
    ('{"source":{"type":"system"},"content":{"name":"admin","hashedPwd":"$2y$10$JgMWTivQui3cBsGIYcVltO4NEw4QdbXtMtfrlJfCZfSjqEX3uHyyO","role":"super_admin"}}')::jsonb
);
