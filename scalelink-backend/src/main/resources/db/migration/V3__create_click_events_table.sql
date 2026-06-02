-- ============================================================
-- ScaleLink Database Migration: V3 — Create Click Events Table
-- ============================================================
-- This table records every click/redirect event.
-- It will be the LARGEST table in the system.
-- Design priorities: fast writes (async inserts) and 
-- efficient analytics queries (composite indexes).
-- ============================================================

CREATE TABLE IF NOT EXISTS click_events (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- Which URL was clicked
    url_id          BIGINT NOT NULL,

    -- Client IP address
    -- VARCHAR(45) supports IPv6 (max 45 chars: xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx)
    ip_address      VARCHAR(45),

    -- Full user agent string for device/browser detection
    -- Using TEXT because user agents can be very long
    user_agent      TEXT,

    -- HTTP Referer header (where the user came from)
    -- Note: "Referrer" is the correct English spelling but HTTP uses "Referer" (historical typo)
    referrer        VARCHAR(500),

    -- Country derived from IP geolocation
    -- Populated by the application layer, not the database
    country         VARCHAR(100),

    -- Parsed device type: 'mobile', 'desktop', 'tablet', 'unknown'
    device_type     VARCHAR(20),

    -- When the click occurred
    clicked_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Foreign key to urls table
    CONSTRAINT fk_clicks_url FOREIGN KEY (url_id) REFERENCES urls(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes — Designed for Analytics Queries
-- ============================================================

-- THE most important index in the system for analytics
-- Composite index: (url_id, clicked_at) 
-- Supports: "Give me clicks for URL X between dates Y and Z"
-- Column order matters: equality (url_id) first, range (clicked_at) second
CREATE INDEX idx_clicks_url_date ON click_events (url_id, clicked_at);

-- Index for time-based queries across all URLs
-- Supports: "Recent activity" and global analytics
CREATE INDEX idx_clicks_clicked_at ON click_events (clicked_at DESC);

-- Index for unique click counting by IP per URL
-- Supports: COUNT(DISTINCT ip_address) WHERE url_id = X
CREATE INDEX idx_clicks_url_ip ON click_events (url_id, ip_address);

-- Table documentation
COMMENT ON TABLE click_events IS 'Records every redirect/click event for analytics';
COMMENT ON COLUMN click_events.ip_address IS 'Client IP (IPv4 or IPv6) for unique click counting';
COMMENT ON COLUMN click_events.device_type IS 'Parsed from user_agent: mobile, desktop, tablet, unknown';
COMMENT ON COLUMN click_events.country IS 'Derived from IP geolocation in application layer';
COMMENT ON COLUMN click_events.referrer IS 'HTTP Referer header — where the click originated';
