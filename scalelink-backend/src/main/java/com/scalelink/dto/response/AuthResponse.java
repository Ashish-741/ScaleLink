package com.scalelink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO — returned after successful login.
 * Contains the JWT token the frontend stores and sends with every request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;        // Token lifetime in seconds

    private UserSummary user;      // Basic user info

    /**
     * Minimal user info included in auth response.
     * We don't send the full User entity (it has passwordHash!).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
    }
}
