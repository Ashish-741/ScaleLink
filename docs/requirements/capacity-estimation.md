# ScaleLink — Capacity Estimation

> **Document Version**: 1.0
> **Author**: ScaleLink Engineering Team
> **Last Updated**: 2026-06-02

---

## 1. Why Capacity Estimation Matters

In system design interviews and real architecture discussions, engineers are
expected to perform "back-of-the-envelope" calculations to justify their
design decisions. This is NOT guesswork — it's informed estimation that drives
architectural choices.

**Key question**: Should we use a single database? Do we need caching? How much
storage do we need? The answers come from capacity estimation.

---

## 2. Assumptions

| Parameter | Value | Reasoning |
|---|---|---|
| Total users | 500K | Moderate-scale SaaS platform |
| Daily active users (DAU) | 50K | ~10% of total users |
| URLs created per day | 100K | ~2 URLs per active user |
| Read:Write ratio | 100:1 | URL shorteners are read-heavy |
| Redirects per day | 10M | 100 reads per write |
| Average URL length | 200 bytes | Typical long URL |
| Short code length | 7 characters | Base62 encoding |
| URL record size | ~500 bytes | All fields combined |
| Click event size | ~300 bytes | IP, user agent, referrer, etc. |
| Data retention | 5 years | Before archival |

---

## 3. Traffic Estimation

### Writes (URL Creation)

```
URLs created per day    = 100,000
URLs created per second = 100,000 / 86,400 ≈ 1.16 URLs/sec
Peak write rate (3x)    = ~3.5 URLs/sec
```

### Reads (Redirects)

```
Redirects per day       = 10,000,000
Redirects per second    = 10,000,000 / 86,400 ≈ 115 redirects/sec
Peak read rate (3x)     = ~350 redirects/sec
```

### Insight

> This is a **read-heavy** system. For every 1 URL created, it gets clicked
> ~100 times. This means **caching is critical** — we must optimize for reads.

---

## 4. Storage Estimation

### URL Storage

```
URLs per day            = 100,000
URL record size         = 500 bytes
Daily URL storage       = 100,000 × 500 = 50 MB/day
Monthly URL storage     = 50 × 30 = 1.5 GB/month
Yearly URL storage      = 1.5 × 12 = 18 GB/year
5-year URL storage      = 18 × 5 = 90 GB
```

### Click Event Storage

```
Click events per day    = 10,000,000
Click event size        = 300 bytes
Daily click storage     = 10M × 300 = 3 GB/day
Monthly click storage   = 3 × 30 = 90 GB/month
Yearly click storage    = 90 × 12 = 1.08 TB/year
5-year click storage    = 1.08 × 5 = 5.4 TB
```

### Total Storage (5 years)

```
URLs:          90 GB
Click Events:  5.4 TB
─────────────────────
Total:         ~5.5 TB
```

### Insight

> Click events dominate storage. This is why we need:
> 1. **Table partitioning** — Partition click_events by month
> 2. **Data archival** — Move old events to cold storage
> 3. **Aggregation** — Pre-compute daily/weekly/monthly summaries
> 4. **Sharding** — Distribute across multiple databases at scale

---

## 5. Bandwidth Estimation

### Incoming (Writes)

```
URL creation: 1.16 req/sec × 1 KB (request body) = ~1.16 KB/sec
Negligible.
```

### Outgoing (Reads/Redirects)

```
Redirects: 115 req/sec × 500 bytes (response + headers) = ~57.5 KB/sec
Peak: ~172 KB/sec
```

### Insight

> Bandwidth is NOT a bottleneck for this system. The bottleneck is
> **database read throughput** and **latency**, which is why we cache.

---

## 6. Short Code Space Analysis

### Base62 Encoding

Characters: `a-z` (26) + `A-Z` (26) + `0-9` (10) = **62 characters**

```
Code length = 7 characters
Total combinations = 62^7 = 3,521,614,606,208 ≈ 3.5 TRILLION
```

### How Long Until We Run Out?

```
URLs per day    = 100,000
URLs per year   = 36,500,000
Years to exhaust = 3.5 trillion / 36.5 million ≈ 96,000 years
```

### Insight

> With 7-character Base62 codes, we have **3.5 trillion unique codes**.
> Even at 100K URLs/day, this lasts ~96,000 years. No collision concerns.
> 
> This is an important interview answer: "I chose 7 characters because
> 62^7 gives us 3.5 trillion unique codes, which is sufficient for
> decades of growth even at massive scale."

---

## 7. Cache Estimation

### Redis Memory

```
Cached URL mappings     = 1,000,000 (hot URLs)
Per entry               = shortCode (7 bytes) + originalUrl (200 bytes) + overhead (50 bytes) ≈ 257 bytes
Total cache memory      = 1M × 257 bytes ≈ 257 MB
```

### Cache Hit Ratio (Expected)

URL access follows a **Zipf distribution** (power law):
- Top 20% of URLs receive 80% of traffic
- Caching the top 1M URLs should achieve > 90% hit ratio

### Insight

> Redis can comfortably hold 1M+ URL mappings in ~257 MB of RAM.
> A basic Redis instance with 1 GB RAM is more than sufficient.
> With 90%+ cache hit ratio, only 10% of requests hit the database.

---

## 8. Database Connection Estimation

```
Peak redirects/sec          = 350
Cache hit ratio             = 90%
DB queries/sec (reads)      = 350 × 0.10 = 35 queries/sec
DB queries/sec (writes)     = 3.5 URLs/sec + async click events
Total DB queries/sec        = ~50 queries/sec (peak)
```

### Connection Pool Size

```
Rule of thumb: connections = (core_count × 2) + effective_spindle_count
For 4-core server: connections = (4 × 2) + 1 = 9
Recommended pool size: 10-20 connections
```

---

## 9. Summary Table

| Metric | Value |
|---|---|
| Write throughput | ~1.2 URLs/sec (peak: 3.5) |
| Read throughput | ~115 redirects/sec (peak: 350) |
| Read:Write ratio | 100:1 |
| URL storage (5yr) | 90 GB |
| Click storage (5yr) | 5.4 TB |
| Cache memory needed | ~257 MB |
| Short code space | 3.5 trillion |
| DB connections needed | 10-20 |

---

## 10. Architectural Decisions Driven by These Numbers

| Insight | Decision |
|---|---|
| Read-heavy (100:1) | Redis caching is essential |
| Click events dominate storage | Partition by month, aggregate daily |
| Peak 350 reads/sec | Single PostgreSQL can handle this; sharding is for demonstration |
| 90%+ cache hit ratio possible | Cache-aside pattern with Redis |
| Stateless backend | Enables horizontal scaling via load balancer |
| 3.5T short code space | Base62 with 7 chars; no collision risk |
