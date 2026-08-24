package com.mediatheque.auth_svc.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForgotPasswordRequestTest {

    @Test
    void setter_shouldSetEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("alice@example.com");

        assertThat(request.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void defaultConstructor_shouldCreateObjectWithNullEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();

        assertThat(request.getEmail()).isNull();
    }

    @Test
    void equals_shouldReturnTrue_whenSameEmail() {
        ForgotPasswordRequest r1 = new ForgotPasswordRequest();
        r1.setEmail("alice@example.com");

        ForgotPasswordRequest r2 = new ForgotPasswordRequest();
        r2.setEmail("alice@example.com");

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentEmail() {
        ForgotPasswordRequest r1 = new ForgotPasswordRequest();
        r1.setEmail("alice@example.com");

        ForgotPasswordRequest r2 = new ForgotPasswordRequest();
        r2.setEmail("bob@example.com");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void toString_shouldContainEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("alice@example.com");

        assertThat(request.toString()).contains("alice@example.com");
    }
}