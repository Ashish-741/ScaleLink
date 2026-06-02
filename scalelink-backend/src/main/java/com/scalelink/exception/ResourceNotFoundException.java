package com.scalelink.exception;

/**
 * Thrown when a requested resource (URL, User, etc.) is not found.
 *
 * HTTP Status: 404 Not Found
 *
 * WHY A CUSTOM EXCEPTION?
 * Instead of returning error responses manually in every service method,
 * we THROW exceptions and let the GlobalExceptionHandler convert them
 * to proper HTTP responses. This keeps service code clean:
 *
 * BAD (mixing business logic with HTTP concerns):
 *   if (url == null) {
 *       return ResponseEntity.status(404).body(new ApiResponse(...));
 *   }
 *
 * GOOD (clean separation):
 *   if (url == null) {
 *       throw new ResourceNotFoundException("URL", "shortCode", shortCode);
 *   }
 *   // GlobalExceptionHandler converts this to a 404 response automatically
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}
