package com.scalelink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.scalelink.security.JwtAuthenticationFilter;

/**
 * Security Configuration (Initial Setup)
 *
 * WHAT IS SPRING SECURITY?
 * Spring Security is a powerful framework that handles:
 * - Authentication (WHO are you? → JWT token validation)
 * - Authorization (WHAT can you do? → role-based access control)
 * - Protection (CSRF, XSS, clickjacking, etc.)
 *
 * By default, when you add spring-boot-starter-security, ALL endpoints
 * are locked down. You need to configure which ones are public.
 *
 * WHAT IS SecurityFilterChain?
 * It's a chain of filters that every request passes through. Spring
 * Security adds ~15 filters by default. We customize this chain to:
 * - Allow public access to certain endpoints
 * - Require JWT authentication for protected endpoints
 * - Disable CSRF (not needed for stateless APIs)
 * - Use stateless sessions (no server-side sessions)
 *
 * NOTE: This is the INITIAL setup. We'll add JWT filter in Phase 5.
 * For now, we configure the basic structure so the app starts.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define the security filter chain.
     *
     * WHAT IS HttpSecurity?
     * A builder that lets you configure security rules. Each method
     * call adds or modifies a security filter in the chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            // CSRF: Disabled because we use JWT (stateless API)
            // CSRF protection is for cookie-based sessions where a malicious
            // site could trick the browser into sending requests with the
            // user's cookies. Since we use JWT in the Authorization header
            // (not cookies), CSRF attacks aren't possible.
            .csrf(AbstractHttpConfigurer::disable)

            // CORS: Use our CorsConfig configuration
            .cors(cors -> cors.configurationSource(
                    new CorsConfig().corsConfigurationSource()))

            // SESSION: Stateless — never create HTTP sessions
            // This is crucial for horizontal scaling. If we had sessions,
            // we'd need sticky sessions or session replication across servers.
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL AUTHORIZATION RULES
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication needed
                .requestMatchers("/api/v1/auth/**").permitAll()     // Login, Register
                .requestMatchers("/actuator/health").permitAll()     // Health check
                .requestMatchers("/swagger-ui/**").permitAll()       // Swagger UI
                .requestMatchers("/api-docs/**").permitAll()         // API docs
                .requestMatchers("/v3/api-docs/**").permitAll()      // OpenAPI spec

                // Short URL redirect — must be public!
                // This regex matches single path segments like /aB3x7Kp
                // but NOT /api/v1/something
                .requestMatchers("/{shortCode:[a-zA-Z0-9-]{3,30}}").permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated()
            );

            // Add JWT filter before the standard Spring Security authentication filter
            // We check the JWT token BEFORE Spring tries to do its default authentication
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password Encoder Bean
     *
     * WHAT IS BCrypt?
     * BCrypt is a password hashing algorithm designed to be SLOW on purpose.
     * Why slow? Because it makes brute-force attacks impractical.
     *
     * - MD5 can hash billions per second → cracked instantly
     * - BCrypt with strength 12 → ~250ms per hash → 4 hashes/second
     * - At 4 hashes/second, trying 1 billion passwords takes ~8 years
     *
     * The "12" is the "cost factor" or "work factor":
     * - Cost 10 = 2^10 = 1024 rounds → ~100ms
     * - Cost 12 = 2^12 = 4096 rounds → ~250ms (our choice)
     * - Cost 14 = 2^14 = 16384 rounds → ~1 second
     *
     * BCrypt also automatically handles SALTING:
     * Each password gets a random salt, so two users with the same
     * password will have different hashes. This prevents rainbow table attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
