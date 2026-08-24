package com.collection.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("media1");
        request.setContent("Super film !");

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenMediaIdIsBlank() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("");
        request.setContent("Contenu");

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("MediaId is required")));
    }

    @Test
    void shouldFailWhenMediaIdIsNull() {
        CommentRequest request = new CommentRequest();
        request.setMediaId(null);
        request.setContent("Contenu");

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenContentIsBlank() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("media1");
        request.setContent("");

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content is required")));
    }

    @Test
    void shouldFailWhenContentIsNull() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("media1");
        request.setContent(null);

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenContentExceeds1000Characters() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("media1");
        request.setContent("a".repeat(1001));

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Comment must be between 1 and 1000 characters")));
    }

    @Test
    void shouldPassWhenContentIsExactly1000Characters() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("media1");
        request.setContent("a".repeat(1000));

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenBothFieldsAreBlank() {
        CommentRequest request = new CommentRequest();
        request.setMediaId("");
        request.setContent("");

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 2);
    }
}