package com.scalelink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 *
 * WHAT IS CORS?
 * When your React frontend (running on http://localhost:3000) makes an
 * API call to your Spring Boot backend (running on http://localhost:8080),
 * the browser blocks it by default. Why? Because they're on different
 * "origins" (different ports count as different origins).
 *
 * This is a SECURITY feature called the Same-Origin Policy. It prevents
 * malicious websites from making requests to your API using your cookies.
 *
 * CORS is the mechanism that tells the browser: "It's OK, I trust
 * requests from http://localhost:3000."
 *
 * HOW IT WORKS:
 * 1. Browser sends a "preflight" OPTIONS request to the backend
 * 2. Backend responds with CORS headers (allowed origins, methods, etc.)
 * 3. If the origin is allowed, browser sends the actual request
 * 4. If not allowed, browser blocks the request (you see a CORS error)
 *
 * WHAT IS @Configuration?
 * Tells Spring: "This class contains @Bean methods that create objects
 * I should manage." It's like a factory class.
 *
 * WHAT IS @Bean?
 * A method annotated with @Bean returns an object that Spring manages.
 * Spring calls this method once, stores the result, and injects it
 * wherever it's needed. Think of it as: "Hey Spring, here's how to
 * create this object."
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Which origins (domains) can make requests to our API
        // In production, this should be ONLY your frontend domain
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",          // React dev server
                "http://localhost:5173",          // Vite dev server
                "https://scalelink.onrender.com"  // Production frontend
        ));

        // Which HTTP methods are allowed
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Which headers the client can send
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",      // JWT token
                "Content-Type",       // JSON content type
                "X-Requested-With",   // AJAX requests
                "Accept",             // Accept header
                "Origin"              // Origin header
        ));

        // Which headers the client can READ from the response
        // We expose rate limit headers so the frontend can display them
        configuration.setExposedHeaders(Arrays.asList(
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
        ));

        // Allow cookies/authorization headers to be sent
        configuration.setAllowCredentials(true);

        // How long the browser can cache the preflight response (1 hour)
        // This means the browser won't send a preflight OPTIONS request
        // for every API call — only once per hour
        configuration.setMaxAge(3600L);

        // Apply this CORS config to ALL URL paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
