-- ============================================================
-- ScaleLink Database Migration: V4 — Create Shard Schemas
-- ============================================================
-- This migration sets up the sharding simulation.
-- We create 3 schemas (shard_0, shard_1, shard_2) each with
-- their own urls and click_events tables.
--
-- In production, these would be separate PostgreSQL instances.
-- Using schemas lets us demonstrate the concept with a single DB.
--
-- The shard router determines the schema using:
--   shard_id = hash(shortCode) % 3
-- ============================================================

-- Create shard schemas
CREATE SCHEMA IF NOT EXISTS shard_0;
CREATE SCHEMA IF NOT EXISTS shard_1;
CREATE SCHEMA IF NOT EXISTS shard_2;

-- ============================================================
-- Shard 0: URLs table
-- ============================================================
CREATE TABLE IF NOT EXISTS shard_0.urls (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code      VARCHAR(10) NOT NULL,
    original_url    TEXT NOT NULL,
    custom_alias    VARCHAR(30),
    user_id         BIGINT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    expires_at      TIMESTAMP WITH TIME ZONE,
    click_count     BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shard0_urls_short_code UNIQUE (short_code),
    CONSTRAINT uk_shard0_urls_custom_alias UNIQUE (custom_alias),
    CONSTRAINT chk_shard0_click_count CHECK (click_count >= 0)
);

CREATE INDEX idx_shard0_urls_user_created ON shard_0.urls (user_id, created_at DESC);
CREATE INDEX idx_shard0_urls_user_clicks ON shard_0.urls (user_id, click_count DESC);

-- Shard 0: Click Events table
CREATE TABLE IF NOT EXISTS shard_0.click_events (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url_id          BIGINT NOT NULL,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    referrer        VARCHAR(500),
    country         VARCHAR(100),
    device_type     VARCHAR(20),
    clicked_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_shard0_clicks_url FOREIGN KEY (url_id) REFERENCES shard_0.urls(id) ON DELETE CASCADE
);

CREATE INDEX idx_shard0_clicks_url_date ON shard_0.click_events (url_id, clicked_at);

-- ============================================================
-- Shard 1: URLs table
-- ============================================================
CREATE TABLE IF NOT EXISTS shard_1.urls (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code      VARCHAR(10) NOT NULL,
    original_url    TEXT NOT NULL,
    custom_alias    VARCHAR(30),
    user_id         BIGINT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    expires_at      TIMESTAMP WITH TIME ZONE,
    click_count     BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shard1_urls_short_code UNIQUE (short_code),
    CONSTRAINT uk_shard1_urls_custom_alias UNIQUE (custom_alias),
    CONSTRAINT chk_shard1_click_count CHECK (click_count >= 0)
);

CREATE INDEX idx_shard1_urls_user_created ON shard_1.urls (user_id, created_at DESC);
CREATE INDEX idx_shard1_urls_user_clicks ON shard_1.urls (user_id, click_count DESC);

-- Shard 1: Click Events table
CREATE TABLE IF NOT EXISTS shard_1.click_events (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url_id          BIGINT NOT NULL,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    referrer        VARCHAR(500),
    country         VARCHAR(100),
    device_type     VARCHAR(20),
    clicked_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_shard1_clicks_url FOREIGN KEY (url_id) REFERENCES shard_1.urls(id) ON DELETE CASCADE
);

CREATE INDEX idx_shard1_clicks_url_date ON shard_1.click_events (url_id, clicked_at);

-- ============================================================
-- Shard 2: URLs table
-- ============================================================
CREATE TABLE IF NOT EXISTS shard_2.urls (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code      VARCHAR(10) NOT NULL,
    original_url    TEXT NOT NULL,
    custom_alias    VARCHAR(30),
    user_id         BIGINT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    expires_at      TIMESTAMP WITH TIME ZONE,
    click_count     BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shard2_urls_short_code UNIQUE (short_code),
    CONSTRAINT uk_shard2_urls_custom_alias UNIQUE (custom_alias),
    CONSTRAINT chk_shard2_click_count CHECK (click_count >= 0)
);

CREATE INDEX idx_shard2_urls_user_created ON shard_2.urls (user_id, created_at DESC);
CREATE INDEX idx_shard2_urls_user_clicks ON shard_2.urls (user_id, click_count DESC);

-- Shard 2: Click Events table
CREATE TABLE IF NOT EXISTS shard_2.click_events (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url_id          BIGINT NOT NULL,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    referrer        VARCHAR(500),
    country         VARCHAR(100),
    device_type     VARCHAR(20),
    clicked_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_shard2_clicks_url FOREIGN KEY (url_id) REFERENCES shard_2.urls(id) ON DELETE CASCADE
);

CREATE INDEX idx_shard2_clicks_url_date ON shard_2.click_events (url_id, clicked_at);

-- Documentation
COMMENT ON SCHEMA shard_0 IS 'URL shard 0: handles shortCodes where hash(code) % 3 = 0';
COMMENT ON SCHEMA shard_1 IS 'URL shard 1: handles shortCodes where hash(code) % 3 = 1';
COMMENT ON SCHEMA shard_2 IS 'URL shard 2: handles shortCodes where hash(code) % 3 = 2';
