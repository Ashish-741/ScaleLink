package com.scalelink.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration Request DTO
 *
 * WHAT IS A DTO (Data Transfer Object)?
 * A DTO is a simple object that carries data between layers.
 * We NEVER expose our Entity directly to the API because:
 * 1. Entity has passwordHash — we'd leak it in the response
 * 2. Entity has JPA annotations — not relevant for the API
 * 3. Entity structure might change — API format should be stable
 *
 * DTO = what the client sends/receives
 * Entity = what the database stores
 *
 * WHAT IS @Valid (on the controller parameter)?
 * When a controller has @Valid on a request body parameter,
 * Spring automatically validates all the annotations below
 * (@NotBlank, @Email, @Size, @Pattern) BEFORE your code runs.
 * If validation fails, Spring throws MethodArgumentNotValidException,
 * which our GlobalExceptionHandler catches and returns a 400 error
 * with field-specific error messages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least 1 uppercase letter, 1 number, and 1 special character")
    private String password;
}
