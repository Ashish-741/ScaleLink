# ScaleLink — API Contract (v1)

> **Document Version**: 1.0
> **Base URL**: `http://localhost:8080/api/v1`
> **Auth**: JWT Bearer Token (unless marked as Public)
> **Content-Type**: `application/json`

---

## 1. API Design Philosophy

### Why API-First Development?

In real companies, the API contract is designed BEFORE any code is written.
This allows:
- **Frontend and backend teams to work in parallel**
- **Clear documentation for external consumers**
- **Contract testing to prevent breaking changes**
- **Versioning from day one** (`/api/v1/`) so you can evolve without breaking clients

### Response Envelope

Every API response follows a consistent envelope:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

Error response:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is already registered"
    }
  ],
  "timestamp": "2026-06-02T12:00:00Z"
}
```

---

## 2. Authentication APIs

### POST `/api/v1/auth/register` — Register a New User

**Access**: Public

**Request Body**:
```json
{
  "username": "ashish_dev",
  "email": "ashish@example.com",
  "password": "SecureP@ss123"
}
```

**Success Response** `201 Created`:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "ashish_dev",
    "email": "ashish@example.com",
    "createdAt": "2026-06-02T12:00:00Z"
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Validation error (weak password, invalid email) |
| 409 | Email or username already exists |

---

### POST `/api/v1/auth/login` — Login

**Access**: Public

**Request Body**:
```json
{
  "email": "ashish@example.com",
  "password": "SecureP@ss123"
}
```

**Success Response** `200 OK`:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "ashish_dev",
      "email": "ashish@example.com"
    }
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 401 | Invalid credentials |

---

## 3. URL APIs

### POST `/api/v1/urls` — Create Short URL

**Access**: Authenticated

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "originalUrl": "https://www.example.com/very/long/path/to/resource?param=value",
  "customAlias": "my-link",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

> `customAlias` and `expiresAt` are optional.

**Success Response** `201 Created`:
```json
{
  "success": true,
  "message": "Short URL created successfully",
  "data": {
    "id": 42,
    "shortCode": "aB3x7Kp",
    "shortUrl": "http://localhost:8080/aB3x7Kp",
    "originalUrl": "https://www.example.com/very/long/path/to/resource?param=value",
    "customAlias": "my-link",
    "expiresAt": "2026-12-31T23:59:59Z",
    "clickCount": 0,
    "isActive": true,
    "createdAt": "2026-06-02T12:00:00Z"
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Invalid URL format |
| 409 | Custom alias already taken |
| 429 | Rate limit exceeded |

---

### GET `/api/v1/urls` — List User's URLs

**Access**: Authenticated

**Query Parameters**:
| Param | Type | Default | Description |
|---|---|---|---|
| page | int | 0 | Page number (0-indexed) |
| size | int | 20 | Items per page |
| search | string | — | Search by original URL or short code |
| status | string | all | Filter: `active`, `expired`, `all` |
| sortBy | string | createdAt | Sort field: `createdAt`, `clickCount`, `expiresAt` |
| sortDir | string | desc | Sort direction: `asc`, `desc` |

**Success Response** `200 OK`:
```json
{
  "success": true,
  "message": "URLs retrieved successfully",
  "data": {
    "content": [
      {
        "id": 42,
        "shortCode": "aB3x7Kp",
        "shortUrl": "http://localhost:8080/aB3x7Kp",
        "originalUrl": "https://www.example.com/...",
        "customAlias": "my-link",
        "expiresAt": "2026-12-31T23:59:59Z",
        "clickCount": 1523,
        "isActive": true,
        "createdAt": "2026-06-02T12:00:00Z"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 45,
    "totalPages": 3,
    "isLast": false
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

---

### GET `/api/v1/urls/{shortCode}` — Get URL Details

**Access**: Authenticated (owner only)

**Success Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "id": 42,
    "shortCode": "aB3x7Kp",
    "shortUrl": "http://localhost:8080/aB3x7Kp",
    "originalUrl": "https://www.example.com/...",
    "customAlias": "my-link",
    "expiresAt": "2026-12-31T23:59:59Z",
    "clickCount": 1523,
    "isActive": true,
    "createdAt": "2026-06-02T12:00:00Z",
    "updatedAt": "2026-06-02T14:00:00Z"
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 404 | URL not found |
| 403 | Not the owner |

---

### PUT `/api/v1/urls/{shortCode}` — Update URL

**Access**: Authenticated (owner only)

**Request Body**:
```json
{
  "originalUrl": "https://www.new-destination.com/page",
  "expiresAt": "2027-06-01T00:00:00Z",
  "isActive": true
}
```

**Success Response** `200 OK`: Returns updated URL object.

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Invalid URL format |
| 404 | URL not found |
| 403 | Not the owner |

---

### DELETE `/api/v1/urls/{shortCode}` — Delete URL

**Access**: Authenticated (owner only)

**Success Response** `200 OK`:
```json
{
  "success": true,
  "message": "URL deleted successfully",
  "data": null,
  "timestamp": "2026-06-02T12:00:00Z"
}
```

---

## 4. Redirect API

### GET `/{shortCode}` — Redirect to Original URL

**Access**: Public (NOT under `/api/v1/`)

**Success Response** `302 Found`:
```
HTTP/1.1 302 Found
Location: https://www.example.com/very/long/path/to/resource
```

**Error Responses**:
| Status | Reason |
|---|---|
| 404 | Short code not found |
| 410 | URL has expired |

### Why 302 and Not 301?

- **301 (Permanent Redirect)**: Browser caches the redirect forever. We CANNOT
  track clicks because the browser never hits our server again.
- **302 (Temporary Redirect)**: Browser asks our server every time. This allows
  us to track every click and change the destination URL later.

> This is a common interview question. Always use 302 for URL shorteners.

---

## 5. Analytics APIs

### GET `/api/v1/urls/{shortCode}/analytics` — URL Analytics

**Access**: Authenticated (owner only)

**Query Parameters**:
| Param | Type | Default | Description |
|---|---|---|---|
| startDate | date | 7 days ago | Start of date range |
| endDate | date | today | End of date range |

**Success Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "shortCode": "aB3x7Kp",
    "totalClicks": 1523,
    "uniqueClicks": 892,
    "dailyClicks": [
      { "date": "2026-06-01", "clicks": 245, "uniqueClicks": 180 },
      { "date": "2026-06-02", "clicks": 312, "uniqueClicks": 210 }
    ],
    "topReferrers": [
      { "referrer": "twitter.com", "count": 456 },
      { "referrer": "linkedin.com", "count": 234 }
    ],
    "deviceBreakdown": [
      { "device": "mobile", "count": 890 },
      { "device": "desktop", "count": 520 },
      { "device": "tablet", "count": 113 }
    ]
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

---

### GET `/api/v1/analytics/dashboard` — User Dashboard Analytics

**Access**: Authenticated

**Success Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "totalUrls": 45,
    "totalClicks": 15230,
    "totalUniqueClicks": 8920,
    "topUrls": [
      {
        "shortCode": "aB3x7Kp",
        "originalUrl": "https://...",
        "clickCount": 1523
      }
    ],
    "recentActivity": [
      {
        "shortCode": "aB3x7Kp",
        "clickedAt": "2026-06-02T11:55:00Z",
        "deviceType": "mobile",
        "country": "IN"
      }
    ],
    "clicksTrend": [
      { "date": "2026-05-27", "clicks": 1200 },
      { "date": "2026-05-28", "clicks": 1450 }
    ]
  },
  "timestamp": "2026-06-02T12:00:00Z"
}
```

---

## 6. QR Code API

### GET `/api/v1/urls/{shortCode}/qr` — Generate QR Code

**Access**: Authenticated (owner only)

**Query Parameters**:
| Param | Type | Default | Description |
|---|---|---|---|
| size | int | 250 | QR code size in pixels |

**Success Response** `200 OK`:
```
Content-Type: image/png
Body: <PNG binary data>
```

---

## 7. Health & Metrics APIs

### GET `/actuator/health` — Health Check

**Access**: Public

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## 8. HTTP Status Code Summary

| Code | Usage |
|---|---|
| 200 | Successful GET, PUT, DELETE |
| 201 | Successful POST (resource created) |
| 302 | URL redirect |
| 400 | Validation error / bad request |
| 401 | Not authenticated |
| 403 | Not authorized (wrong owner) |
| 404 | Resource not found |
| 409 | Conflict (duplicate alias, email) |
| 410 | URL expired |
| 429 | Rate limit exceeded |
| 500 | Internal server error |

---

## 9. Rate Limit Headers

All rate-limited endpoints include these response headers:

```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-RateLimit-Reset: 1717329600
```
