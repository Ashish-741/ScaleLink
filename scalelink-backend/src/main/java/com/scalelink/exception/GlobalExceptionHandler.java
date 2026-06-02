package com.scalelink.exception;

import com.scalelink.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 *
 * WHAT IS @RestControllerAdvice?
 * It's a special class that intercepts ALL exceptions thrown by ANY controller.
 * Instead of handling errors in every controller method, we handle them HERE
 * in one centralized place.
 *
 * HOW IT WORKS:
 * 1. Controller throws an exception (e.g., ResourceNotFoundException)
 * 2. Spring looks for an @ExceptionHandler that matches the exception type
 * 3. The matching handler method converts it to a proper HTTP response
 * 4. The response is sent to the client
 *
 * WHY IS THIS IMPORTANT?
 * - CONSISTENT error responses across ALL endpoints
 * - CLEAN controller code (no try-catch blocks everywhere)
 * - ONE place to log all errors
 * - ONE place to change error format
 *
 * WHAT IS @Slf4j?
 * Lombok annotation that creates a logger. Instead of:
 *   private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
 * You just use: log.error("message")
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle 404 Not Found — resource doesn't exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle 409 Conflict — duplicate alias.
     */
    @ExceptionHandler(DuplicateAliasException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateAlias(DuplicateAliasException ex) {
        log.warn("Duplicate alias: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle 410 Gone — URL has expired.
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleUrlExpired(UrlExpiredException ex) {
        log.info("Expired URL accessed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle 429 Too Many Requests — rate limit exceeded.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimit(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle 400 Bad Request — validation errors.
     *
     * This catches @Valid annotation failures on request DTOs.
     * For example, if RegisterRequest has @NotBlank on email and the user
     * sends an empty email, this handler catches it and returns a nice
     * error response with the specific field that failed.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();

        // Convert Spring's FieldError to our ApiResponse.FieldError
        List<ApiResponse.FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(error -> new ApiResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", fieldErrors));
    }

    /**
     * Handle 401 Unauthorized — bad credentials during login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
    }

    /**
     * Handle 403 Forbidden — user doesn't have permission.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You don't have permission to access this resource"));
    }

    /**
     * Handle 400 Bad Request — general illegal arguments.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * CATCH-ALL: Handle 500 Internal Server Error.
     *
     * This is the safety net. If any unexpected exception escapes
     * (NullPointerException, database errors, etc.), this catches it
     * and returns a generic error instead of a stack trace.
     *
     * SECURITY: Never expose stack traces to the client in production.
     * They reveal internal implementation details that attackers can exploit.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
