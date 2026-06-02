-- ============================================================
-- ScaleLink Database Migration: V2 — Create URLs Table
-- ============================================================
-- This is the core table of the entire system.
-- Every short URL mapping lives here.
-- ============================================================

CREATE TABLE IF NOT EXISTS urls (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- The system-generated 7-character Base62 short code
    -- This is the primary lookup key for redirects
    -- VARCHAR(10) gives room for future expansion
    short_code      VARCHAR(10) NOT NULL,

    -- The destination URL — using TEXT because URLs can be very long
    -- In PostgreSQL, TEXT has no performance difference from VARCHAR
    original_url    TEXT NOT NULL,

    -- Optional user-chosen branded alias (e.g., "my-brand")
    -- NULL means no custom alias was set
    custom_alias    VARCHAR(30),

    -- Foreign key to the user who created this URL
    user_id         BIGINT NOT NULL,

    -- Soft-delete/disable flag
    is_active       BOOLEAN NOT NULL DEFAULT true,

    -- Optional expiration timestamp
    -- NULL means the URL never expires
    expires_at      TIMESTAMP WITH TIME ZONE,

    -- Denormalized click counter for dashboard performance
    -- This avoids COUNT(*) on click_events for every dashboard load
    -- Trade-off: slightly stale count vs. query performance
    click_count     BIGINT NOT NULL DEFAULT 0,

    -- Timestamps
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT uk_urls_short_code UNIQUE (short_code),
    CONSTRAINT uk_urls_custom_alias UNIQUE (custom_alias),
    CONSTRAINT fk_urls_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_click_count CHECK (click_count >= 0)
);

-- ============================================================
-- Indexes (beyond the unique constraints which auto-create indexes)
-- ============================================================

-- Index for "List all my URLs" query (user's dashboard)
-- Composite with created_at DESC for default sort order
CREATE INDEX idx_urls_user_created ON urls (user_id, created_at DESC);

-- Index for "My top URLs" query (dashboard top 5)
CREATE INDEX idx_urls_user_clicks ON urls (user_id, click_count DESC);

-- Index for finding expired URLs (cleanup job)
-- Partial index: only index rows that CAN expire (expires_at IS NOT NULL)
-- This makes the index much smaller and faster
CREATE INDEX idx_urls_expires_at ON urls (expires_at)
    WHERE expires_at IS NOT NULL;

-- Index for filtering active/inactive URLs per user
CREATE INDEX idx_urls_active_user ON urls (is_active, user_id);

-- Table documentation
COMMENT ON TABLE urls IS 'Short URL mappings — the core table of ScaleLink';
COMMENT ON COLUMN urls.short_code IS 'System-generated 7-char Base62 code (always exists)';
COMMENT ON COLUMN urls.custom_alias IS 'User-chosen branded alias (optional, must be unique if set)';
COMMENT ON COLUMN urls.click_count IS 'Denormalized counter — source of truth is COUNT(*) from click_events';
COMMENT ON COLUMN urls.expires_at IS 'NULL means never expires';
