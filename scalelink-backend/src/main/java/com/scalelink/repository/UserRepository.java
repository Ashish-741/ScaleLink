package com.scalelink.repository;

import com.scalelink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository — Data access layer for the users table.
 *
 * WHAT IS A REPOSITORY?
 * A repository is an interface (not a class!) that provides database
 * operations. You declare METHOD SIGNATURES, and Spring Data JPA
 * AUTOMATICALLY generates the implementation at runtime.
 *
 * WHAT IS JpaRepository<User, Long>?
 * - User = the entity type this repository manages
 * - Long = the type of the entity's primary key (id)
 * JpaRepository gives you these methods FOR FREE (no code needed):
 * - save(user) → INSERT or UPDATE
 * - findById(id) → SELECT WHERE id = ?
 * - findAll() → SELECT *
 * - delete(user) → DELETE WHERE id = ?
 * - count() → SELECT COUNT(*)
 * - And many more!
 *
 * WHAT IS QUERY DERIVATION?
 * Spring Data JPA reads your method name and generates SQL from it:
 * - findByEmail(email) → SELECT * FROM users WHERE email = ?
 * - existsByEmail(email) → SELECT COUNT(*) > 0 FROM users WHERE email = ?
 * - findByUsernameOrEmail(username, email) → SELECT * WHERE username = ? OR email = ?
 *
 * This is MAGIC. You write ZERO SQL, and Spring generates type-safe queries.
 *
 * WHAT IS Optional<User>?
 * Optional is a container that may or may not contain a value.
 * Instead of returning null (which causes NullPointerException),
 * we return Optional.empty(). The caller must handle both cases:
 *   userRepo.findByEmail("x@y.com")
 *     .orElseThrow(() -> new ResourceNotFoundException(...))
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address.
     * Used during: login, registration (uniqueness check)
     * Generated SQL: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by username.
     * Used during: registration (uniqueness check)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find by email OR username (for flexible login).
     * Generated SQL: SELECT * FROM users WHERE email = ? OR username = ?
     */
    Optional<User> findByEmailOrUsername(String email, String username);

    /**
     * Check if an email already exists (for registration validation).
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * Returns boolean instead of loading the full entity — more efficient.
     */
    boolean existsByEmail(String email);

    /**
     * Check if a username already exists.
     */
    boolean existsByUsername(String username);
}
