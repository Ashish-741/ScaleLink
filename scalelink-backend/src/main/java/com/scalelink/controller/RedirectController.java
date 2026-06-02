package com.scalelink.controller;

import com.scalelink.dto.response.ApiResponse;
import com.scalelink.exception.UrlExpiredException;
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

    // Service will be implemented in the next phase (Redirection & Caching)
    
    /**
     * GET /{shortCode}
     *
     * Example: http://localhost:8080/aB3x7Kp
     * We don't use /api/v1/ here because we want short URLs to be as short as possible!
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        log.info("Received redirect request for code: {}", shortCode);
        
        // FOR NOW: Just return a message. 
        // In Phase 7, we will change this to return a 302 Redirect with Redis caching!
        return ResponseEntity.ok(ApiResponse.success(
            "Phase 6 complete! In Phase 7, this will redirect to the original URL."
        ));
    }
}
