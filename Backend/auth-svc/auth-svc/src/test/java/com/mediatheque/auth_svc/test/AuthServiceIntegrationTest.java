package com.mediatheque.auth_svc.test;

import com.mediatheque.auth_svc.dto.AuthResponse;
import com.mediatheque.auth_svc.dto.LoginRequest;
import com.mediatheque.auth_svc.dto.RegisterRequest;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.UserRepository;
import com.mediatheque.auth_svc.repository.PasswordResetTokenRepository;
import com.mediatheque.auth_svc.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void register_shouldCreateUser_andReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Dupont");
        request.setEmail("alice.unique@example.com");
        request.setPassword("password123");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isNotNull();
        assertThat(userRepository.existsByEmail("alice.unique@example.com")).isTrue();
    }

    @Test
    void register_shouldReturnError_whenEmailAlreadyUsed() {
        User existingUser = User.builder()
                .email("duplicate@example.com")
                .password("hashed_pwd")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.saveAndFlush(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isNull();
        assertThat(response.getMessage()).containsIgnoringCase("déjà utilisé");
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Préparation : Inscription
        RegisterRequest register = new RegisterRequest();
        register.setEmail("login@example.com");
        register.setPassword("secret123");
        register.setFirstName("Jean");
        register.setLastName("Test");
        authService.register(register);

        LoginRequest login = new LoginRequest();
        login.setEmail("login@example.com");
        login.setPassword("secret123");

        AuthResponse response = authService.login(login);

        assertThat(response.getToken()).isNotNull();
        assertThat(response.getMessage()).containsIgnoringCase("réussie");
    }
}