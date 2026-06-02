package com.scalelink.controller;

import com.scalelink.dto.response.ApiResponse;
import com.scalelink.exception.UrlExpiredException;
import com.scalelink.service.RedirectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redirect Controller — The most important endpoint!
 *
 * This controller listens at the ROOT path (/) and handles the actual
 * redirection when a user clicks a short link (e.g., /aB3x7Kp).
 *
 * This MUST be public (no JWT required).
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Redirect", description = "Handles redirection of short URLs")
public class RedirectController {

    private final RedirectService redirectService;
    
    /**
     * GET /{shortCode}
     *
     * Example: http://localhost:8080/aB3x7Kp
     * Returns an HTTP 302 Found status with a Location header pointing to the original URL.
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            jakarta.servlet.http.HttpServletRequest request) {
            
        log.info("Received redirect request for code: {}", shortCode);
        
        // Extract analytics info
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        
        // Resolve URL (hits Redis cache first!)
        String originalUrl = redirectService.getOriginalUrl(shortCode, ipAddress, userAgent, referer);
        
        // Issue HTTP 302 Redirect
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create(originalUrl))
                .build();
    }
}
