-- @formatter:off
CREATE FUNCTION user_can_read(photo_id uuid, user_id uuid) RETURNS bool
    LANGUAGE plpgsql AS
$$
DECLARE
    _can_read bool;
BEGIN
    SELECT upa.can_read
    FROM user_photo_authorization upa
    WHERE upa.photo_id = $1 AND upa.user_id = $2
    INTO _can_read;
    IF _can_read IS NULL THEN
        SELECT EXISTS(
            SELECT 1
            FROM user_album_authorization uaa
            WHERE uaa.user_id = $2
              AND uaa.can_read
              AND $1 IN (SELECT ap.photo_id FROM album_photo ap WHERE ap.album_id = uaa.album_id)
        )
        INTO _can_read;
    END IF;
    RETURN _can_read;
END;
$$;
