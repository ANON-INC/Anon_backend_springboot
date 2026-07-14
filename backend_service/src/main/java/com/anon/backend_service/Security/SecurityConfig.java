package com.anon.backend_service.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Whitelisted public endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/user/register",           // User registration
        "/user/verify-email",       // Email verification
        "/user/verify",             // Verification link endpoint
        "/user/login",              // Login endpoint
        "/api/posts",                   // Get all public posts
        "/api/posts/{id}",              // Get single post
        "/api/posts/public/**",         // All public post endpoints
        "/error"                        // Spring Boot error endpoint
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()  // Whitelist all public endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin-only endpoints
                // Only verified users with system access can create/modify posts
                .requestMatchers("/api/posts/create", "/api/posts/update/**", "/api/posts/delete/**").authenticated()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
            .httpBasic(withDefaults())
            .formLogin(form -> form
                .loginProcessingUrl("/user/login")
                .successHandler((request, response, authentication) -> {
                    // Never expose sensitive user data in responses
                    // Only return a success status - session is managed by Spring Security
                    response.setStatus(200);
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                })
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                })
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}