-- @formatter:off
CREATE TYPE photo_type AS enum ('jpg', 'png');

CREATE TYPE photo_event_type AS enum ('upload', 'visualization', 'deletion', 'transform');

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
    no          serial                   NOT NULL UNIQUE,
    UNIQUE (owner_id, hash)
);

CREATE FUNCTION next_photo_event_number(id uuid) RETURNS bigint
    LANGUAGE SQL AS
$$
SELECT COALESCE(MAX(number), 0) + 1
FROM photo_event
WHERE subject_id = $1;
$$;
