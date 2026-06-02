# ScaleLink — User Stories

> **Document Version**: 1.0
> **Format**: As a [role], I want [feature], so that [benefit]
> **Last Updated**: 2026-06-02

---

## Why User Stories?

In agile software companies, requirements are written as user stories because:
1. They keep the focus on **user value**, not technical implementation
2. They have clear **acceptance criteria** — you know when you're done
3. They can be **estimated** and **prioritized** for sprint planning
4. They are **testable** — each story maps to test cases

---

## Authentication Stories

### US-001: User Registration
**As a** new user,
**I want to** register with my email, username, and password,
**so that** I can create and manage short URLs.

**Acceptance Criteria**:
- [ ] Registration form accepts email, username, and password
- [ ] Email must be valid format and unique
- [ ] Username must be 3-20 characters, alphanumeric, and unique
- [ ] Password must be min 8 characters with 1 uppercase, 1 number, 1 special char
- [ ] Password is hashed before storage
- [ ] Success returns 201 with user details (no password)
- [ ] Duplicate email/username returns 409

---

### US-002: User Login
**As a** registered user,
**I want to** log in with my email and password,
**so that** I can access my dashboard and manage my URLs.

**Acceptance Criteria**:
- [ ] Login accepts email and password
- [ ] Valid credentials return JWT access token
- [ ] Token contains user ID and email
- [ ] Token expires after 1 hour
- [ ] Invalid credentials return 401
- [ ] Response includes token expiration time

---

### US-003: Authenticated Access
**As a** logged-in user,
**I want** my requests to be authenticated via JWT,
**so that** my data is secure and only I can manage my URLs.

**Acceptance Criteria**:
- [ ] Protected endpoints require `Authorization: Bearer <token>` header
- [ ] Missing/invalid token returns 401
- [ ] Expired token returns 401 with "Token expired" message
- [ ] Token is validated on every request

---

## URL Management Stories

### US-004: Create Short URL
**As a** logged-in user,
**I want to** submit a long URL and receive a short URL,
**so that** I can share a shorter, cleaner link.

**Acceptance Criteria**:
- [ ] Accepts a valid URL and returns a 7-character short code
- [ ] Short URL is immediately functional for redirecting
- [ ] Invalid URL format returns 400
- [ ] Short code is unique across the system
- [ ] URL is associated with the authenticated user

---

### US-005: Custom Alias
**As a** logged-in user,
**I want to** create a short URL with a custom alias (e.g., `my-brand`),
**so that** my link is memorable and branded.

**Acceptance Criteria**:
- [ ] Accepts optional `customAlias` field
- [ ] Alias must be 3-30 characters, alphanumeric and hyphens
- [ ] Duplicate alias returns 409
- [ ] Both `/shortCode` and `/customAlias` redirect to the same URL

---

### US-006: URL Expiration
**As a** logged-in user,
**I want to** set an expiration date on my short URL,
**so that** the link automatically stops working after a certain time.

**Acceptance Criteria**:
- [ ] Accepts optional `expiresAt` field (ISO 8601 datetime)
- [ ] Expiration date must be in the future
- [ ] Expired URLs return 410 Gone on redirect
- [ ] Expired URLs are marked in the dashboard

---

### US-007: View My URLs
**As a** logged-in user,
**I want to** see a list of all short URLs I've created,
**so that** I can manage and track my links.

**Acceptance Criteria**:
- [ ] Returns paginated list of user's URLs
- [ ] Each URL shows: short code, original URL, click count, status, created date
- [ ] Default page size is 20
- [ ] Supports sorting by creation date, click count

---

### US-008: Search and Filter URLs
**As a** logged-in user,
**I want to** search and filter my URLs,
**so that** I can quickly find a specific link.

**Acceptance Criteria**:
- [ ] Search by original URL (partial match)
- [ ] Search by short code
- [ ] Filter by status: active, expired, all
- [ ] Results are paginated

---

### US-009: Edit URL
**As a** logged-in user,
**I want to** update the destination of an existing short URL,
**so that** I can fix mistakes or change where the link points.

**Acceptance Criteria**:
- [ ] Can update: original URL, expiration date, active status
- [ ] Cannot update: short code (immutable)
- [ ] Only the owner can edit their URL
- [ ] Non-owner gets 403
- [ ] Cache is invalidated after update

---

### US-010: Delete URL
**As a** logged-in user,
**I want to** delete a short URL I no longer need,
**so that** it stops working and is removed from my dashboard.

**Acceptance Criteria**:
- [ ] Only the owner can delete
- [ ] Deleted URL returns 404 on redirect
- [ ] URL is removed from cache
- [ ] Associated analytics data is retained (soft delete)

---

## Redirect Stories

### US-011: URL Redirect
**As a** visitor (anonymous),
**I want to** visit a short URL and be redirected to the original,
**so that** I reach the intended destination.

**Acceptance Criteria**:
- [ ] GET `/{shortCode}` returns 302 redirect
- [ ] Redirect latency < 100ms (with cache)
- [ ] Click event is recorded asynchronously
- [ ] No authentication required

---

## Analytics Stories

### US-012: View URL Analytics
**As a** logged-in user,
**I want to** see analytics for a specific URL,
**so that** I can understand how my link is performing.

**Acceptance Criteria**:
- [ ] Shows total clicks and unique clicks
- [ ] Shows daily click breakdown
- [ ] Shows top referrers
- [ ] Shows device type breakdown
- [ ] Supports date range filtering

---

### US-013: Dashboard Overview
**As a** logged-in user,
**I want to** see an overview dashboard,
**so that** I can quickly gauge the performance of all my links.

**Acceptance Criteria**:
- [ ] Shows total URLs created
- [ ] Shows total clicks across all URLs
- [ ] Shows top 5 URLs by click count
- [ ] Shows recent activity (last 10 clicks)
- [ ] Shows click trend for last 7 days

---

## QR Code Stories

### US-014: QR Code Generation
**As a** logged-in user,
**I want to** generate a QR code for my short URL,
**so that** I can share it in print or offline media.

**Acceptance Criteria**:
- [ ] QR code is generated for each URL
- [ ] QR code is downloadable as PNG
- [ ] QR code encodes the full short URL
- [ ] QR code is displayed on the URL detail page
