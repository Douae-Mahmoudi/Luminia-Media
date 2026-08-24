package com.mediatheque.auth_svc.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordRequestTest {

    @Test
    void setters_shouldSetAllFields() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setCode("394601");
        request.setNewPassword("nouveauMotDePasse123");

        assertThat(request.getEmail()).isEqualTo("user@example.com");
        assertThat(request.getCode()).isEqualTo("394601");
        assertThat(request.getNewPassword()).isEqualTo("nouveauMotDePasse123");
    }

    @Test
    void defaultConstructor_shouldCreateObjectWithNullFields() {
        ResetPasswordRequest request = new ResetPasswordRequest();

        assertThat(request.getEmail()).isNull();
        assertThat(request.getCode()).isNull();
        assertThat(request.getNewPassword()).isNull();
    }

    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        ResetPasswordRequest r1 = new ResetPasswordRequest();
        r1.setEmail("user@example.com");
        r1.setCode("394601");
        r1.setNewPassword("nouveauMotDePasse123");

        ResetPasswordRequest r2 = new ResetPasswordRequest();
        r2.setEmail("user@example.com");
        r2.setCode("394601");
        r2.setNewPassword("nouveauMotDePasse123");

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentEmail() {
        ResetPasswordRequest r1 = new ResetPasswordRequest();
        r1.setEmail("alice@example.com");
        r1.setCode("394601");
        r1.setNewPassword("motDePasse123");

        ResetPasswordRequest r2 = new ResetPasswordRequest();
        r2.setEmail("bob@example.com");
        r2.setCode("394601");
        r2.setNewPassword("motDePasse123");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentCode() {
        ResetPasswordRequest r1 = new ResetPasswordRequest();
        r1.setEmail("user@example.com");
        r1.setCode("111111");
        r1.setNewPassword("motDePasse123");

        ResetPasswordRequest r2 = new ResetPasswordRequest();
        r2.setEmail("user@example.com");
        r2.setCode("999999");
        r2.setNewPassword("motDePasse123");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentPassword() {
        ResetPasswordRequest r1 = new ResetPasswordRequest();
        r1.setEmail("user@example.com");
        r1.setCode("394601");
        r1.setNewPassword("motDePasse123");

        ResetPasswordRequest r2 = new ResetPasswordRequest();
        r2.setEmail("user@example.com");
        r2.setCode("394601");
        r2.setNewPassword("autreMotDePasse");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void toString_shouldContainAllFields() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setCode("394601");
        request.setNewPassword("nouveauMotDePasse123");

        String str = request.toString();
        assertThat(str).contains("user@example.com");
        assertThat(str).contains("394601");
        assertThat(str).contains("nouveauMotDePasse123");
    }
}