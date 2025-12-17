package com.smartwealth.smartwealth_backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    @Getter
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtTokenProvider(
            @Value("${jwt.secret.access}") String accessSecret,
            @Value("${jwt.secret.refresh}") String refreshSecret,
            @Value("${jwt.expiry.access}") long accessTokenExpiry,
            @Value("${jwt.expiry.refresh}") long refreshTokenExpiry
    ) {
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes());
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    // ACCESS TOKEN

    public String generateAccessToken(String customerId, String role) {
        return Jwts.builder()
                .subject(customerId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(accessKey)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessKey);
    }

    public String getCustomerIdFromAccessToken(String token) {
        return extractCustomerId(token, accessKey);
    }

    public String getRoleFromAccessToken(String token) {
        return extractRole(token, accessKey);
    }

//    REFRESH TOKEN

    public String generateRefreshToken(String customerId) {
        return Jwts.builder()
                .subject(customerId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(refreshKey)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshKey);
    }

    public String getCustomerIdFromRefreshToken(String token) {
        return extractCustomerId(token, refreshKey);
    }

    // COMMON

    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired");
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT");
        }
        return false;
    }

    private String extractCustomerId(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private String extractRole(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
