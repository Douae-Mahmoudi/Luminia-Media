package com.mediatheque.auth_svc.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    @Test
    void builder_shouldCreateObjectWithAllFields() {
        AuthResponse response = AuthResponse.builder()
                .token("jwt_token")
                .role("USER")
                .message(" Inscription réussie.")
                .build();

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getMessage()).isEqualTo(" Inscription réussie.");
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        AuthResponse response = new AuthResponse();

        assertThat(response.getToken()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        AuthResponse response = new AuthResponse("jwt_token", "ADMIN", " Connexion réussie.");

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getMessage()).isEqualTo(" Connexion réussie.");
    }

    @Test
    void setters_shouldUpdateFields() {
        AuthResponse response = new AuthResponse();
        response.setToken("new_token");
        response.setRole("USER");
        response.setMessage("Erreur.");

        assertThat(response.getToken()).isEqualTo("new_token");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getMessage()).isEqualTo("Erreur.");
    }

    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        AuthResponse r1 = AuthResponse.builder()
                .token("tok").role("USER").message("ok").build();
        AuthResponse r2 = AuthResponse.builder()
                .token("tok").role("USER").message("ok").build();

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentFields() {
        AuthResponse r1 = AuthResponse.builder().token("tok1").build();
        AuthResponse r2 = AuthResponse.builder().token("tok2").build();

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void toString_shouldContainAllFields() {
        AuthResponse response = AuthResponse.builder()
                .token("jwt_token")
                .role("USER")
                .message("ok")
                .build();

        String str = response.toString();
        assertThat(str).contains("jwt_token");
        assertThat(str).contains("USER");
        assertThat(str).contains("ok");
    }
}