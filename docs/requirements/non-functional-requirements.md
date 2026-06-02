# ScaleLink — Non-Functional Requirements

> **Document Version**: 1.0
> **Author**: ScaleLink Engineering Team
> **Last Updated**: 2026-06-02

---

## 1. Overview

Non-functional requirements define HOW the system should behave, not WHAT it
should do. These are the quality attributes that separate a student CRUD project
from a production-grade system.

---

## 2. Performance

| ID | Requirement | Target |
|----|-------------|--------|
| PF-01 | URL redirect latency (cache hit) | < 50ms (p99) |
| PF-02 | URL redirect latency (cache miss) | < 200ms (p99) |
| PF-03 | URL creation latency | < 300ms (p99) |
| PF-04 | API response time (general) | < 500ms (p99) |
| PF-05 | Dashboard page load | < 2 seconds |
| PF-06 | Cache hit ratio for URL lookups | > 90% |

### Why These Numbers?

- **50ms redirect (cache hit)**: Bitly achieves ~30ms. We target 50ms as a 
  realistic goal. Users perceive anything > 200ms as "slow".
- **90% cache hit ratio**: URL access follows a power-law distribution — a small
  percentage of URLs receive the majority of traffic. This makes caching highly 
  effective.

---

## 3. Scalability

| ID | Requirement | Target |
|----|-------------|--------|
| SC-01 | Concurrent users supported | 1,000+ |
| SC-02 | URLs stored | 10M+ (design for 100M) |
| SC-03 | Click events per day | 1M+ |
| SC-04 | Horizontal scaling | Stateless backend, add instances behind load balancer |
| SC-05 | Database scaling | Sharding simulation with 3 shards |

### Design Principles

- **Stateless Backend**: No server-side sessions. JWT tokens carry all auth state.
  This allows any instance to serve any request.
- **Database Sharding**: Distribute URLs across multiple shards using 
  `hash(shortCode) % N` to prevent any single database from becoming a bottleneck.
- **Async Processing**: Click event recording is asynchronous — it should never
  slow down the redirect response.

---

## 4. Availability

| ID | Requirement | Target |
|----|-------------|--------|
| AV-01 | System uptime | 99.9% (three nines) |
| AV-02 | Redirect service availability | 99.99% (four nines, aspirational) |
| AV-03 | Graceful degradation | System works without Redis (fallback to DB) |
| AV-04 | Zero-downtime deployments | Rolling updates via Docker |

### What Do the Nines Mean?

| Availability | Downtime/Year | Downtime/Month |
|---|---|---|
| 99% | 3.65 days | 7.3 hours |
| 99.9% | 8.76 hours | 43.8 minutes |
| 99.99% | 52.6 minutes | 4.38 minutes |
| 99.999% | 5.26 minutes | 26.3 seconds |

---

## 5. Security

| ID | Requirement | Target |
|----|-------------|--------|
| SE-01 | Authentication | JWT with RS256 or HS256 |
| SE-02 | Password storage | BCrypt with cost factor 12 |
| SE-03 | Input validation | All user inputs validated server-side |
| SE-04 | SQL injection prevention | Parameterized queries via JPA |
| SE-05 | XSS prevention | Output encoding, Content-Security-Policy headers |
| SE-06 | CSRF protection | Not needed for stateless JWT APIs |
| SE-07 | Rate limiting | Per-user and per-IP |
| SE-08 | HTTPS | Enforced in production |
| SE-09 | CORS | Whitelist frontend domain only |
| SE-10 | Secrets management | Environment variables, never in code |

### Why JWT Instead of Session Cookies?

- **Stateless**: No server-side session storage needed
- **Scalable**: Any server instance can validate the token
- **Mobile-friendly**: Works with any client, not just browsers
- **Trade-off**: Tokens can't be revoked instantly (we mitigate with short TTL + refresh tokens)

---

## 6. Reliability

| ID | Requirement | Target |
|----|-------------|--------|
| RL-01 | Data durability | No URL mapping data loss |
| RL-02 | Unique short codes | Collision probability < 1 in 1 billion |
| RL-03 | Idempotent operations | Creating the same URL twice returns the same short code |
| RL-04 | Transaction integrity | ACID compliance for writes |

---

## 7. Observability

| ID | Requirement | Target |
|----|-------------|--------|
| OB-01 | Request logging | All API requests logged with correlation ID |
| OB-02 | Health checks | `/actuator/health` endpoint |
| OB-03 | Metrics | Micrometer metrics exposed via Actuator |
| OB-04 | Structured logging | JSON format in production |
| OB-05 | Error tracking | Global exception handler with meaningful error codes |

---

## 8. Maintainability

| ID | Requirement | Target |
|----|-------------|--------|
| MT-01 | Code coverage | > 80% |
| MT-02 | API documentation | Auto-generated Swagger/OpenAPI |
| MT-03 | Database migrations | Versioned with Flyway |
| MT-04 | Configuration | Environment-specific profiles (dev, prod) |
| MT-05 | Code style | Consistent formatting, meaningful names |

---

## 9. Rate Limiting

| ID | Requirement | Target |
|----|-------------|--------|
| RT-01 | URL creation | 10 requests/minute per user |
| RT-02 | URL redirect | 100 requests/minute per IP |
| RT-03 | API general | 60 requests/minute per user |
| RT-04 | Rate limit response | HTTP 429 Too Many Requests |
| RT-05 | Rate limit headers | X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset |
