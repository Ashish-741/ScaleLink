# ScaleLink — Phase 1 Interview Preparation

> These are real interview questions you should be able to answer after 
> completing Phase 1. Practice explaining these out loud.

---

## Requirements & Estimation Questions

### Q1: How would you estimate the storage needed for 100M URLs?

**Answer Framework**:
- Each URL record ≈ 500 bytes (original URL, short code, metadata, timestamps)
- 100M × 500 bytes = 50 GB for URL records
- Click events: assume 100 clicks per URL average = 10 billion events
- Each click event ≈ 300 bytes → 3 TB for click events
- Total: ~3 TB (dominated by click events, not URLs)

**Key insight**: The URL table is small. The analytics (click_events) table 
is what requires careful design — partitioning, aggregation, archival.

---

### Q2: What's the read-to-write ratio for a URL shortener?

**Answer**:
- URL shorteners are **extremely read-heavy** — typically 100:1 or even 1000:1
- For every URL created (write), it gets clicked hundreds of times (reads)
- This means we optimize for **read performance**: caching, read replicas, CDN
- Writes are relatively infrequent and can tolerate slightly higher latency

---

### Q3: Why do we need back-of-the-envelope estimation?

**Answer**:
- To justify architectural decisions with data, not gut feelings
- To determine if we need caching (yes, because read-heavy)
- To determine if we need sharding (at what scale?)
- To estimate infrastructure costs
- To identify bottlenecks before they happen
- Interviewers use this to test your ability to think at scale

---

### Q4: How would you design the API for a URL shortener?

**Answer**:
- RESTful API with versioning (`/api/v1/`)
- Consistent JSON response envelope for all endpoints
- Proper HTTP status codes (201 Created, 302 Redirect, 429 Too Many Requests)
- Authentication via JWT Bearer token
- Public redirect endpoint separate from authenticated management APIs
- Pagination for list endpoints
- Rate limiting with standard headers

---

### Q5: What HTTP status codes are appropriate for a redirect service?

**Answer**:
- **302 Found** (Temporary Redirect): Use this for URL shorteners because:
  - Browser doesn't cache the redirect permanently
  - Every click hits our server → we can track analytics
  - We can change the destination URL later
- **301 Moved Permanently**: DON'T use because:
  - Browser caches forever → clicks bypass our server
  - We lose analytics tracking
  - We can't change the destination
- **404 Not Found**: Short code doesn't exist
- **410 Gone**: URL has expired (tells client "this existed but is gone")

---

### Q6: Why use Base62 encoding instead of Base64 or UUID?

**Answer**:
- **Base62** (a-z, A-Z, 0-9) = 62 characters
  - URL-safe: no special characters that need encoding
  - Compact: 7 characters = 3.5 trillion unique codes
  - Human-readable and typeable
- **Base64** includes `+` and `/` which are not URL-safe
- **UUID** is 36 characters — defeats the purpose of shortening
- **Sequential IDs** are predictable → security concern (enumeration attacks)

---

### Q7: How do you handle URL expiration?

**Answer**:
- Store `expires_at` timestamp in the database
- On redirect: check if `NOW() > expires_at`
- Return **410 Gone** for expired URLs (not 404 — semantically different)
- Background job can periodically mark expired URLs as inactive
- Cache entries for expired URLs should also be invalidated

---

### Q8: Why separate the redirect endpoint from the API?

**Answer**:
- Redirect: `GET /{shortCode}` — public, no auth, fast, cached
- API: `GET /api/v1/urls/{shortCode}` — authenticated, returns JSON details
- Different concerns: redirect is for end users, API is for the URL owner
- Redirect must be extremely fast (< 50ms), API can be slightly slower
- Redirect doesn't go through the full auth filter chain

---

## Common Mistakes in Requirements Gathering

1. **Not thinking about scale** — designing for 100 URLs when you need to 
   handle millions
2. **Ignoring read-heavy nature** — treating reads and writes equally
3. **Using 301 redirects** — losing all analytics capability
4. **Forgetting URL expiration** — leaving expired URLs accessible forever
5. **No rate limiting in requirements** — getting DDoS'd on day one
6. **Sequential short codes** — allowing enumeration attacks
7. **Not versioning the API** — breaking clients with every change
8. **Inconsistent error responses** — different formats for different endpoints
