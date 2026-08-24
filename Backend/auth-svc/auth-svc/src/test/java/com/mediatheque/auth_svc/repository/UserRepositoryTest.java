package com.mediatheque.auth_svc.repository;

import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("alice@example.com")
                .password("encoded_password")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(user);
    }


    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        Optional<User> result = userRepository.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Alice");
        assertThat(result.get().getLastName()).isEqualTo("Dupont");
        assertThat(result.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailNotExists() {
        Optional<User> result = userRepository.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_shouldBeCaseSensitive() {
        Optional<User> result = userRepository.findByEmail("ALICE@EXAMPLE.COM");

        assertThat(result).isEmpty();
    }



    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        boolean exists = userRepository.existsByEmail("alice@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotExists() {
        boolean exists = userRepository.existsByEmail("unknown@example.com");

        assertThat(exists).isFalse();
    }



    @Test
    void save_shouldPersistUser() {
        User newUser = User.builder()
                .email("bob@example.com")
                .password("encoded_password")
                .firstName("Bob")
                .lastName("Martin")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByEmail("bob@example.com")).isPresent();
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        Optional<User> result = userRepository.findById(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> result = userRepository.findById(999L);

        assertThat(result).isEmpty();
    }



    @Test
    void delete_shouldRemoveUser() {
        userRepository.delete(user);

        assertThat(userRepository.findByEmail("alice@example.com")).isEmpty();
        assertThat(userRepository.existsByEmail("alice@example.com")).isFalse();
    }


    @Test
    void findAll_shouldReturnAllUsers() {
        User second = User.builder()
                .email("bob@example.com")
                .password("encoded_password")
                .firstName("Bob")
                .lastName("Martin")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(second);

        assertThat(userRepository.findAll()).hasSize(2);
    }
}