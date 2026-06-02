package com.scalelink.exception;

/**
 * Thrown when a user tries to create a custom alias that already exists.
 * HTTP Status: 409 Conflict
 */
public class DuplicateAliasException extends RuntimeException {
    public DuplicateAliasException(String alias) {
        super(String.format("Custom alias '%s' is already taken", alias));
    }
}
