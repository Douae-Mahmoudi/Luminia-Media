package com.mediatheque.auth_svc.test;

import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        savedUser = userRepository.save(User.builder()
                .email("alice@example.com")
                .password("hashed")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .build());
    }


    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        Optional<User> result = userRepository.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailNotFound() {
        Optional<User> result = userRepository.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }


    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }


    @Test
    void save_shouldPersistUser_withGeneratedId() {
        User user = userRepository.save(User.builder()
                .email("bob@example.com")
                .password("hashed")
                .firstName("Bob")
                .lastName("Martin")
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        assertThat(user.getId()).isNotNull();
        assertThat(userRepository.findByEmail("bob@example.com")).isPresent();
    }

    @Test
    void save_shouldUpdateUser_whenAlreadyExists() {
        savedUser.setFirstName("Alicia");
        userRepository.save(savedUser);

        User updated = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Alicia");
    }


    @Test
    void findAll_shouldReturnAllUsers() {
        userRepository.save(User.builder()
                .email("bob@example.com")
                .password("hashed")
                .firstName("Bob")
                .lastName("Martin")
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }


    @Test
    void delete_shouldRemoveUser() {
        userRepository.delete(savedUser);

        assertThat(userRepository.findByEmail("alice@example.com")).isEmpty();
    }


    @Test
    void save_shouldFail_whenEmailDuplicated() {
        User duplicate = User.builder()
                .email("alice@example.com") // même email
                .password("other")
                .firstName("Alice2")
                .lastName("Dupont2")
                .role(Role.USER)
                .enabled(true)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    userRepository.saveAndFlush(duplicate);
                }
        );
    }
}