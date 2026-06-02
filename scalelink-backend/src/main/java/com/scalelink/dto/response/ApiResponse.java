package com.scalelink.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Universal API Response Envelope
 *
 * WHAT IS THIS?
 * Every single API endpoint in ScaleLink returns this SAME response structure.
 * This is called a "response envelope" — it wraps the actual data in a
 * consistent format so the frontend always knows what to expect.
 *
 * WHY?
 * - Frontend can always check response.success to know if the request worked
 * - Error messages are always in the same place
 * - Validation errors are always in the same format
 * - No surprises — consistency builds trust
 *
 * EXAMPLE SUCCESS:
 * {
 *   "success": true,
 *   "message": "URL created successfully",
 *   "data": { ... },
 *   "timestamp": "2026-06-02T12:00:00"
 * }
 *
 * EXAMPLE ERROR:
 * {
 *   "success": false,
 *   "message": "Validation failed",
 *   "errors": [{"field": "email", "message": "Invalid email"}],
 *   "timestamp": "2026-06-02T12:00:00"
 * }
 *
 * WHAT IS @Data?
 * Lombok annotation that auto-generates:
 * - All getters and setters
 * - toString() method
 * - equals() and hashCode() methods
 * Without Lombok, you'd write 50+ lines of boilerplate code.
 *
 * WHAT IS @Builder?
 * Lombok annotation that generates a Builder pattern.
 * Instead of: new ApiResponse(true, "message", data, null, timestamp)
 * You write:  ApiResponse.builder().success(true).message("msg").data(data).build()
 * Much more readable, especially with many fields.
 *
 * WHAT IS @JsonInclude(NON_NULL)?
 * Tells Jackson (JSON library) to skip null fields in the JSON output.
 * So if "errors" is null, it won't appear in the response at all.
 * Keeps responses clean.
 *
 * WHAT IS THE <T> (Generic Type)?
 * T is a placeholder for any type. ApiResponse<UrlResponse> means
 * the "data" field will contain a UrlResponse object.
 * ApiResponse<List<UrlResponse>> means data is a list of UrlResponses.
 * This gives us type safety — the compiler checks types for us.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<FieldError> errors;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ============================================
    // Static Factory Methods
    // ============================================
    // These are convenience methods for creating responses.
    // Instead of using the builder every time, call:
    //   ApiResponse.success("Created!", urlResponse)
    //   ApiResponse.error("Not found")
    // ============================================

    /**
     * Create a success response with data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response without data.
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with a message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with field-level validation errors.
     */
    public static <T> ApiResponse<T> error(String message, List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Represents a single field validation error.
     * Example: { "field": "email", "message": "Email is already registered" }
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
