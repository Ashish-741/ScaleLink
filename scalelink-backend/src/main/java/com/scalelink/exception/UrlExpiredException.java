package com.scalelink.exception;

/**
 * Thrown when a short URL has expired.
 * HTTP Status: 410 Gone
 *
 * WHY 410 AND NOT 404?
 * - 404 means "never existed" or "we don't know about it"
 * - 410 means "this used to exist but is now gone"
 * This distinction matters for SEO and client behavior.
 */
public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String shortCode) {
        super(String.format("URL with code '%s' has expired", shortCode));
    }
}
