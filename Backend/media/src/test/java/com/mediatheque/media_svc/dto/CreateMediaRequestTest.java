package com.mediatheque.media_svc.dto;

import com.mediatheque.media_svc.model.MediaType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateMediaRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    private CreateMediaRequest buildValid() {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Mon média");
        request.setAuthor("John Doe");
        request.setType(MediaType.BOOK);
        request.setDescription("Une description");
        request.setReleaseYear(2024);
        request.setGenre("Fiction");
        request.setImageUrl("https://example.com/image.jpg");
        request.setOwnerId(824036515L);
        return request;
    }


    @Test
    void validation_shouldPass_whenAllRequiredFieldsPresent() {
        CreateMediaRequest request = buildValid();
        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldPass_whenOptionalFieldsAreNull() {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Mon média");
        request.setAuthor("John Doe");
        request.setType(MediaType.BOOK);

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }


    @Test
    void validation_shouldFail_whenTitleIsNull() {
        CreateMediaRequest request = buildValid();
        request.setTitle(null);

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("title") &&
                        v.getMessage().equals("Le titre est obligatoire")
        );
    }

    @Test
    void validation_shouldFail_whenTitleIsBlank() {
        CreateMediaRequest request = buildValid();
        request.setTitle("   ");

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("title")
        );
    }

    @Test
    void validation_shouldFail_whenTitleIsEmpty() {
        CreateMediaRequest request = buildValid();
        request.setTitle("");

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("title")
        );
    }


    @Test
    void validation_shouldFail_whenAuthorIsNull() {
        CreateMediaRequest request = buildValid();
        request.setAuthor(null);

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("author") &&
                        v.getMessage().equals("L'auteur est obligatoire")
        );
    }

    @Test
    void validation_shouldFail_whenAuthorIsBlank() {
        CreateMediaRequest request = buildValid();
        request.setAuthor("   ");

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("author")
        );
    }

    @Test
    void validation_shouldFail_whenAuthorIsEmpty() {
        CreateMediaRequest request = buildValid();
        request.setAuthor("");

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("author")
        );
    }


    @Test
    void validation_shouldFail_whenTypeIsNull() {
        CreateMediaRequest request = buildValid();
        request.setType(null);

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("type") &&
                        v.getMessage().equals("Le type est obligatoire")
        );
    }

    @Test
    void validation_shouldPass_whenTypeIsBook() {
        CreateMediaRequest request = buildValid();
        request.setType(MediaType.BOOK);

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }


    @Test
    void setters_shouldUpdateFields() {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Titre");
        request.setAuthor("Auteur");
        request.setDescription("Desc");
        request.setType(MediaType.BOOK);
        request.setReleaseYear(2023);
        request.setGenre("Drame");
        request.setImageUrl("https://img.com/a.jpg");
        request.setOwnerId(999L);

        assertThat(request.getTitle()).isEqualTo("Titre");
        assertThat(request.getAuthor()).isEqualTo("Auteur");
        assertThat(request.getDescription()).isEqualTo("Desc");
        assertThat(request.getType()).isEqualTo(MediaType.BOOK);
        assertThat(request.getReleaseYear()).isEqualTo(2023);
        assertThat(request.getGenre()).isEqualTo("Drame");
        assertThat(request.getImageUrl()).isEqualTo("https://img.com/a.jpg");
        assertThat(request.getOwnerId()).isEqualTo(999L);
    }


    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        CreateMediaRequest r1 = buildValid();
        CreateMediaRequest r2 = buildValid();
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentTitle() {
        CreateMediaRequest r1 = buildValid();
        CreateMediaRequest r2 = buildValid();
        r2.setTitle("Autre titre");
        assertThat(r1).isNotEqualTo(r2);
    }


    @Test
    void toString_shouldContainKeyFields() {
        CreateMediaRequest request = buildValid();
        String str = request.toString();
        assertThat(str).contains("Mon média");
        assertThat(str).contains("John Doe");
        assertThat(str).contains("BOOK");
    }


    @Test
    void validation_shouldFail_withMultipleViolations_whenAllRequiredFieldsMissing() {
        CreateMediaRequest request = new CreateMediaRequest();

        Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(3);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("author"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }



    @Test
    void validation_shouldPass_forAllMediaTypes() {
        for (MediaType type : MediaType.values()) {
            CreateMediaRequest request = buildValid();
            request.setType(type);
            Set<ConstraintViolation<CreateMediaRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }
}