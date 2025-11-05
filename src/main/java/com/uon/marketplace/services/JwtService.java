package com.uon.marketplace.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Service for secure token-based authentication
 * Implements industry-standard JWT practices with expiration and claims
 */
@Service
public class JwtService {

    @Value("${jwt.secret:UON-Marketplace-Super-Secret-Key-For-JWT-2024-Change-In-Production-Min-256-Bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;

    private static final String ISSUER = "UON-Marketplace";

    /**
     * Generate JWT token for authenticated user
     * @param userId user ID
     * @param email user email
     * @param role user role
     * @param twoFactorVerified whether 2FA was verified
     * @return JWT token string
     */
    public String generateToken(Long userId, String email, String role, boolean twoFactorVerified) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("twoFactorVerified", twoFactorVerified);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract all claims from token
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Check if 2FA was verified for this token
     */
    public boolean isTwoFactorVerified(String token) {
        return extractClaims(token).get("twoFactorVerified", Boolean.class);
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
