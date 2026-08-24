package com.mediatheque.auth_svc.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {


    private User buildUser() {
        return User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("hashed-password")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void builder_shouldCreateUser_withAllFields() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("hashed-password")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .createdAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPassword()).isEqualTo("hashed-password");
        assertThat(user.getFirstName()).isEqualTo("Alice");
        assertThat(user.getLastName()).isEqualTo("Dupont");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyUser() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();

        User user = new User(1L, "alice@example.com", "pwd", "Alice", "Dupont",
                Role.ADMIN, true, now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }



    @Test
    void getUsername_shouldReturnEmail() {
        User user = buildUser();

        assertThat(user.getUsername()).isEqualTo("alice@example.com");
    }


    @Test
    void getAuthorities_shouldReturnRoleUser_withPrefix() {
        User user = buildUser(); // role = USER

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void getAuthorities_shouldReturnRoleAdmin_withPrefix() {
        User user = User.builder()
                .email("admin@example.com")
                .password("pwd")
                .firstName("Bob")
                .lastName("Admin")
                .role(Role.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }


    @Test
    void isAccountNonExpired_shouldAlwaysReturnTrue() {
        assertThat(buildUser().isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLocked_shouldAlwaysReturnTrue() {
        assertThat(buildUser().isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpired_shouldAlwaysReturnTrue() {
        assertThat(buildUser().isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnTrue_whenEnabledIsTrue() {
        User user = buildUser(); // enabled = true
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenEnabledIsFalse() {
        User user = buildUser();
        user.setEnabled(false);

        assertThat(user.isEnabled()).isFalse();
    }


    @Test
    void prePersist_shouldSetCreatedAt() {
        User user = User.builder()
                .email("alice@example.com")
                .password("pwd")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(false)
                .build();

        assertThat(user.getCreatedAt()).isNull(); // avant persist

        user.prePersist();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }


    @Test
    void setRole_shouldUpdateRole() {
        User user = buildUser(); // role = USER
        user.setRole(Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void setEmail_shouldUpdateEmail() {
        User user = buildUser();
        user.setEmail("new@example.com");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getUsername()).isEqualTo("new@example.com"); // getUsername suit l'email
    }


    @Test
    void twoUsers_withSameFields_shouldBeEqual() {
        LocalDateTime now = LocalDateTime.now();

        User u1 = User.builder().id(1L).email("alice@example.com")
                .password("pwd").firstName("Alice").lastName("Dupont")
                .role(Role.USER).enabled(true).createdAt(now).build();

        User u2 = User.builder().id(1L).email("alice@example.com")
                .password("pwd").firstName("Alice").lastName("Dupont")
                .role(Role.USER).enabled(true).createdAt(now).build();

        assertThat(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
    }

    @Test
    void twoUsers_withDifferentEmails_shouldNotBeEqual() {
        User u1 = buildUser();
        User u2 = buildUser();
        u2.setEmail("other@example.com");

        assertThat(u1).isNotEqualTo(u2);
    }


    @Test
    void toString_shouldContainEmail() {
        User user = buildUser();

        assertThat(user.toString()).contains("alice@example.com");
    }
}