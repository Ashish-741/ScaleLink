package com.scalelink.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider — Creates and validates JWT tokens.
 *
 * WHAT IS JWT (JSON Web Token)?
 * JWT is a compact, URL-safe token that carries claims (data) between
 * the client and server. It consists of three parts separated by dots:
 *
 *   header.payload.signature
 *   eyJhbGci.eyJzdWIi.SflKxwRJ
 *
 * 1. HEADER: Algorithm used (HS256) and token type (JWT)
 *    {"alg": "HS256", "typ": "JWT"}
 *
 * 2. PAYLOAD (Claims): The actual data
 *    {"sub": "1", "email": "ashish@example.com", "iat": 1717329600, "exp": 1717333200}
 *    - sub (subject): user ID
 *    - iat (issued at): when the token was created
 *    - exp (expiration): when the token expires
 *
 * 3. SIGNATURE: Proves the token hasn't been tampered with
 *    HMACSHA256(base64(header) + "." + base64(payload), SECRET_KEY)
 *
 * HOW JWT AUTH WORKS:
 * 1. User logs in → server creates a JWT token with user ID inside
 * 2. Server sends the token to the client
 * 3. Client stores the token (usually in localStorage)
 * 4. Client sends the token in every request: Authorization: Bearer <token>
 * 5. Server validates the token and extracts the user ID
 * 6. No database lookup needed to verify the session!
 *
 * WHY JWT INSTEAD OF SESSIONS?
 * - STATELESS: Server doesn't store anything (no session table)
 * - SCALABLE: Any server instance can validate the token
 * - SELF-CONTAINED: Token carries the user info inside it
 *
 * WHAT IS @Value?
 * Injects a value from application.yml into this field.
 * ${scalelink.jwt.secret} reads from our YAML config.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${scalelink.jwt.secret}") String secret,
            @Value("${scalelink.jwt.expiration}") long expirationMs) {
        // Create a cryptographic key from our secret string
        // The key must be at least 256 bits for HS256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a JWT token for a user.
     *
     * @param userId The user's database ID
     * @param email  The user's email
     * @return A signed JWT token string
     */
    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))         // sub: user ID
                .claim("email", email)                    // custom claim: email
                .issuedAt(now)                            // iat: issued at
                .expiration(expiryDate)                   // exp: expires at
                .signWith(secretKey)                      // sign with our secret key
                .compact();                               // build the token string
    }

    /**
     * Extract the user ID from a token.
     * The "subject" claim contains the user ID as a string.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract the email from a token.
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Validate a token — check signature, expiration, and format.
     *
     * WHAT CAN GO WRONG?
     * 1. Token is expired (user logged in hours ago)
     * 2. Token signature is invalid (someone tampered with it)
     * 3. Token is malformed (not a valid JWT string)
     * 4. Token is unsupported (different algorithm than expected)
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature validation failed: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Parse and verify a JWT token.
     * If the signature doesn't match or the token is expired,
     * this throws an exception.
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)        // verify the signature
                .build()
                .parseSignedClaims(token)     // parse the token
                .getPayload();                // get the claims (payload)
    }

    /**
     * Get token expiration time in seconds (for API response).
     */
    public long getExpirationInSeconds() {
        return expirationMs / 1000;
    }
}
