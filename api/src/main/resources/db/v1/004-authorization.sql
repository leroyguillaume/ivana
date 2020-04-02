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
