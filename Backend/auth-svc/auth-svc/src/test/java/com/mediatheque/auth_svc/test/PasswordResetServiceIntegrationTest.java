package com.mediatheque.auth_svc.test;

import com.mediatheque.auth_svc.model.PasswordResetToken;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.PasswordResetTokenRepository;
import com.mediatheque.auth_svc.repository.UserRepository;
import com.mediatheque.auth_svc.service.EmailService;
import com.mediatheque.auth_svc.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PasswordResetServiceIntegrationTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    private User user;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        user = userRepository.save(User.builder()
                .email("alice@example.com")
                .password("hashed")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .build());

        doNothing().when(emailService).sendPasswordResetCode(anyString(), anyString());
    }



    @Test
    void sendResetCode_createsToken_andCallsEmailService() {
        passwordResetService.sendResetCode("alice@example.com");

        assertThat(tokenRepository.findAll()).hasSize(1);
        PasswordResetToken token = tokenRepository.findAll().get(0);
        assertThat(token.getUser().getEmail()).isEqualTo("alice@example.com");
        assertThat(token.getCode()).hasSize(6);
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now(ZoneOffset.UTC));

        verify(emailService).sendPasswordResetCode(eq("alice@example.com"), anyString());
    }

    @Test
    void sendResetCode_deletesOldToken_beforeCreatingNew() {
        passwordResetService.sendResetCode("alice@example.com");
        assertThat(tokenRepository.findAll()).hasSize(1);

        passwordResetService.sendResetCode("alice@example.com");
        assertThat(tokenRepository.findAll()).hasSize(1);
    }

    @Test
    void sendResetCode_throws_whenEmailNotFound() {
        assertThrows(RuntimeException.class,
                () -> passwordResetService.sendResetCode("inconnu@example.com"));

        verifyNoInteractions(emailService);
    }



    @Test
    void verifyCode_success_whenCodeIsValidAndNotExpired() {
        passwordResetService.sendResetCode("alice@example.com");
        String code = tokenRepository.findAll().get(0).getCode();

        passwordResetService.verifyCode("alice@example.com", code);
    }

    @Test
    void verifyCode_throws_whenCodeIsWrong() {
        passwordResetService.sendResetCode("alice@example.com");

        assertThrows(RuntimeException.class,
                () -> passwordResetService.verifyCode("alice@example.com", "000000"));
    }

    @Test
    void verifyCode_throws_whenCodeIsExpired() {
        // FIX: utiliser UTC comme le service
        PasswordResetToken expired = PasswordResetToken.builder()
                .code("123456")
                .user(user)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1))
                .used(false)
                .build();
        tokenRepository.save(expired);

        assertThrows(RuntimeException.class,
                () -> passwordResetService.verifyCode("alice@example.com", "123456"));
    }

    @Test
    void verifyCode_throws_whenEmailNotFound() {
        assertThrows(RuntimeException.class,
                () -> passwordResetService.verifyCode("inconnu@example.com", "123456"));
    }


    @Test
    void resetPassword_updatesPassword_andMarksTokenUsed() {
        passwordResetService.sendResetCode("alice@example.com");
        String code = tokenRepository.findAll().get(0).getCode();

        passwordResetService.resetPassword("alice@example.com", code, "NouveauMdp123!");

        User updated = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("NouveauMdp123!", updated.getPassword())).isTrue();

        PasswordResetToken usedToken = tokenRepository.findAll().get(0);
        assertThat(usedToken.isUsed()).isTrue();
    }

    @Test
    void resetPassword_throws_whenCodeIsWrong() {
        passwordResetService.sendResetCode("alice@example.com");

        assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword("alice@example.com", "000000", "NouveauMdp123!"));
    }

    @Test
    void resetPassword_throws_whenCodeIsExpired() {
        // FIX: utiliser UTC comme le service
        PasswordResetToken expired = PasswordResetToken.builder()
                .code("999999")
                .user(user)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1))
                .used(false)
                .build();
        tokenRepository.save(expired);

        assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword("alice@example.com", "999999", "NouveauMdp123!"));
    }

    @Test
    void resetPassword_throws_whenEmailNotFound() {
        assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword("inconnu@example.com", "123456", "NouveauMdp123!"));
    }
}









