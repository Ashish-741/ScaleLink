package com.scalelink.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * THIS IS THE HEART OF JWT AUTHENTICATION.
 *
 * HOW IT WORKS:
 * Every HTTP request passes through this filter BEFORE reaching any controller.
 *
 * 1. Extract the JWT token from the "Authorization: Bearer <token>" header
 * 2. Validate the token (signature, expiration)
 * 3. Extract the user ID from the token
 * 4. Load the user from the database
 * 5. Set the user in Spring's SecurityContext
 * 6. Let the request continue to the controller
 *
 * After step 5, any controller can access the current user via:
 *   SecurityContextHolder.getContext().getAuthentication().getName()
 *
 * WHAT IS SecurityContext?
 * Spring Security stores the current user's authentication in a
 * thread-local variable called SecurityContext. When we set it here
 * in the filter, all downstream code (controllers, services) can
 * access it. Each request gets its own SecurityContext (thread-local
 * means each thread has its own copy).
 *
 * WHAT IS OncePerRequestFilter?
 * Ensures the filter runs exactly ONCE per request, even if there
 * are internal forwards or includes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT token from the Authorization header
            String jwt = extractTokenFromRequest(request);

            // Step 2: If token exists and is valid, authenticate the user
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Step 3: Extract user ID from the token
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

                // Step 4: Load the user from the database
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                // Step 5: Create an authentication token and set it in the SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // principal (the user)
                                null,                           // credentials (not needed, token is proof)
                                userDetails.getAuthorities()    // authorities (roles/permissions)
                        );

                // Attach request details (IP, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the security context
                // After this, the request is considered "authenticated"
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user ID: {} for URI: {}", userId, request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            // Don't throw — let the request continue. Spring Security will
            // return 401 if the endpoint requires authentication.
        }

        // Step 6: Continue the filter chain (pass to next filter or controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the JWT token from the Authorization header.
     *
     * Expected format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
     * We strip "Bearer " prefix and return just the token.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }
        return null;
    }
}
