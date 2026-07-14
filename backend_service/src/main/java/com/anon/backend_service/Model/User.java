package com.anon.backend_service.Model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
public class User implements UserDetails {
    
    // Required by UserDetails interface - default authorities for regular users
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // All users have basic user permissions - add roles like "ROLE_ADMIN" if needed
        return Collections.emptyList();
    }
    
    @Override
    public String getUsername() {
        // Use email as the username for Spring Security
        return this.email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        // All accounts are valid (never expire)
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        // No account locking implemented yet
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        // Passwords never expire
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        // Only enable accounts that have verified their email and have system access
        return this.emailVerified && this.hasSystemAccess;
    }

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