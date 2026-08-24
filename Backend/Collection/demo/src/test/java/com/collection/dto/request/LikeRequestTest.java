package com.collection.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LikeRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidMediaId() {
        LikeRequest request = new LikeRequest();
        request.setMediaId("media1");

        Set<ConstraintViolation<LikeRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenMediaIdIsBlank() {
        LikeRequest request = new LikeRequest();
        request.setMediaId("");

        Set<ConstraintViolation<LikeRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("MediaId is required")));
    }

    @Test
    void shouldFailWhenMediaIdIsNull() {
        LikeRequest request = new LikeRequest();
        request.setMediaId(null);

        Set<ConstraintViolation<LikeRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("MediaId is required")));
    }

    @Test
    void shouldFailWhenMediaIdIsWhitespace() {
        LikeRequest request = new LikeRequest();
        request.setMediaId("   ");

        Set<ConstraintViolation<LikeRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldReturnCorrectMediaId() {
        LikeRequest request = new LikeRequest();
        request.setMediaId("media123");

        assertEquals("media123", request.getMediaId());
    }
}