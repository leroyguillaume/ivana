-- @formatter:off
CREATE TABLE user_photo_authorization
(
    user_id    uuid NOT NULL REFERENCES "user" ON DELETE CASCADE,
    photo_id   uuid NOT NULL REFERENCES photo  ON DELETE CASCADE,
    can_read   bool NOT NULL,
    can_update bool NOT NULL,
    can_delete bool NOT NULL,
    PRIMARY KEY (photo_id, user_id)
);

CREATE TABLE user_album_authorization
(
    user_id    uuid NOT NULL REFERENCES "user" ON DELETE CASCADE,
    album_id   uuid NOT NULL REFERENCES album  ON DELETE CASCADE,
    can_read   bool NOT NULL,
    can_update bool NOT NULL,
    can_delete bool NOT NULL,
    PRIMARY KEY (album_id, user_id)
);
