package com.gotrip.common_library.service;

import com.gotrip.common_library.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper; // Standard Jackson
import com.gotrip.common_library.dto.user.UserResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretString;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes());
    }

    // Now uses UserResponseDTO so it can work in Auth-Service without a DB
    public String generateAccessToken(Long userId, String email, String user) {
        try {
            String userJson = objectMapper.writeValueAsString(user);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("email", email);
            claims.put("user", userJson); // Storing serialized DTO for the filter to read

            return Jwts.builder()
                    .claims(claims)
                    .subject(email)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + AppConfig.ACCESS_TOKEN_EXPIRATION * 1000L))
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error serializing user DTO to JWT", e);
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public Map<String, Object> generateRefreshToken() {
        // AppConfig should also be in common_library/config
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(AppConfig.REFRESH_TOKEN_EXPIRATION);
        String token = UUID.randomUUID().toString();
        return Map.of("token", token, "expiration", expiration);
    }
}