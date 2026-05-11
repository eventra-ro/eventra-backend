-- V4__add_password_reset_tokens.sql
-- Tokeni pentru resetarea parolei — lipseau din V1

CREATE TABLE password_reset_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pwreset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_pwreset_hash ON password_reset_tokens(token_hash);