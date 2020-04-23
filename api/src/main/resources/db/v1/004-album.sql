-- @formatter:off
CREATE TYPE album_event_type AS enum ('creation', 'update', 'deletion');

CREATE TABLE album_event
(
    date       timestamp WITH TIME ZONE NOT NULL DEFAULT now(),
    subject_id uuid                     NOT NULL,
    number     bigint                   NOT NULL,
    type       album_event_type         NOT NULL,
    data       jsonb                    NOT NULL,
    PRIMARY KEY (subject_id, number)
);

CREATE TABLE album
(
    id            uuid                     NOT NULL PRIMARY KEY,
    owner_id      uuid                     NOT NULL REFERENCES "user" ON DELETE CASCADE,
    name          varchar(50)              NOT NULL,
    creation_date timestamp WITH TIME ZONE NOT NULL
);

CREATE TABLE album_photo
(
    album_id uuid   NOT NULL REFERENCES album ON DELETE CASCADE,
    photo_id uuid   NOT NULL REFERENCES photo ON DELETE CASCADE,
    "order"  bigint NOT NULL,
    PRIMARY KEY (album_id, photo_id),
    UNIQUE (album_id, photo_id, "order")
);

CREATE FUNCTION next_album_event_number(id uuid) RETURNS bigint
    LANGUAGE SQL AS
$$
SELECT COALESCE(MAX(number), 0) + 1
FROM album_event
WHERE subject_id = $1;
$$;

CREATE FUNCTION next_album_photo_order(album_id uuid) RETURNS bigint
    LANGUAGE SQL AS
$$
SELECT COALESCE(MAX("order"), 0) + 1
FROM album_photo
WHERE album_id = $1;
$$;
