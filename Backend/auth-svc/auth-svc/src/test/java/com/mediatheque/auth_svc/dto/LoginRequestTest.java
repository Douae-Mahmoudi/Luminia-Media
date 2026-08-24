package com.mediatheque.auth_svc.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    @Test
    void setters_shouldSetAllFields() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        assertThat(request.getEmail()).isEqualTo("alice@example.com");
        assertThat(request.getPassword()).isEqualTo("secret123");
    }

    @Test
    void defaultConstructor_shouldCreateObjectWithNullFields() {
        LoginRequest request = new LoginRequest();

        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
    }

    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        LoginRequest r1 = new LoginRequest();
        r1.setEmail("alice@example.com");
        r1.setPassword("secret123");

        LoginRequest r2 = new LoginRequest();
        r2.setEmail("alice@example.com");
        r2.setPassword("secret123");

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentEmail() {
        LoginRequest r1 = new LoginRequest();
        r1.setEmail("alice@example.com");
        r1.setPassword("secret123");

        LoginRequest r2 = new LoginRequest();
        r2.setEmail("bob@example.com");
        r2.setPassword("secret123");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentPassword() {
        LoginRequest r1 = new LoginRequest();
        r1.setEmail("alice@example.com");
        r1.setPassword("secret123");

        LoginRequest r2 = new LoginRequest();
        r2.setEmail("alice@example.com");
        r2.setPassword("autrePassword");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void toString_shouldContainEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        assertThat(request.toString()).contains("alice@example.com");
    }
}