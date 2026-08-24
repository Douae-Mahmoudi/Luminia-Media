package com.mediatheque.auth_svc.repository;

import com.mediatheque.auth_svc.model.PasswordResetToken;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("user@example.com")
                .password("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();
        em.persist(user);
        em.flush();
    }



    @Test
    void findByUser_IdAndUsedFalse_returnsToken_whenCodeNotUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .code("123456")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        em.persist(token);
        em.flush();

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("123456");
        assertThat(result.get().isUsed()).isFalse();
    }

    @Test
    void findByUser_IdAndUsedFalse_returnsEmpty_whenCodeAlreadyUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .code("654321")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build();
        em.persist(token);
        em.flush();

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndUsedFalse_returnsEmpty_whenNoTokenForUser() {
        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndUsedFalse_returnsEmpty_whenWrongUserId() {
        PasswordResetToken token = PasswordResetToken.builder()
                .code("111111")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        em.persist(token);
        em.flush();

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(999L);

        assertThat(result).isEmpty();
    }


    @Test
    void deleteByUser_Id_removesAllTokensOfUser() {
        PasswordResetToken token1 = PasswordResetToken.builder()
                .code("111111")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        PasswordResetToken token2 = PasswordResetToken.builder()
                .code("222222")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build();
        em.persist(token1);
        em.persist(token2);
        em.flush();

        tokenRepository.deleteByUser_Id(user.getId());
        em.flush();
        em.clear();

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(user.getId());
        assertThat(result).isEmpty();
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void deleteByUser_Id_doesNothing_whenNoTokenExists() {
        // ne doit pas lever d'exception
        tokenRepository.deleteByUser_Id(user.getId());
        em.flush();

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void deleteByUser_Id_doesNotDeleteTokensOfOtherUsers() {
        User otherUser = User.builder()
                .email("other@example.com")
                .password("encoded_password")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();
        em.persist(otherUser);

        PasswordResetToken tokenUser = PasswordResetToken.builder()
                .code("333333")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        PasswordResetToken tokenOther = PasswordResetToken.builder()
                .code("444444")
                .user(otherUser)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        em.persist(tokenUser);
        em.persist(tokenOther);
        em.flush();

        tokenRepository.deleteByUser_Id(user.getId());
        em.flush();
        em.clear();

        Optional<PasswordResetToken> result = tokenRepository.findByUser_IdAndUsedFalse(otherUser.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("444444");
    }
}