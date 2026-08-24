package com.mediatheque.auth_svc.service;

import com.mediatheque.auth_svc.model.PasswordResetToken;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.PasswordResetTokenRepository;
import com.mediatheque.auth_svc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;
    private PasswordResetToken validToken;
    private PasswordResetToken expiredToken;
    private PasswordResetToken usedToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@test.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();

        validToken = PasswordResetToken.builder()
                .id(1L)
                .code("394601")
                .user(user)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .used(false)
                .build();

        expiredToken = PasswordResetToken.builder()
                .id(2L)
                .code("111111")
                .user(user)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5))
                .used(false)
                .build();

        usedToken = PasswordResetToken.builder()
                .id(3L)
                .code("222222")
                .user(user)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .used(true)
                .build();
    }



    @Test
    void sendResetCode_shouldSendEmail_whenUserExists() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        passwordResetService.sendResetCode("john@test.com");

        verify(tokenRepository).deleteByUser_Id(1L);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetCode(eq("john@test.com"), anyString());
    }

    @Test
    void sendResetCode_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.sendResetCode("unknown@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aucun compte trouvé avec cet email");

        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    void sendResetCode_shouldDeleteOldTokens_beforeSavingNew() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        passwordResetService.sendResetCode("john@test.com");

        var inOrder = inOrder(tokenRepository);
        inOrder.verify(tokenRepository).deleteByUser_Id(1L);
        inOrder.verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void sendResetCode_shouldSaveToken_withCorrectFields() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        passwordResetService.sendResetCode("john@test.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getCode()).isNotNull().hasSize(6);
        // FIX: comparer en UTC comme le service
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now(ZoneOffset.UTC));
    }


    @Test
    void verifyCode_shouldSucceed_whenCodeIsValidAndNotExpired() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(validToken));

        assertThatNoException().isThrownBy(() ->
                passwordResetService.verifyCode("john@test.com", "394601"));
    }

    @Test
    void verifyCode_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.verifyCode("unknown@test.com", "394601"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aucun compte trouvé avec cet email");
    }

    @Test
    void verifyCode_shouldThrow_whenNoActiveToken() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.verifyCode("john@test.com", "394601"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aucun code actif trouvé");
    }

    @Test
    void verifyCode_shouldThrow_whenCodeIsWrong() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> passwordResetService.verifyCode("john@test.com", "000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Code incorrect");
    }

    @Test
    void verifyCode_shouldThrow_whenCodeIsExpired() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        // FIX: expiredToken utilise maintenant UTC dans setUp()
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> passwordResetService.verifyCode("john@test.com", "111111"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Code expiré");
    }



    @Test
    void resetPassword_shouldUpdatePassword_whenCodeValid() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        passwordResetService.resetPassword("john@test.com", "394601", "newPassword123");

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void resetPassword_shouldMarkToken_asUsed() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        passwordResetService.resetPassword("john@test.com", "394601", "newPass");

        assertThat(validToken.isUsed()).isTrue();
        verify(tokenRepository).save(validToken);
    }

    @Test
    void resetPassword_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("unknown@test.com", "394601", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aucun compte trouvé avec cet email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_shouldThrow_whenNoActiveToken() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("john@test.com", "394601", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aucun code actif trouvé");

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_shouldThrow_whenCodeIsWrong() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("john@test.com", "000000", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Code incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_shouldThrow_whenCodeIsExpired() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndUsedFalse(1L)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("john@test.com", "111111", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Code expiré");

        verify(userRepository, never()).save(any());
    }
}