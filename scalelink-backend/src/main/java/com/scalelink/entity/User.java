package com.scalelink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity — Maps to the "users" table in PostgreSQL.
 *
 * WHAT IS AN ENTITY?
 * An entity is a Java class that represents a database table.
 * Each instance of this class = one row in the "users" table.
 * JPA (Java Persistence API) + Hibernate handle the translation
 * between Java objects and SQL automatically.
 *
 * WHAT IS @Entity?
 * Tells JPA: "This class maps to a database table."
 * Without this, JPA ignores the class.
 *
 * WHAT IS @Table?
 * Specifies the table name. If omitted, JPA uses the class name.
 * We explicitly set it to "users" for clarity.
 *
 * WHAT IS @Id + @GeneratedValue?
 * @Id marks the primary key field.
 * @GeneratedValue(IDENTITY) tells JPA the database generates the ID
 * (using our GENERATED ALWAYS AS IDENTITY in PostgreSQL).
 * You never set the ID manually — the database does it on INSERT.
 *
 * WHAT IS @Column?
 * Maps a Java field to a specific database column.
 * Properties like unique, nullable, length add constraints.
 * If omitted, JPA maps the field to a column with the same name.
 *
 * WHAT ARE @PrePersist / @PreUpdate?
 * Lifecycle callbacks — methods that JPA calls automatically:
 * - @PrePersist → called BEFORE inserting a new row (used for createdAt)
 * - @PreUpdate → called BEFORE updating an existing row (used for updatedAt)
 *
 * WHAT IS FetchType.LAZY?
 * When you load a User from the database, LAZY means:
 * "Don't load the user's URLs yet. Only load them when I explicitly
 * access user.getUrls()."
 * This prevents loading thousands of URLs when you only need the user's name.
 * EAGER would load everything at once — terrible for performance.
 */
@Entity
@Table(name = "users")
@Data                      // Generates getters, setters, toString, equals, hashCode
@Builder                   // Generates builder pattern: User.builder().email("...").build()
@NoArgsConstructor         // Generates no-args constructor (required by JPA)
@AllArgsConstructor        // Generates all-args constructor (used by Builder)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One user can have many URLs
    // mappedBy = "user" refers to the "user" field in the Url entity
    // CascadeType.ALL means: if we delete a user, delete their URLs too
    // FetchType.LAZY means: don't load URLs until we explicitly access them
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude        // Prevent infinite recursion in toString()
    @EqualsAndHashCode.Exclude
    private List<com.scalelink.entity.Url> urls = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
