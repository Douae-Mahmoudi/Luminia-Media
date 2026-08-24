package com.mediatheque.auth_svc.service;

import com.mediatheque.auth_svc.dto.UserProfileDto;
import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .build();
    }


    @Test
    void getProfile_shouldReturnDto_whenUserExists() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserProfileDto result = userService.getProfile("alice@example.com");

        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getLastName()).isEqualTo("Dupont");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void getProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }


    @Test
    void updateProfile_shouldUpdateAndReturnDto() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileDto dto = UserProfileDto.builder()
                .firstName("Alicia")
                .lastName("Martin")
                .build();

        UserProfileDto result = userService.updateProfile("alice@example.com", dto);

        assertThat(result.getFirstName()).isEqualTo("Alicia");
        assertThat(result.getLastName()).isEqualTo("Martin");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        UserProfileDto dto = UserProfileDto.builder().firstName("X").lastName("Y").build();

        assertThatThrownBy(() -> userService.updateProfile("ghost@example.com", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any());
    }



    @Test
    void getAllUsers_shouldReturnMappedList() {
        User second = User.builder()
                .id(2L)
                .email("bob@example.com")
                .firstName("Bob")
                .lastName("Leroy")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user, second));

        List<UserProfileDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("alice@example.com");
        assertThat(result.get(1).getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserProfileDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }


    @Test
    void updateUserRole_shouldChangeRoleAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileDto result = userService.updateUserRole(1L, "admin");

        assertThat(result.getRole()).isEqualTo("ADMIN");
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(99L, "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_shouldThrow_whenRoleIsInvalid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUserRole(1L, "SUPERUSER"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}