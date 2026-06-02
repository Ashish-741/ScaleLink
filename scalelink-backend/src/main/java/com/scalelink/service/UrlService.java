package com.scalelink.service;

import com.scalelink.dto.request.UrlCreateRequest;
import com.scalelink.dto.response.UrlResponse;
import com.scalelink.entity.Url;
import com.scalelink.entity.User;
import com.scalelink.exception.DuplicateAliasException;
import com.scalelink.exception.ResourceNotFoundException;
import com.scalelink.repository.UrlRepository;
import com.scalelink.repository.UserRepository;
import com.scalelink.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * URL Service — Business logic for creating and managing short URLs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    @Value("${scalelink.url.base-url}")
    private String baseUrl;

    @Value("${scalelink.url.code-length}")
    private int codeLength;

    @Value("${scalelink.url.default-expiration-days}")
    private int defaultExpirationDays;

    /**
     * Create a new short URL.
     *
     * FLOW:
     * 1. Get the currently authenticated user from SecurityContext.
     * 2. If a custom alias is provided, check if it's taken.
     * 3. If no custom alias, generate a random Base62 short code.
     *    (Loop to ensure no collisions in the database).
     * 4. Set expiration (user provided or default).
     * 5. Save to database.
     * 6. Return response DTO.
     */
    @Transactional
    public UrlResponse createShortUrl(UrlCreateRequest request) {
        log.info("Creating short URL for: {}", request.getOriginalUrl());

        // 1. Get current user
        // We set this in JwtAuthenticationFilter
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String shortCode;
        String customAlias = request.getCustomAlias();

        // 2 & 3. Handle Alias or generate Short Code
        if (customAlias != null && !customAlias.trim().isEmpty()) {
            if (urlRepository.existsByCustomAlias(customAlias)) {
                throw new DuplicateAliasException(customAlias);
            }
            // For custom aliases, the shortCode field can be anything (or the alias itself)
            // But we keep shortCode separate so we can still have a random code just in case
            shortCode = generateUniqueShortCode();
        } else {
            customAlias = null; // Ensure empty strings become null
            shortCode = generateUniqueShortCode();
        }

        // 4. Set Expiration
        LocalDateTime expiresAt = request.getExpiresAt();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(defaultExpirationDays);
        }

        // 5. Save Entity
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .customAlias(customAlias)
                .user(currentUser)
                .expiresAt(expiresAt)
                .isActive(true)
                .build();

        Url savedUrl = urlRepository.save(url);
        log.info("Successfully created short URL ID: {} with code: {}", savedUrl.getId(), shortCode);

        // 6. Return Response
        return UrlResponse.fromEntity(savedUrl, baseUrl);
    }

    /**
     * Loop until we find a Base62 code that isn't in the database.
     * At 7 characters, collisions are mathematically VERY rare,
     * but we must handle them defensively.
     */
    private String generateUniqueShortCode() {
        String code;
        do {
            code = Base62Encoder.generateRandom(codeLength);
        } while (urlRepository.existsByShortCode(code));
        return code;
    }
}
