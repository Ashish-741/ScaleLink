package com.scalelink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * URL Creation Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlCreateRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "Please provide a valid URL (must include http:// or https://)")
    private String originalUrl;

    /**
     * Optional custom alias (e.g., "my-portfolio").
     * If null, we generate a random Base62 string.
     */
    @Size(min = 4, max = 30, message = "Custom alias must be between 4 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", 
             message = "Custom alias can only contain letters, numbers, and hyphens")
    private String customAlias;

    /**
     * Optional expiration date. 
     * If null, the system applies the default (e.g., 1 year).
     */
    private LocalDateTime expiresAt;
}
