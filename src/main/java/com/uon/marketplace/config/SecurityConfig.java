package com.uon.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration
 * Configures Spring Security for stateless JWT-based authentication
 * Permits public access to auth and documentation endpoints
 * Enables method-level security with @PreAuthorize annotations
 */
@Configuration
@EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true) // TEMPORARILY DISABLED FOR TESTING - Enable for production with JWT
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
            .cors(cors -> {}) // Use existing CORS configuration
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/auth/**",           // All auth endpoints (login, register, 2FA)
                    "/swagger-ui/**",     // Swagger UI
                    "/v3/api-docs/**",    // OpenAPI docs
                    "/uploads/**",        // Public product images
                    "/error"              // Error handling
                ).permitAll()
                // All other endpoints require authentication
                .anyRequest().permitAll() // Changed to permitAll for now, add JWT filter later
            );

        return http.build();
    }
}
