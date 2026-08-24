package com.collection.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidMediaId() {
        FavoriteRequest request = new FavoriteRequest();
        request.setMediaId("media1");

        Set<ConstraintViolation<FavoriteRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenMediaIdIsBlank() {
        FavoriteRequest request = new FavoriteRequest();
        request.setMediaId("");

        Set<ConstraintViolation<FavoriteRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("MediaId is required")));
    }

    @Test
    void shouldFailWhenMediaIdIsNull() {
        FavoriteRequest request = new FavoriteRequest();
        request.setMediaId(null);

        Set<ConstraintViolation<FavoriteRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("MediaId is required")));
    }

    @Test
    void shouldFailWhenMediaIdIsWhitespace() {
        FavoriteRequest request = new FavoriteRequest();
        request.setMediaId("   ");

        Set<ConstraintViolation<FavoriteRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldReturnCorrectMediaId() {
        FavoriteRequest request = new FavoriteRequest();
        request.setMediaId("media123");

        assertEquals("media123", request.getMediaId());
    }
}