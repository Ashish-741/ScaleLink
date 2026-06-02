-- ============================================================
-- ScaleLink Database Migration: V1 — Create Users Table
-- ============================================================
-- Flyway migration file. Naming convention: V{version}__{description}.sql
-- Flyway executes migrations in version order and tracks them
-- in a schema_version table so each migration runs exactly once.
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    -- Primary key: bigint for large-scale systems
    -- GENERATED ALWAYS AS IDENTITY is the modern PostgreSQL way
    -- (preferred over SERIAL which is legacy)
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- Email: used for login, must be unique
    -- VARCHAR(100) is generous for emails (RFC 5321 allows 254, but 100 covers 99.9%)
    email       VARCHAR(100) NOT NULL,

    -- Username: display name, must be unique
    username    VARCHAR(50) NOT NULL,

    -- Password hash: BCrypt produces 60-char hashes
    -- Using VARCHAR(255) for flexibility if we change hashing algorithm
    password_hash VARCHAR(255) NOT NULL,

    -- Soft-delete/disable flag
    -- DEFAULT true means new users are active by default
    is_active   BOOLEAN NOT NULL DEFAULT true,

    -- Timestamps: TIMESTAMP WITH TIME ZONE stores in UTC
    -- This avoids timezone bugs — always store UTC, convert on display
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Unique constraints create indexes automatically
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

-- Add a comment for documentation (shows up in psql \d+ users)
COMMENT ON TABLE users IS 'Registered users of ScaleLink platform';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password (never store plaintext)';
COMMENT ON COLUMN users.is_active IS 'false = account disabled/deleted';
