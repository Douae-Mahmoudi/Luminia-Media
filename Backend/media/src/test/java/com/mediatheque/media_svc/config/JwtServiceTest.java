package com.mediatheque.media_svc.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private String generateToken(String email, String role, long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        if (role != null) claims.put("role", role);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
    }

    @Test
    void extractUsername_shouldReturnEmail() {
        String token = generateToken("test@test.com", "USER", 86400000);
        String username = jwtService.extractUsername(token);
        assertEquals("test@test.com", username);
    }

    @Test
    void extractClaim_role_shouldReturnUSER() {
        String token = generateToken("test@test.com", "USER", 86400000);
        String role = jwtService.extractClaim(token,
                claims -> claims.get("role", String.class));
        assertEquals("USER", role);
    }

    @Test
    void extractClaim_role_shouldReturnADMIN() {
        String token = generateToken("admin@test.com", "ADMIN", 86400000);
        String role = jwtService.extractClaim(token,
                claims -> claims.get("role", String.class));
        assertEquals("ADMIN", role);
    }

    @Test
    void extractClaim_role_shouldReturnNull_whenNoRoleClaim() {
        String token = generateToken("test@test.com", null, 86400000);
        String role = jwtService.extractClaim(token,
                claims -> claims.get("role", String.class));
        assertNull(role);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenValid() {
        String token = generateToken("test@test.com", "USER", 86400000);
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "test@test.com", "", java.util.List.of());
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenWrongUsername() {
        String token = generateToken("test@test.com", "USER", 86400000);
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "other@test.com", "", java.util.List.of());
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenExpired() {
        String token = generateToken("test@test.com", "USER", -86400000); // expiré hier

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "test@test.com", "", java.util.List.of());

        try {
            boolean result = jwtService.isTokenValid(token, userDetails);
            assertFalse(result);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            assertTrue(true);
        }
    }
}
