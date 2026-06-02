package com.scalelink.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Request Logging Filter
 *
 * WHAT IS A FILTER?
 * A filter is like a checkpoint that every HTTP request must pass through
 * BEFORE reaching your controller. Filters are used for cross-cutting
 * concerns — things that apply to ALL requests regardless of endpoint.
 *
 * FILTER CHAIN ORDER (how a request flows):
 * Client → Tomcat → RequestLoggingFilter → CORS Filter → Rate Limiter
 *    → JWT Filter → Controller → Service → Repository → Database
 *
 * WHAT IS OncePerRequestFilter?
 * A Spring base class that guarantees the filter runs exactly ONCE per
 * request. Without this, filters might run multiple times if there are
 * internal forwards or redirects.
 *
 * WHAT THIS FILTER DOES:
 * 1. Assigns a unique correlation ID to each request (for tracing)
 * 2. Logs the incoming request (method, URL, IP)
 * 3. Lets the request proceed through the chain
 * 4. Logs the response (status code, duration)
 *
 * WHAT IS A CORRELATION ID?
 * A unique ID assigned to each request so you can trace it through logs.
 * If a user reports "something broke at 3:45 PM", you search your logs
 * for their correlation ID and see exactly what happened.
 *
 * @Order(1) means this filter runs FIRST in the chain.
 */
@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Generate a unique correlation ID for this request
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Record start time for duration calculation
        long startTime = System.currentTimeMillis();

        // Extract request details
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);

        // Build the full URL (with query string if present)
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        // Log the incoming request
        log.info("[{}] → {} {} from {}", correlationId, method, fullUrl, clientIp);

        // Add correlation ID to the response header (so frontend can report it)
        response.setHeader("X-Correlation-ID", correlationId);

        try {
            // Let the request continue through the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Calculate how long the request took
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Log the response
            log.info("[{}] ← {} {} → {} ({}ms)", correlationId, method, fullUrl, status, duration);
        }
    }

    /**
     * Extract the real client IP address.
     *
     * WHY NOT JUST USE request.getRemoteAddr()?
     * If the request goes through a proxy or load balancer (like Nginx),
     * getRemoteAddr() returns the proxy's IP, not the client's.
     * The real client IP is in the X-Forwarded-For header.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            // The first one is the real client IP
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Skip logging for certain paths that are called frequently
     * and would clutter the logs (health checks, static resources).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger")
                || path.startsWith("/api-docs")
                || path.endsWith(".html")
                || path.endsWith(".css")
                || path.endsWith(".js");
    }
}
