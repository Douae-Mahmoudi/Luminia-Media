package com.mediatheque.auth_svc.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetTokenTest {

    private User buildUser() {
        return User.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Dupont")
                .build();
    }

    private PasswordResetToken buildToken() {
        return PasswordResetToken.builder()
                .id(1L)
                .code("123456")
                .user(buildUser())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
    }

    @Test
    void builder_shouldCreateToken_withAllFields() {
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        User user = buildUser();

        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .code("123456")
                .user(user)
                .expiresAt(expiry)
                .used(false)
                .build();

        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getCode()).isEqualTo("123456");
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getExpiresAt()).isEqualTo(expiry);
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyToken() {
        PasswordResetToken token = new PasswordResetToken();

        assertThat(token.getId()).isNull();
        assertThat(token.getCode()).isNull();
        assertThat(token.getUser()).isNull();
        assertThat(token.getExpiresAt()).isNull();
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        LocalDateTime expiry = LocalDateTime.now().plusHours(2);
        User user = buildUser();

        PasswordResetToken token = new PasswordResetToken(1L, "654321", user, expiry, false);

        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getCode()).isEqualTo("654321");
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getExpiresAt()).isEqualTo(expiry);
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void setUsed_shouldMarkTokenAsUsed() {
        PasswordResetToken token = buildToken();
        assertThat(token.isUsed()).isFalse();

        token.setUsed(true);

        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void setCode_shouldUpdateCodeValue() {
        PasswordResetToken token = buildToken();

        token.setCode("999999");

        assertThat(token.getCode()).isEqualTo("999999");
    }

    @Test
    void setExpiresAt_shouldUpdateExpiry() {
        PasswordResetToken token = buildToken();
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(1);

        token.setExpiresAt(newExpiry);

        assertThat(token.getExpiresAt()).isEqualTo(newExpiry);
    }

    @Test
    void setUser_shouldUpdateUser() {
        PasswordResetToken token = buildToken();
        User newUser = User.builder()
                .id(2L)
                .email("bob@example.com")
                .build();

        token.setUser(newUser);

        assertThat(token.getUser().getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void token_shouldNotBeExpired_whenExpiresAtIsInFuture() {
        PasswordResetToken token = buildToken();

        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void token_shouldBeExpired_whenExpiresAtIsInPast() {
        PasswordResetToken token = PasswordResetToken.builder()
                .code("000000")
                .user(buildUser())
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        assertThat(token.getExpiresAt()).isBefore(LocalDateTime.now());
    }

    @Test
    void token_shouldBeUsable_whenNotUsedAndNotExpired() {
        PasswordResetToken token = buildToken();

        boolean isUsable = !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());

        assertThat(isUsable).isTrue();
    }

    @Test
    void token_shouldNotBeUsable_whenAlreadyUsed() {
        PasswordResetToken token = buildToken();
        token.setUsed(true);

        boolean isUsable = !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());

        assertThat(isUsable).isFalse();
    }

    @Test
    void token_shouldNotBeUsable_whenExpiredButNotUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .code("111111")
                .user(buildUser())
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .used(false)
                .build();

        boolean isUsable = !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());

        assertThat(isUsable).isFalse();
    }

    @Test
    void twoTokens_withSameFields_shouldBeEqual() {
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        User user = buildUser();

        PasswordResetToken t1 = PasswordResetToken.builder()
                .id(1L).code("123456").user(user).expiresAt(expiry).used(false).build();

        PasswordResetToken t2 = PasswordResetToken.builder()
                .id(1L).code("123456").user(user).expiresAt(expiry).used(false).build();

        assertThat(t1).isEqualTo(t2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    void twoTokens_withDifferentCodes_shouldNotBeEqual() {
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        User user = buildUser();

        PasswordResetToken t1 = PasswordResetToken.builder()
                .id(1L).code("123456").user(user).expiresAt(expiry).used(false).build();

        PasswordResetToken t2 = PasswordResetToken.builder()
                .id(1L).code("654321").user(user).expiresAt(expiry).used(false).build();

        assertThat(t1).isNotEqualTo(t2);
    }

    @Test
    void toString_shouldContainCodeValue() {
        PasswordResetToken token = buildToken();

        assertThat(token.toString()).contains("123456");
    }
}