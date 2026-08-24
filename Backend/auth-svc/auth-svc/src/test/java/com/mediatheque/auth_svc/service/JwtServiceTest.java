package com.mediatheque.auth_svc.service;

import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L; // 24h

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        user = User.builder()
                .id(1L)
                .email("john@test.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();
    }


    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtService.generateToken(user);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateToken_ShouldContainCorrectUsername() {
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("john@test.com");
    }

    @Test
    void generateToken_WithExtraClaims_ShouldWork() {
        Map<String, Object> extraClaims = Map.of("role", "USER");
        String token = jwtService.generateToken(extraClaims, user);
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo("john@test.com");
    }


    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo("john@test.com");
    }


    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenWrongUser() {
        String token = jwtService.generateToken(user);

        User otherUser = User.builder()
                .email("other@test.com")
                .password("pass")
                .role(Role.USER)
                .enabled(true)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenExpired() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String expiredToken = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, user))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void generateToken_ShouldBeInvalid_AfterExpiration() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(user);

        try { Thread.sleep(10); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }}