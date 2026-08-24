package com.mediatheque.auth_svc.service;

import com.mediatheque.auth_svc.dto.AuthResponse;
import com.mediatheque.auth_svc.dto.LoginRequest;
import com.mediatheque.auth_svc.dto.RegisterRequest;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("alice@example.com");
        registerRequest.setPassword("secret123");
        registerRequest.setFirstName("Alice");
        registerRequest.setLastName("Dupont");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("secret123");

        user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("encoded_secret")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  register
    // ------------------------------------------------------------------ //

    @Test
    void register_shouldReturnSuccess_whenEmailIsNew() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt_token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getMessage()).isEqualTo(" Inscription réussie.");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldReturnError_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getMessage()).isEqualTo(" Cet email est déjà utilisé.");
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_shouldEncodePassword_beforeSaving() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        authService.register(registerRequest);

        verify(passwordEncoder).encode("secret123");
    }



    @Test
    void login_shouldReturnSuccess_whenCredentialsAreValid() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt_token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getMessage()).isEqualTo(" Connexion réussie.");
    }

    @Test
    void login_shouldReturnError_whenBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getMessage()).isEqualTo(" Email ou mot de passe incorrect.");
        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldReturnError_whenUserNotFoundAfterAuth() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getMessage()).isEqualTo(" Utilisateur introuvable.");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldCallAuthenticationManager_withCorrectCredentials() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        authService.login(loginRequest);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("alice@example.com", "secret123")
        );
    }
}