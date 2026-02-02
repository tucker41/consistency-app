ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS username VARCHAR(30);

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_username_lower
    ON app_user (LOWER(username))
    WHERE username IS NOT NULL;
