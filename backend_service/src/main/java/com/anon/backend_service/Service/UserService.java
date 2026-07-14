package com.anon.backend_service.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.anon.backend_service.Model.User;
import com.anon.backend_service.Repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final EmailService emailService;
    
    // In-memory cache for unverified users - only saved to DB after email verification
    private final Map<String, User> unverifiedUsersCache = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Registration logic - cache unverified users in-memory until email verification
    public User registerUser(String email, String password, String displayName) {
        // Check if email already exists in database OR in cache
        if (userRepository.existsByEmail(email) || unverifiedUsersCache.values().stream()
                .anyMatch(u -> u.getEmail().equals(email))) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        
        // Generate unique anonymous ID - this maintains separation between account and posts
        UUID anonymousId;
        while (true) {
            UUID tempId = UUID.randomUUID();
            // Check if tempId exists in database or cache
            boolean existsInDb = userRepository.existsByAnonymousId(tempId);
            boolean existsInCache = unverifiedUsersCache.values().stream()
                .anyMatch(u -> u.getAnonymousId().equals(tempId));
            
            if (!existsInDb && !existsInCache) {
                anonymousId = tempId;
                break;
            }
        }
        user.setAnonymousId(anonymousId);
        
        // Set up email verification with 6-digit code
        String verificationCode = generateSixDigitCode();
        user.setEmailVerificationToken(verificationCode);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15)); // 15 minutes expiry
        
        // Mark as cached and not yet verified
        user.setCached(true);
        user.setEmailVerified(false);
        user.setHasSystemAccess(false);
        user.setCreatedAt(LocalDateTime.now());
        
        // Store in in-memory cache only - NOT saved to database yet!
        unverifiedUsersCache.put(verificationCode, user);
        
        // Send verification email
        emailService.sendVerificationEmail(email, verificationCode);
        
        return user;
    }
    
    // Verify email and grant system access - only save to database after successful verification
    public void verifyEmail(String token) {
        // First check in-memory cache for the unverified user
        User user = unverifiedUsersCache.remove(token);
        if (user == null) {
            throw new RuntimeException("Invalid or expired verification token");
        }
            
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }
        
        // Now that email is verified, save to the database permanently
        user.setEmailVerified(true);
        user.setCached(false);
        user.setHasSystemAccess(true);
        user.setGrantedAccessAt(LocalDateTime.now());
        
        // Clear verification data since it's no longer needed
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setCacheExpiry(null);
        
        // Save to database only after successful verification!
        userRepository.save(user);
    }
    
    // Cleanup expired cached users from in-memory cache
    @Scheduled(fixedRate = 300000) // Run every 60 seconds
    public void cleanupExpiredCachedUsers() {
        System.out.println("Running scheduled cleanup of expired cached users..." + LocalDateTime.now());
        // First count how many expired users are in the cache
        long expiredCount = unverifiedUsersCache.values().stream()
            .filter(user -> user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now()))
            .count();

        // Clean up in-memory cache
        unverifiedUsersCache.entrySet().removeIf(entry ->
            entry.getValue().getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now()));
            
        // Also clean up any accidentally persisted cached users
        userRepository.findByIsCachedTrueAndCacheExpiryBefore(LocalDateTime.now())
            .forEach(userRepository::delete);

            System.out.println("Cleanup completed. Expired cached users removed: " + expiredCount + " Remaining users: " + unverifiedUsersCache.size());
    }
    
    // Generate 6-digit verification code
    private String generateSixDigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    // Get user by email for authentication
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}