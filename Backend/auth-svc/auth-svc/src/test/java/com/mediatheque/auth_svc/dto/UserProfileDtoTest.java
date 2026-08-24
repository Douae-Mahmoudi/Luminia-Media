package com.mediatheque.auth_svc.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileDtoTest {

    @Test
    void builder_shouldCreateObjectWithAllFields() {
        UserProfileDto dto = UserProfileDto.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Dupont")
                .email("alice@example.com")
                .role("USER")
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Dupont");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getRole()).isEqualTo("USER");
    }

    @Test
    void noArgsConstructor_shouldCreateObjectWithNullFields() {
        UserProfileDto dto = new UserProfileDto();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getFirstName()).isNull();
        assertThat(dto.getLastName()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getRole()).isNull();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        UserProfileDto dto = new UserProfileDto(1L, "Alice", "Dupont", "alice@example.com", "USER");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Dupont");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getRole()).isEqualTo("USER");
    }

    @Test
    void setters_shouldUpdateFields() {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(2L);
        dto.setFirstName("Bob");
        dto.setLastName("Martin");
        dto.setEmail("bob@example.com");
        dto.setRole("ADMIN");

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getFirstName()).isEqualTo("Bob");
        assertThat(dto.getLastName()).isEqualTo("Martin");
        assertThat(dto.getEmail()).isEqualTo("bob@example.com");
        assertThat(dto.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        UserProfileDto d1 = UserProfileDto.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Dupont")
                .email("alice@example.com")
                .role("USER")
                .build();

        UserProfileDto d2 = UserProfileDto.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Dupont")
                .email("alice@example.com")
                .role("USER")
                .build();

        assertThat(d1).isEqualTo(d2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        UserProfileDto d1 = UserProfileDto.builder().id(1L).email("alice@example.com").build();
        UserProfileDto d2 = UserProfileDto.builder().id(2L).email("alice@example.com").build();

        assertThat(d1).isNotEqualTo(d2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentEmail() {
        UserProfileDto d1 = UserProfileDto.builder().id(1L).email("alice@example.com").build();
        UserProfileDto d2 = UserProfileDto.builder().id(1L).email("bob@example.com").build();

        assertThat(d1).isNotEqualTo(d2);
    }

    @Test
    void toString_shouldContainAllFields() {
        UserProfileDto dto = UserProfileDto.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Dupont")
                .email("alice@example.com")
                .role("USER")
                .build();

        String str = dto.toString();
        assertThat(str).contains("Alice");
        assertThat(str).contains("Dupont");
        assertThat(str).contains("alice@example.com");
        assertThat(str).contains("USER");
    }
}