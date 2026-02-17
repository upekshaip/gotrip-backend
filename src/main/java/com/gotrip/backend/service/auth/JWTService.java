package com.gotrip.backend.service.auth;

import com.gotrip.backend.config.AppConfig;
import com.gotrip.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

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

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes());
    }

    // Generate Token with FilteredUser data
    public String generateAccessToken(User user) {
        try {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("user", jsonString);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + AppConfig.ACCESS_TOKEN_EXPIRATION * 1000))
                .signWith(getSigningKey())
                .compact();
    } catch (Exception e) {
        throw new RuntimeException("Error serializing user to JWT", e);
    }}

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
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(AppConfig.REFRESH_TOKEN_EXPIRATION);
        String token = UUID.randomUUID().toString() ;
        return Map.of("token", token, "expiration", expiration);
    }
}
