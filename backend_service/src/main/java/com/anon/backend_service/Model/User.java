package com.anon.backend_service.Model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String displayName;

    // Anonymous identifier that completely separates account from content
    // This is the only link between a user and their posts - never expose this
    @Column(unique = true, nullable = false)
    private UUID anonymousId;

    // Email verification system - users are cached until verified
    private boolean emailVerified = false;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    // Caching status for unverified users
    private boolean isCached = true;
    private LocalDateTime cachedAt = LocalDateTime.now();
    private LocalDateTime cacheExpiry;

    // System access granted only after email verification
    private boolean hasSystemAccess = false;
    private LocalDateTime grantedAccessAt;

    // Audit timestamps
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
