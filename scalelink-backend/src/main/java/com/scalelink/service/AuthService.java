package com.scalelink.service;

import com.scalelink.dto.request.LoginRequest;
import com.scalelink.dto.request.RegisterRequest;
import com.scalelink.dto.response.AuthResponse;
import com.scalelink.entity.User;
import com.scalelink.repository.UserRepository;
import com.scalelink.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service — Business logic for registration and login.
 *
 * WHAT IS @Service?
 * Marks this class as a service bean — Spring creates one instance and
 * manages it. The @Service annotation is functionally identical to
 * @Component, but it communicates intent: "This is business logic."
 *
 * WHAT IS @Transactional?
 * Wraps the method in a database transaction. If any exception occurs,
 * ALL database changes in the method are rolled back.
 * Example: If we create a user but the email is taken (unique constraint),
 * the transaction rolls back — no partial data in the database.
 *
 * WHAT IS THE SERVICE LAYER FOR?
 * - Contains BUSINESS LOGIC (validation, rules, calculations)
 * - Calls the repository layer for database operations
 * - Returns DTOs (never entities) to the controller
 * - Orchestrates multiple operations in a single transaction
 *
 * LAYER RULE:
 *   Controller → receives HTTP request, calls Service
 *   Service → business logic, calls Repository
 *   Repository → database operations
 *   Controller NEVER calls Repository directly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user.
     *
     * FLOW:
     * 1. Check if email already exists → 409 Conflict
     * 2. Check if username already exists → 409 Conflict
     * 3. Hash the password with BCrypt
     * 4. Save the user to the database
     * 5. Generate a JWT token
     * 6. Return the token and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Create and save the user
        // BCrypt automatically generates a random salt and hashes the password
        // The result looks like: $2a$12$LJ3m4ys2k... (60 characters)
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate JWT token for immediate login after registration
        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .user(AuthResponse.UserSummary.builder()
                        .id(savedUser.getId())
                        .username(savedUser.getUsername())
                        .email(savedUser.getEmail())
                        .build())
                .build();
    }

    /**
     * Authenticate a user and return a JWT token.
     *
     * FLOW:
     * 1. Find the user by email
     * 2. Verify the password against the stored hash
     * 3. Generate a JWT token
     * 4. Return the token and user info
     *
     * SECURITY NOTE:
     * We use the SAME error message for "user not found" and "wrong password"
     * to prevent user enumeration attacks. If we said "user not found",
     * an attacker could discover which emails are registered.
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Verify password
        // passwordEncoder.matches() hashes the raw password and compares
        // it with the stored hash. BCrypt handles salt extraction internally.
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if account is active
        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}
