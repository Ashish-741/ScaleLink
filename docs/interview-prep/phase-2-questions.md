# ScaleLink — Phase 2 Interview Preparation

> Practice explaining these concepts out loud as if you're in a system design
> interview. The best engineers don't just know the answers — they can 
> articulate the trade-offs clearly.

---

## System Design Interview Questions

### Q1: Design a URL shortener. Walk me through the high-level architecture.

**Answer Framework** (use this structure in every system design interview):

1. **Clarify requirements** (2 min)
   - Functional: Shorten URLs, redirect, analytics, custom aliases
   - Non-functional: Low latency redirects, high availability, 100:1 read-write ratio

2. **Capacity estimation** (3 min)
   - 100K URLs/day, 10M redirects/day
   - 115 reads/sec, 1.2 writes/sec
   - 90 GB URL storage (5 years), 5.4 TB click events

3. **API design** (2 min)
   - POST /api/v1/urls → create
   - GET /{shortCode} → 302 redirect
   - GET /api/v1/urls/{shortCode}/analytics → analytics

4. **Database schema** (3 min)
   - users, urls (with indexes on short_code), click_events (partitioned)

5. **High-level design** (5 min)
   - Client → Nginx → Spring Boot (stateless) → Redis → PostgreSQL
   - Cache-aside for reads, async click recording

6. **Deep dives** (10 min)
   - Short code generation (Base62, collision handling)
   - Caching strategy (cache-aside, 90% hit ratio)
   - Sharding (hash-based on shortCode)
   - Rate limiting (token bucket)

---

### Q2: How would you handle 1 billion URLs?

**Answer**:
- **Storage**: 1B × 500 bytes = 500 GB — fits on a single server but queries slow down
- **Solution**: Shard using `hash(shortCode) % N`
  - 10 shards → 100M URLs per shard → fast queries
  - Each shard is an independent PostgreSQL instance
- **Cache**: Hot URLs in Redis — with power-law distribution, top 1% of URLs
  get 99% of traffic. Cache those.
- **Short code space**: 62^7 = 3.5 trillion — still sufficient for 1B URLs
- **Click events**: Partition by month, aggregate into daily summaries,
  archive old raw events to cold storage (S3/GCS)

---

### Q3: Explain the CAP theorem using your URL shortener as an example.

**Answer**:
- CAP says: in a distributed system, you can have at most 2 of 3: Consistency,
  Availability, Partition Tolerance
- Network partitions ARE going to happen → P is mandatory
- So we choose between C and A per feature:
  - **URL creation**: We need Consistency — two users must NOT get the same
    short code. We use a unique constraint in the database.
  - **URL redirect**: We prioritize Availability — serving a slightly stale
    cached URL is better than returning an error. We accept eventual consistency.
  - **Analytics**: We prioritize Availability — click count being off by 1-2
    for a few seconds is acceptable. We record clicks asynchronously.

---

### Q4: Why did you choose 302 instead of 301 for redirects?

**Answer**:
- **301 (Permanent)**: Browser caches the redirect. Next time the user clicks
  the short URL, the browser goes directly to the destination — it never hits
  our server. We lose:
  - Click tracking / analytics
  - Ability to change the destination URL
  - Rate limiting (request never reaches us)

- **302 (Temporary)**: Browser asks our server every time. We can:
  - Track every click
  - Change the destination URL anytime
  - Enforce rate limits
  - Handle URL expiration

- **Trade-off**: 302 means more load on our server (every click hits us),
  but the analytics and flexibility are essential for a URL shortener product.

---

### Q5: How does caching improve your system's performance?

**Answer**:
- Without cache: every redirect → PostgreSQL query (~10-50ms)
- With Redis cache: most redirects → Redis lookup (~1ms)
- URL access follows a Zipf distribution (power law) — top 20% of URLs get
  80% of traffic
- We cache URL mappings with 24h TTL
- Expected cache hit ratio: 90%+
- This means only 10% of redirects hit the database
- Performance improvement: 10-50x for cached URLs

**Cache invalidation**:
- On URL update: immediately delete from cache
- On URL delete: immediately delete from cache
- TTL of 24h ensures stale entries eventually expire

---

### Q6: What happens if Redis goes down?

**Answer** (graceful degradation):
- System continues working — falls back to direct PostgreSQL queries
- Redirect latency increases from ~1ms to ~10-50ms
- Rate limiting falls back to in-memory (per-instance, less accurate)
- No data loss — Redis is a cache, not the source of truth
- This is what "graceful degradation" means — the system gets slower but
  doesn't break

---

### Q7: How would you implement rate limiting? Compare the algorithms.

**Answer**:

| Algorithm | Pros | Cons | Best For |
|---|---|---|---|
| Fixed Window | Simple, low memory | Boundary burst problem | Simple APIs |
| Sliding Window | Smooth, no burst issue | More memory, more complex | Production APIs |
| Token Bucket | Allows controlled bursts | More state to manage | API gateways |

"In ScaleLink, I implemented all three algorithms behind a Strategy pattern
interface, configurable via application.yml. In production, I'd use
Sliding Window for most APIs and Token Bucket for the redirect endpoint
to allow short bursts of traffic."

---

### Q8: Why is your backend stateless? What does that enable?

**Answer**:
- No server-side sessions — all auth state is in the JWT token
- Any server instance can handle any request
- Enables horizontal scaling: add more instances behind a load balancer
- Enables zero-downtime deployments: drain and replace one instance at a time
- Enables container orchestration: Kubernetes/Docker can restart instances freely
- Trade-off: JWT tokens can't be revoked instantly (we use short TTL +
  refresh tokens to mitigate)

---

### Q9: Explain your sharding strategy. Why shortCode and not userId?

**Answer**:
- Shard key: `hash(shortCode) % N`
- The hottest query is redirect: `GET /{shortCode}` → need to find URL by shortCode
- If we sharded by userId, a redirect would require checking ALL shards
  (because we don't know which user created it)
- By sharding on shortCode, redirect queries go directly to the correct shard
- Trade-off: "get all URLs for user X" now requires a scatter-gather across
  all shards. But this is a much less frequent query, so it's an acceptable
  trade-off.

---

## Common Mistakes in System Design

1. **Over-engineering**: Don't shard from day 1 — start simple, shard when
   needed (but show you KNOW how)
2. **Ignoring the read/write ratio**: This fundamentally shapes your design
3. **Using 301 redirects**: Losing all analytics capability
4. **Not mentioning trade-offs**: Every decision has a downside — interviewers
   want to hear you discuss them
5. **Forgetting about failure**: What if Redis dies? What if a shard goes down?
6. **No numbers**: "It's fast" vs "It handles 350 req/sec with p99 < 50ms"
7. **Monolithic thinking**: Not considering how to scale each component independently
8. **Premature optimization**: Design for current scale, plan for future scale
