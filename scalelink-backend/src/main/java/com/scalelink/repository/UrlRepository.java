package com.scalelink.repository;

import com.scalelink.entity.Url;
import com.scalelink.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Url Repository — Data access layer for the urls table.
 */
@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    /**
     * Fetch all URLs created by a specific user, ordered by creation date descending.
     * Supports pagination.
     */
    Page<Url> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find a URL by its short code (e.g., 'aB3x7Kp').
     * Used for redirection.
     */
    Optional<Url> findByShortCode(String shortCode);

    /**
     * Find a URL by its custom alias (e.g., 'summer-sale').
     */
    Optional<Url> findByCustomAlias(String customAlias);

    /**
     * Check if a short code already exists (to prevent collisions).
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Check if a custom alias is already taken.
     */
    boolean existsByCustomAlias(String customAlias);
}
