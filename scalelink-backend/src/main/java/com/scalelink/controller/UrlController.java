package com.scalelink.controller;

import com.scalelink.dto.request.UrlCreateRequest;
import com.scalelink.dto.response.ApiResponse;
import com.scalelink.dto.response.PageResponse;
import com.scalelink.dto.response.UrlResponse;
import com.scalelink.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * URL Management Controller
 * Handles creation, modification, and deletion of short URLs.
 * (Redirection is handled by a separate controller at the root path).
 */
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "URL Management", description = "Endpoints for creating and managing short URLs")
public class UrlController {

    private final UrlService urlService;

    /**
     * POST /api/v1/urls
     * Create a new short URL.
     * Requires JWT authentication.
     */
    @PostMapping
    @Operation(summary = "Create a short URL", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<UrlResponse>> createUrl(
            @Valid @RequestBody UrlCreateRequest request) {
            
        log.info("Received request to shorten URL: {}", request.getOriginalUrl());
        
        UrlResponse response = urlService.createShortUrl(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Short URL created successfully", response));
    }

    /**
     * GET /api/v1/urls
     * Fetch all URLs for the logged-in user.
     * Requires JWT authentication.
     */
    @GetMapping
    @Operation(summary = "Get user's URLs", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PageResponse<UrlResponse>>> getUserUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        log.info("Fetching URLs for current user (page: {}, size: {})", page, size);
        
        Page<UrlResponse> urlPage = urlService.getUserUrls(page, size);
        PageResponse<UrlResponse> pageResponse = PageResponse.from(urlPage);
        
        return ResponseEntity.ok(ApiResponse.success("URLs fetched successfully", pageResponse));
    }
}
