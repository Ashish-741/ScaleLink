package com.scalelink.service;

import com.scalelink.entity.Url;
import com.scalelink.exception.ResourceNotFoundException;
import com.scalelink.exception.UrlExpiredException;
import com.scalelink.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Redirect Service
 * 
 * CORE SCALABILITY COMPONENT:
 * Resolves short codes to original URLs using a Cache-Aside pattern with Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectService {

    private final UrlRepository urlRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // We will inject the async click tracker
    private final ClickEventService clickEventService;

    @Value("${scalelink.cache.url-ttl-hours}")
    private long cacheTtlHours;

    private static final String CACHE_PREFIX = "url:";

    /**
     * Resolves a short code to the original URL.
     * 
     * CACHE-ASIDE PATTERN:
     * 1. Check Redis for the shortCode.
     * 2. If found (Cache Hit), return it immediately (sub-millisecond!).
     * 3. If not found (Cache Miss), query PostgreSQL.
     * 4. Save the result in Redis for next time.
     * 5. Fire off an async event to track the click.
     */
    public String getOriginalUrl(String shortCode, String ipAddress, String userAgent, String referer) {
        String cacheKey = CACHE_PREFIX + shortCode;
        
        // 1. Check Redis Cache
        String cachedUrl = (String) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUrl != null) {
            log.debug("Cache HIT for code: {}", shortCode);
            // Async track click based on cache hit. 
            // Note: We need the full URL entity for our analytics design, 
            // but fetching it defeats the purpose of the cache!
            // In a real high-scale system, we'd queue an event with just the shortCode
            // and resolve the ID later in a batch processor. 
            // For simplicity here, we'll fetch from DB async to record the click.
            fetchUrlAndRecordClickAsync(shortCode, ipAddress, userAgent, referer);
            return cachedUrl;
        }

        log.debug("Cache MISS for code: {}", shortCode);
        
        // 2. Query Database
        // We check BOTH shortCode and customAlias
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseGet(() -> urlRepository.findByCustomAlias(shortCode)
                        .orElseThrow(() -> new ResourceNotFoundException("URL", "shortCode", shortCode)));

        // 3. Check business rules (expiration, active status)
        if (!url.getIsActive()) {
            throw new ResourceNotFoundException("URL is disabled", "shortCode", shortCode);
        }
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("URL has expired");
        }

        String originalUrl = url.getOriginalUrl();

        // 4. Update Redis Cache
        redisTemplate.opsForValue().set(cacheKey, originalUrl, cacheTtlHours, TimeUnit.HOURS);

        // 5. Track Click
        clickEventService.recordClick(url, ipAddress, userAgent, referer);

        return originalUrl;
    }
    
    private void fetchUrlAndRecordClickAsync(String code, String ip, String ua, String ref) {
        // Run in a background thread to avoid blocking the fast cache-hit path
        new Thread(() -> {
            urlRepository.findByShortCode(code)
                .or(() -> urlRepository.findByCustomAlias(code))
                .ifPresent(u -> clickEventService.recordClick(u, ip, ua, ref));
        }).start();
    }
}
