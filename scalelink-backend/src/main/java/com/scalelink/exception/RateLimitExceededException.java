package com.scalelink.exception;

/**
 * Thrown when a user exceeds their rate limit.
 * HTTP Status: 429 Too Many Requests
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException() {
        super("Rate limit exceeded. Please try again later.");
    }
}
