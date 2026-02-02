ALTER TABLE app_user
    ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_provider
    ON app_user (auth_provider, provider_id)
    WHERE provider_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_display_name
    ON app_user (display_name);
