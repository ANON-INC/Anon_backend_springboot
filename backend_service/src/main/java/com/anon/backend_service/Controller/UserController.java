package com.anon.backend_service.Controller;

import com.anon.backend_service.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import com.anon.backend_service.Service.JwtService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Public registration endpoint
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String displayName = request.get("displayName");

            if (email == null || password == null || displayName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            userService.registerUser(email, password, displayName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful. Please check your email to verify your account.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Login endpoint - returns JWT token
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
            }

            // Authenticate user with Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Get user details and generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);

            // Return success response with token
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwtToken);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Invalid credentials or authentication failure
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }
    }

// Add logout endpoint too (JWT is stateless - client must discard token)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful. Please discard your JWT token.");
        return ResponseEntity.ok(response);
    }

    // Public email verification endpoint - accepts 6-digit verification code
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");

            // Validate 6-digit code format
            if (code == null || !code.matches("\\d{6}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code format. Must be 6 digits."));
            }

            userService.verifyEmail(code);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully! You now have access to the system.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint to manually trigger cleanup of expired cached users (admin only)
    @PostMapping("/cleanup-expired")
    public ResponseEntity<Map<String, String>> cleanupExpiredUsers() {
        userService.cleanupExpiredCachedUsers();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Expired cached users cleaned up successfully");
        return ResponseEntity.ok(response);
    }
}
