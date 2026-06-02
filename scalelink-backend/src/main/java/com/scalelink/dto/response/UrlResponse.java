package com.scalelink.dto.response;

import com.scalelink.entity.Url;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * URL Response DTO
 * Sent back to the client after creating or fetching a URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    private Long id;
    private String shortCode;
    private String shortUrl;      // The full clickable URL (e.g., http://localhost:8080/aB3x7Kp)
    private String originalUrl;
    private String customAlias;
    private LocalDateTime expiresAt;
    private Long clickCount;
    private LocalDateTime createdAt;

    /**
     * Utility method to map a Url entity to a UrlResponse DTO.
     * This keeps conversion logic out of the controller and service.
     */
    public static UrlResponse fromEntity(Url url, String baseUrl) {
        // Determine the path segment (either the custom alias or the random code)
        String pathSegment = url.getCustomAlias() != null ? url.getCustomAlias() : url.getShortCode();
        
        // Build the full short URL
        String fullShortUrl = baseUrl + "/" + pathSegment;

        return UrlResponse.builder()
                .id(url.getId())
                .shortCode(url.getShortCode())
                .shortUrl(fullShortUrl)
                .originalUrl(url.getOriginalUrl())
                .customAlias(url.getCustomAlias())
                .expiresAt(url.getExpiresAt())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .build();
    }
}
