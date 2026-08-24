package com.collection.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret;
    private String validToken;

    private final String USER_ID = "user-123";
    private final String ROLE = "USER";

    @BeforeEach
    void setUp() {

        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
        secret = Base64.getEncoder().encodeToString(keyBytes);

        jwtUtil = new JwtUtil(secret);

        Key key = Keys.hmacShaKeyFor(keyBytes);
        validToken = Jwts.builder()
                .setSubject(USER_ID)
                .claim("role", ROLE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(key)
                .compact();
    }

    @Test
    void extractUserId_ShouldReturnCorrectSubject() {
        String extractedId = jwtUtil.extractUserId(validToken);
        assertThat(extractedId).isEqualTo(USER_ID);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String extractedRole = jwtUtil.extractRole(validToken);
        assertThat(extractedRole).isEqualTo(ROLE);
    }

    @Test
    void isTokenValid_ShouldReturnTrueForValidToken() {
        boolean isValid = jwtUtil.isTokenValid(validToken);
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForInvalidToken() {
        String invalidToken = validToken + "corrupted";
        boolean isValid = jwtUtil.isTokenValid(invalidToken);
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForExpiredToken() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        String expiredToken = Jwts.builder()
                .setSubject(USER_ID)
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        boolean isValid = jwtUtil.isTokenValid(expiredToken);
        assertThat(isValid).isFalse();
    }
}