package com.collection.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CollectionRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        CollectionRequest request = new CollectionRequest();
        request.setName("Ma Collection");
        request.setDescription("Une description");
        request.setPublic(true);

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        CollectionRequest request = new CollectionRequest();
        request.setName("");

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Name is required")));
    }

    @Test
    void shouldFailWhenNameIsNull() {
        CollectionRequest request = new CollectionRequest();
        request.setName(null);

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameExceeds100Characters() {
        CollectionRequest request = new CollectionRequest();
        request.setName("a".repeat(101));

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Name must be between 1 and 100 characters")));
    }

    @Test
    void shouldPassWhenNameIsExactly100Characters() {
        CollectionRequest request = new CollectionRequest();
        request.setName("a".repeat(100));

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDescriptionExceeds500Characters() {
        CollectionRequest request = new CollectionRequest();
        request.setName("Ma Collection");
        request.setDescription("a".repeat(501));

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Description max 500 characters")));
    }

    @Test
    void shouldPassWhenDescriptionIsExactly500Characters() {
        CollectionRequest request = new CollectionRequest();
        request.setName("Ma Collection");
        request.setDescription("a".repeat(500));

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassWhenDescriptionIsNull() {
        CollectionRequest request = new CollectionRequest();
        request.setName("Ma Collection");
        request.setDescription(null);

        Set<ConstraintViolation<CollectionRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldDefaultIsPublicToFalse() {
        CollectionRequest request = new CollectionRequest();
        assertFalse(request.isPublic());
    }

    @Test
    void shouldSetIsPublicToTrue() {
        CollectionRequest request = new CollectionRequest();
        request.setPublic(true);
        assertTrue(request.isPublic());
    }
}