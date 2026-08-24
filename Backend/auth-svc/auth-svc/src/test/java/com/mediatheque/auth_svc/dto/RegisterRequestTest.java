package com.mediatheque.auth_svc.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    void setters_shouldSetAllFields() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Dupont");
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        assertThat(request.getFirstName()).isEqualTo("Alice");
        assertThat(request.getLastName()).isEqualTo("Dupont");
        assertThat(request.getEmail()).isEqualTo("alice@example.com");
        assertThat(request.getPassword()).isEqualTo("secret123");
    }

    @Test
    void defaultConstructor_shouldCreateObjectWithNullFields() {
        RegisterRequest request = new RegisterRequest();

        assertThat(request.getFirstName()).isNull();
        assertThat(request.getLastName()).isNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
    }

    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        RegisterRequest r1 = new RegisterRequest();
        r1.setFirstName("Alice");
        r1.setLastName("Dupont");
        r1.setEmail("alice@example.com");
        r1.setPassword("secret123");

        RegisterRequest r2 = new RegisterRequest();
        r2.setFirstName("Alice");
        r2.setLastName("Dupont");
        r2.setEmail("alice@example.com");
        r2.setPassword("secret123");

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentEmail() {
        RegisterRequest r1 = new RegisterRequest();
        r1.setEmail("alice@example.com");

        RegisterRequest r2 = new RegisterRequest();
        r2.setEmail("bob@example.com");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentPassword() {
        RegisterRequest r1 = new RegisterRequest();
        r1.setEmail("alice@example.com");
        r1.setPassword("secret123");

        RegisterRequest r2 = new RegisterRequest();
        r2.setEmail("alice@example.com");
        r2.setPassword("autrePassword");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentFirstName() {
        RegisterRequest r1 = new RegisterRequest();
        r1.setFirstName("Alice");
        r1.setEmail("alice@example.com");

        RegisterRequest r2 = new RegisterRequest();
        r2.setFirstName("Bob");
        r2.setEmail("alice@example.com");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void toString_shouldContainAllFields() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Dupont");
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        String str = request.toString();
        assertThat(str).contains("Alice");
        assertThat(str).contains("Dupont");
        assertThat(str).contains("alice@example.com");
    }
}