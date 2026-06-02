package com.scalelink.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalelink.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limit Interceptor
 * 
 * Uses Redis to implement a simple Fixed Window rate limiting algorithm.
 * This intercepts incoming requests before they reach the controller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${scalelink.rate-limit.url-creation-per-minute}")
    private int maxRequestsPerMinute;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:create_url:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only apply rate limiting to POST requests (creating URLs)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = request.getRemoteAddr();
        String key = RATE_LIMIT_PREFIX + clientIp;

        // Redis increment operation is atomic
        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests != null) {
            if (currentRequests == 1) {
                // First request in this window, set expiration to 1 minute
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }

            if (currentRequests > maxRequestsPerMinute) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                
                // Return 429 Too Many Requests
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                
                ApiResponse<?> apiResponse = ApiResponse.error("Rate limit exceeded. Try again in a minute.");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                
                return false; // Stop processing the request
            }
        }

        return true; // Allow the request to proceed
    }
}
