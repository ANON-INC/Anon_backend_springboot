package com.anon.backend_service.Repository;

import com.anon.backend_service.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailVerificationToken(String token);
    List<User> findByIsCachedTrueAndCacheExpiryBefore(LocalDateTime now);
    boolean existsByEmail(String email);
    boolean existsByAnonymousId(UUID anonymousId);
}