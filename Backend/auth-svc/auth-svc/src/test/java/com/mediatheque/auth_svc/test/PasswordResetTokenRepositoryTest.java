package com.mediatheque.auth_svc.test;

import com.mediatheque.auth_svc.model.PasswordResetToken;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.PasswordResetTokenRepository;
import com.mediatheque.auth_svc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("alice@example.com")
                .password("hashed")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .build());
    }



    @Test
    void findByUser_IdAndUsedFalse_returnsToken_whenCodeNotUsed() {
        tokenRepository.save(PasswordResetToken.builder()
                .code("123456")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("123456");
        assertThat(result.get().isUsed()).isFalse();
        assertThat(result.get().getUser().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByUser_IdAndUsedFalse_returnsEmpty_whenCodeAlreadyUsed() {
        tokenRepository.save(PasswordResetToken.builder()
                .code("654321")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build());

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndUsedFalse_returnsEmpty_whenNoTokenExists() {
        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndUsedFalse_returnsEmpty_whenWrongUserId() {
        tokenRepository.save(PasswordResetToken.builder()
                .code("111111")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(999L);

        assertThat(result).isEmpty();
    }



    @Test
    void deleteByUser_Id_removesAllTokensOfUser() {
        tokenRepository.save(PasswordResetToken.builder()
                .code("111111")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        tokenRepository.save(PasswordResetToken.builder()
                .code("222222")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build());

        assertThat(tokenRepository.findAll()).hasSize(2);

        tokenRepository.deleteByUser_Id(user.getId());

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void deleteByUser_Id_doesNothing_whenNoTokenExists() {
        tokenRepository.deleteByUser_Id(user.getId());

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void deleteByUser_Id_doesNotDeleteTokensOfOtherUsers() {
        User otherUser = userRepository.save(User.builder()
                .email("bob@example.com")
                .password("hashed")
                .firstName("Bob")
                .lastName("Martin")
                .role(Role.USER)
                .enabled(true)
                .build());

        tokenRepository.save(PasswordResetToken.builder()
                .code("333333")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        tokenRepository.save(PasswordResetToken.builder()
                .code("444444")
                .user(otherUser)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        tokenRepository.deleteByUser_Id(user.getId());

        assertThat(tokenRepository.findAll()).hasSize(1);
        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(otherUser.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("444444");
    }


    @Test
    void save_persistsToken_withCorrectFields() {
        PasswordResetToken saved = tokenRepository.save(PasswordResetToken.builder()
                .code("555555")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("555555");
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
    }
}