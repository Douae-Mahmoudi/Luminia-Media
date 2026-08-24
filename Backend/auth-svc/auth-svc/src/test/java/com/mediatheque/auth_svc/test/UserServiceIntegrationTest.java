package com.mediatheque.auth_svc.test;

import com.mediatheque.auth_svc.dto.UserProfileDto;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.UserRepository;
import com.mediatheque.auth_svc.repository.PasswordResetTokenRepository;
import com.mediatheque.auth_svc.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAllInBatch();

        userRepository.deleteAllInBatch();

        savedUser = userRepository.save(User.builder()
                .email("alice@example.com")
                .password("hashed")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .enabled(true)
                .build());

        userRepository.flush();
    }


    @Test
    void getProfile_shouldReturnProfile_whenUserExists() {
        UserProfileDto profile = userService.getProfile("alice@example.com");
        assertThat(profile.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void getProfile_shouldThrow_whenUserNotFound() {
        assertThrows(RuntimeException.class,
                () -> userService.getProfile("nobody@example.com"));
    }


    @Test
    void updateProfile_shouldUpdateFirstAndLastName() {
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName("Alicia");
        dto.setLastName("Martin");

        UserProfileDto updated = userService.updateProfile("alice@example.com", dto);

        assertThat(updated.getFirstName()).isEqualTo("Alicia");
        User inDb = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(inDb.getFirstName()).isEqualTo("Alicia");
    }


    @Test
    void getAllUsers_shouldReturnAllUsers() {
        userRepository.save(User.builder()
                .email("bob@example.com")
                .password("hashed")
                .firstName("Bob")
                .lastName("Martin")
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        List<UserProfileDto> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserProfileDto::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        tokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        assertThat(userService.getAllUsers()).isEmpty();
    }


    @Test
    void updateUserRole_shouldChangeRoleToAdmin() {
        UserProfileDto updated = userService.updateUserRole(savedUser.getId(), "ADMIN");
        assertThat(updated.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void updateUserRole_shouldThrow_whenRoleIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRole(savedUser.getId(), "SUPERADMIN"));
    }
}