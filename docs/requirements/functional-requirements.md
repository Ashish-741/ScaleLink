# ScaleLink — Functional Requirements

> **Document Version**: 1.0
> **Author**: ScaleLink Engineering Team
> **Last Updated**: 2026-06-02

---

## 1. Overview

This document defines all user-facing functional requirements for ScaleLink,
a production-grade URL shortener platform. Each requirement is assigned a
priority level and a unique identifier for traceability.

**Priority Levels**:
- **P0** — Must have (launch blocker)
- **P1** — Should have (important but not blocking)
- **P2** — Nice to have (future enhancement)

---

## 2. User Management

| ID | Requirement | Priority |
|----|-------------|----------|
| UM-01 | Users can register with email, username, and password | P0 |
| UM-02 | Users can log in with email/username and password | P0 |
| UM-03 | System issues JWT access token upon successful login | P0 |
| UM-04 | System issues JWT refresh token for session extension | P1 |
| UM-05 | Users can log out (token invalidation) | P0 |
| UM-06 | Passwords are hashed using BCrypt before storage | P0 |
| UM-07 | Email must be unique across the system | P0 |
| UM-08 | Username must be unique across the system | P0 |
| UM-09 | Password must meet strength requirements (min 8 chars, 1 uppercase, 1 number, 1 special) | P1 |

---

## 3. URL Shortening

| ID | Requirement | Priority |
|----|-------------|----------|
| US-01 | Authenticated users can create a short URL from a long URL | P0 |
| US-02 | System generates a unique 7-character Base62 short code | P0 |
| US-03 | Users can optionally provide a custom alias (e.g., `scalelink.io/my-brand`) | P0 |
| US-04 | Custom aliases must be unique across the system | P0 |
| US-05 | Custom aliases must be 3-30 characters, alphanumeric and hyphens only | P1 |
| US-06 | Users can set an expiration date for a short URL | P0 |
| US-07 | Expired URLs return a 410 Gone response | P0 |
| US-08 | System validates the original URL format before accepting | P0 |
| US-09 | System rejects URLs pointing to blacklisted/malicious domains | P1 |
| US-10 | Each short URL is associated with the user who created it | P0 |

---

## 4. URL Redirection

| ID | Requirement | Priority |
|----|-------------|----------|
| UR-01 | Visiting a short URL redirects to the original URL via HTTP 302 | P0 |
| UR-02 | Redirect latency must be < 100ms (with caching) | P0 |
| UR-03 | Expired URLs do not redirect; return 410 Gone | P0 |
| UR-04 | Deleted/deactivated URLs return 404 Not Found | P0 |
| UR-05 | Each redirect records a click event asynchronously | P0 |

---

## 5. URL Management

| ID | Requirement | Priority |
|----|-------------|----------|
| MG-01 | Users can view all their created short URLs | P0 |
| MG-02 | Users can search their URLs by original URL or short code | P0 |
| MG-03 | Users can filter URLs by status (active, expired, all) | P1 |
| MG-04 | Users can edit the original URL of an existing short URL | P0 |
| MG-05 | Users can delete a short URL | P0 |
| MG-06 | Users can toggle a URL between active and inactive | P1 |
| MG-07 | URL list supports pagination (default 20 per page) | P0 |
| MG-08 | Users can sort URLs by creation date, click count, or expiration | P1 |

---

## 6. Analytics

| ID | Requirement | Priority |
|----|-------------|----------|
| AN-01 | System tracks total clicks per URL | P0 |
| AN-02 | System tracks unique clicks per URL (by IP) | P0 |
| AN-03 | System tracks daily click counts (time-series) | P0 |
| AN-04 | System records: IP address, user agent, referrer, device type, timestamp | P0 |
| AN-05 | Users can view analytics for any of their URLs | P0 |
| AN-06 | Dashboard shows: top 5 URLs by clicks, recent activity, total links created | P0 |
| AN-07 | Analytics data supports date range filtering | P1 |
| AN-08 | System aggregates analytics data for dashboard performance | P1 |

---

## 7. QR Code

| ID | Requirement | Priority |
|----|-------------|----------|
| QR-01 | System generates a QR code for each short URL | P1 |
| QR-02 | QR code is downloadable as PNG | P1 |
| QR-03 | QR code is displayed on the URL detail page | P1 |

---

## 8. User Dashboard

| ID | Requirement | Priority |
|----|-------------|----------|
| DB-01 | Dashboard shows total URLs created by user | P0 |
| DB-02 | Dashboard shows total clicks across all user URLs | P0 |
| DB-03 | Dashboard shows top 5 performing URLs | P0 |
| DB-04 | Dashboard shows recent activity (last 10 clicks) | P0 |
| DB-05 | Dashboard shows click trend chart (last 7 days) | P1 |

---

## 9. API Design

| ID | Requirement | Priority |
|----|-------------|----------|
| AP-01 | All APIs follow RESTful conventions | P0 |
| AP-02 | All APIs are versioned under `/api/v1/` | P0 |
| AP-03 | All APIs return consistent JSON response format | P0 |
| AP-04 | Protected endpoints require valid JWT in Authorization header | P0 |
| AP-05 | API documentation is available via Swagger UI | P1 |
| AP-06 | Redirect endpoint (`/{shortCode}`) is public (no auth required) | P0 |
