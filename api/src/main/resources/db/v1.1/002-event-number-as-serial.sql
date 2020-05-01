-- noinspection SqlResolveForFile @ column/"id"

-- @formatter:off
DO
$$
<<update_user_event>>
DECLARE
    _event          record;
    _current_number bigint;
BEGIN
    ALTER TABLE user_event DROP CONSTRAINT user_event_pkey,
                           ADD COLUMN id uuid NOT NULL DEFAULT public.uuid_generate_v4();
    _current_number = 1;
    FOR _event IN SELECT * FROM user_event ORDER BY date LOOP
        UPDATE user_event
        SET number = _current_number
        WHERE id = _event.id;
        _current_number = _current_number + 1;
    END LOOP;
    CREATE SEQUENCE user_event_number_seq;
    PERFORM setval('user_event_number_seq', _current_number);
    ALTER TABLE user_event ALTER COLUMN number SET DEFAULT nextval('user_event_number_seq'),
                           ALTER COLUMN number SET NOT NULL,
                           ADD PRIMARY KEY (number),
                           DROP COLUMN id;
END update_user_event;
$$;
DROP FUNCTION next_user_event_number;

DO
$$
<<update_photo_event>>
    DECLARE
    _event          record;
    _current_number bigint;
BEGIN
    ALTER TABLE photo_event DROP CONSTRAINT photo_event_pkey,
                           ADD COLUMN id uuid NOT NULL PRIMARY KEY DEFAULT public.uuid_generate_v4();
    _current_number = 1;
    FOR _event IN SELECT * FROM photo_event ORDER BY date LOOP
            UPDATE photo_event
            SET number = _current_number
            WHERE id = _event.id;
            _current_number = _current_number + 1;
        END LOOP;
    CREATE SEQUENCE photo_event_number_seq;
    PERFORM setval('photo_event_number_seq', _current_number);
    ALTER TABLE photo_event ALTER COLUMN number SET DEFAULT nextval('photo_event_number_seq'),
                           ALTER COLUMN number SET NOT NULL,
                            ADD PRIMARY KEY (number),
                            DROP COLUMN id;
END update_photo_event;
$$;
DROP FUNCTION next_photo_event_number;

DO
$$
<<update_album_event>>
    DECLARE
    _event          record;
    _current_number bigint;
BEGIN
    ALTER TABLE album_event DROP CONSTRAINT album_event_pkey,
                            ADD COLUMN id uuid NOT NULL PRIMARY KEY DEFAULT public.uuid_generate_v4();
    _current_number = 1;
    FOR _event IN SELECT * FROM album_event ORDER BY date LOOP
            UPDATE album_event
            SET number = _current_number
            WHERE id = _event.id;
            _current_number = _current_number + 1;
        END LOOP;
    CREATE SEQUENCE album_event_number_seq;
    PERFORM setval('album_event_number_seq', _current_number);
    ALTER TABLE album_event ALTER COLUMN number SET DEFAULT nextval('album_event_number_seq'),
                            ALTER COLUMN number SET NOT NULL,
                            ADD PRIMARY KEY (number),
                            DROP COLUMN id;
END update_album_event;
$$;
DROP FUNCTION next_album_event_number;
