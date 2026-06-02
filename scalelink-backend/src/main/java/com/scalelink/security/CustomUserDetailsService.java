package com.scalelink.security;

import com.scalelink.entity.User;
import com.scalelink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService — Loads user data from our database.
 *
 * WHAT IS UserDetailsService?
 * Spring Security needs to know HOW to load a user from your database.
 * It doesn't know about your User entity or UserRepository.
 * UserDetailsService is the bridge: it takes a username (or email)
 * and returns a UserDetails object that Spring Security understands.
 *
 * Spring Security calls this during JWT validation to load the user
 * and set up the security context.
 *
 * WHAT IS @RequiredArgsConstructor?
 * Lombok generates a constructor for all 'final' fields.
 * This is constructor-based dependency injection — the preferred
 * way in Spring (over @Autowired on fields).
 *
 * WHY constructor injection over @Autowired?
 * 1. Fields can be 'final' (immutable — safer)
 * 2. Dependencies are explicit (easy to see what a class needs)
 * 3. Easier to test (just pass mocks to the constructor)
 * 4. Fails fast if a dependency is missing (at startup, not at runtime)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by their email address.
     * Spring Security calls this method during authentication.
     *
     * @param email The user's email (we use email as the "username")
     * @return UserDetails object that Spring Security can work with
     * @throws UsernameNotFoundException if no user found with that email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Convert our User entity to Spring Security's UserDetails
        // The constructor takes: username, password, authorities (roles)
        // We pass an empty list for authorities since we don't have roles yet
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList()  // No roles for now
        );
    }

    /**
     * Load a user by their ID (used by JWT filter).
     */
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList()
        );
    }
}
