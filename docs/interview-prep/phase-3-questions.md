# ScaleLink — Phase 3 Interview Preparation

> Database design questions are among the most common in backend engineering
> interviews. These questions test your ability to design schemas that perform
> well at scale.

---

## Database Design Questions

### Q1: Walk me through your database schema for a URL shortener.

**Answer Framework**:

"I have three core tables:

1. **users** — stores registered users with BCrypt-hashed passwords, unique
   email and username constraints. Uses `bigint` primary keys for scale.

2. **urls** — the core table. Each row maps a `short_code` (7-char Base62)
   to an `original_url`. Has a unique index on `short_code` which is the
   primary lookup key for redirects. Supports optional `custom_alias` and
   `expires_at`. Has a denormalized `click_count` for dashboard performance.

3. **click_events** — append-only event log. Records every click with IP,
   user agent, referrer, device type, and timestamp. Has a composite index
   on `(url_id, clicked_at)` optimized for analytics date-range queries.

The key trade-off is the denormalized `click_count` in the urls table. I
sacrifice perfect consistency (count could be off by 1-2 during failures)
for massive read performance on the dashboard."

---

### Q2: Why did you choose `bigint` over `int` for primary keys?

**Answer**:
- `int` max = 2.1 billion. For click_events at 10M/day, we'd exceed this in
  ~214 days. That's a ticking time bomb.
- `bigint` max = 9.2 quintillion. Effectively unlimited for any realistic scale.
- The storage cost is minimal: 8 bytes vs 4 bytes per row.
- Changing a primary key type on a live table with billions of rows is a
  nightmare. Get it right from the start.

---

### Q3: Explain your indexing strategy. How did you decide which indexes to create?

**Answer**:
"I started by listing every query the application executes, then designed
indexes to support each one:

1. **Redirect lookup**: `UNIQUE (short_code)` — the hottest query, must be < 1ms
2. **Dashboard list**: Composite `(user_id, created_at DESC)` — covers sorting
3. **Top URLs**: Composite `(user_id, click_count DESC)` — covers the ORDER BY
4. **Analytics**: Composite `(url_id, clicked_at)` — the critical analytics index
5. **Expiration cleanup**: Partial index on `expires_at WHERE NOT NULL` — smaller, faster

I specifically avoided indexing every column. Each index slows down writes
and consumes storage. I only index columns that appear in WHERE, JOIN, or
ORDER BY clauses of actual application queries."

---

### Q4: What is a composite index? Why does column order matter?

**Answer**:
- A composite index is an index on multiple columns, e.g., `(url_id, clicked_at)`
- Column order determines which queries can use the index:
  - `WHERE url_id = 42 AND clicked_at > '2026-01-01'` → ✅ Uses index perfectly
  - `WHERE url_id = 42` → ✅ Uses first column of index
  - `WHERE clicked_at > '2026-01-01'` → ❌ Cannot use this index (skips first column)
- **Rule**: Put equality columns first, range columns last
- Think of it like a phone book: sorted by (last name, first name). You can
  look up "Smith" but you can't efficiently look up everyone named "John"

---

### Q5: How would you handle the click_events table growing to billions of rows?

**Answer** (in order of complexity):

1. **Composite indexes** — already done. Queries stay fast even with billions
   of rows because indexes narrow the scan.

2. **Table partitioning** — partition by month on `clicked_at`. Queries with
   date ranges only scan relevant partitions. Old partitions can be dropped.

3. **Pre-aggregation** — create a `daily_click_summary` materialized view
   that aggregates daily counts. Dashboard reads from the summary, not raw events.

4. **Archival** — move events older than 90 days to cold storage (S3/GCS).
   Keep only recent data in the hot database.

5. **Sharding** — distribute click_events across multiple databases based on
   url_id. Each shard handles a subset.

"I'd implement these in order as we grow. No need to jump to sharding when
partitioning solves the problem."

---

### Q6: What is a partial index and when would you use one?

**Answer**:
- A partial index only indexes rows that match a condition
- Example: `CREATE INDEX idx ON urls (expires_at) WHERE expires_at IS NOT NULL`
- Only ~30% of URLs have expiration dates. A full index would include 70%
  useless rows.
- Partial index is smaller, faster to update, and faster to scan
- Use when: a query always includes a specific condition that filters out
  most rows

---

### Q7: Why denormalize click_count? Isn't that bad practice?

**Answer**:
"Denormalization is NOT bad practice — it's a conscious trade-off.

**Without denormalization**:
```sql
-- Dashboard loads 20 URLs, each needs this query
SELECT COUNT(*) FROM click_events WHERE url_id = ?;
-- 20 COUNT queries × potentially millions of rows each = slow dashboard
```

**With denormalization**:
```sql
-- Dashboard just reads the column
SELECT click_count FROM urls WHERE user_id = ? LIMIT 20;
-- Single query, no counting = instant dashboard
```

The trade-off:
- ✅ Dashboard is 100x faster
- ❌ Count might be off by 1-2 during concurrent updates
- ❌ Must remember to increment on every click

For a display counter, being off by 1 is acceptable. When the user views
detailed analytics, we can run the exact COUNT query."

---

### Q8: Explain the difference between SERIAL and GENERATED ALWAYS AS IDENTITY.

**Answer**:
- `SERIAL` is a legacy PostgreSQL shortcut that creates a sequence
- `GENERATED ALWAYS AS IDENTITY` is the SQL standard (PostgreSQL 10+)
- Key differences:
  - IDENTITY prevents manual ID insertion (prevents accidental conflicts)
  - IDENTITY is SQL standard (portable across databases)
  - SERIAL allows `INSERT INTO users (id, ...) VALUES (999, ...)` — dangerous
  - IDENTITY blocks that unless you use `OVERRIDING SYSTEM VALUE`
- **Best practice**: Always use `GENERATED ALWAYS AS IDENTITY` in new projects

---

### Q9: Why TIMESTAMP WITH TIME ZONE instead of TIMESTAMP?

**Answer**:
- `TIMESTAMP` (without timezone) stores the literal time — no timezone info
- `TIMESTAMP WITH TIME ZONE` converts to UTC on storage, converts back on retrieval
- If your server is in UTC but your user is in IST, `TIMESTAMP` will show
  wrong times. `TIMESTAMPTZ` handles it correctly.
- **Rule**: Always use `TIMESTAMPTZ` in PostgreSQL. It stores in UTC internally
  and lets the client's timezone setting handle display.

---

## Common Mistakes in Database Design

1. **Using `int` for PKs** — runs out faster than you think
2. **No indexes on foreign keys** — PostgreSQL does NOT auto-index FKs (unlike MySQL)
3. **Over-indexing** — indexing every column slows writes and wastes storage
4. **VARCHAR(255) for everything** — use appropriate sizes; it documents intent
5. **TIMESTAMP without timezone** — timezone bugs are painful to debug in production
6. **Missing ON DELETE CASCADE** — orphaned rows when parent is deleted
7. **No partial indexes** — full indexes on columns with many NULLs waste space
8. **Forgetting denormalization** — running COUNT on millions of rows per page load
