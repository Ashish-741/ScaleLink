package com.scalelink.controller;

import com.scalelink.dto.request.LoginRequest;
import com.scalelink.dto.request.RegisterRequest;
import com.scalelink.dto.response.ApiResponse;
import com.scalelink.dto.response.AuthResponse;
import com.scalelink.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller — Handles registration and login endpoints.
 *
 * WHAT IS @RestController?
 * Combines @Controller + @ResponseBody.
 * - @Controller: Marks this as a web controller (handles HTTP requests)
 * - @ResponseBody: Return values are serialized to JSON automatically
 * So instead of returning a JSP view, we return JSON data.
 *
 * WHAT IS @RequestMapping?
 * Sets the BASE path for all endpoints in this controller.
 * All methods here will start with "/api/v1/auth/..."
 *
 * WHAT IS @PostMapping?
 * Maps HTTP POST requests to this method.
 * Equivalent to: @RequestMapping(method = RequestMethod.POST, path = "/register")
 *
 * WHAT IS @RequestBody?
 * Tells Spring: "Deserialize the HTTP request body (JSON) into this Java object."
 * Example: {"username": "ashish", "email": "a@b.com"} → RegisterRequest object
 *
 * WHAT IS @Valid?
 * Triggers Bean Validation on the request body.
 * All @NotBlank, @Email, @Size annotations on the DTO are checked.
 * If validation fails, Spring throws MethodArgumentNotValidException
 * (caught by our GlobalExceptionHandler → returns 400 with field errors).
 *
 * WHAT IS ResponseEntity?
 * A wrapper that lets you control the HTTP response:
 * - Status code (201 Created, 200 OK, etc.)
 * - Headers
 * - Body (the response data)
 * Without ResponseEntity, Spring always returns 200 OK.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register — Register a new user.
     *
     * Success: 201 Created with JWT token
     * Failure: 400 (validation), 409 (duplicate email/username)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)     // 201 Created (a new resource was created)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    /**
     * POST /api/v1/auth/login — Authenticate a user.
     *
     * Success: 200 OK with JWT token
     * Failure: 401 (invalid credentials)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success("Login successful", authResponse));
    }
}
