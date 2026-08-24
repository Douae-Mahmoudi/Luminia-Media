package com.example.notification.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BroadcastRequest Tests")
class BroadcastRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    @DisplayName("Builder crée un BroadcastRequest avec tous les champs")
    void builder_shouldCreateRequestWithAllFields() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message("Maintenance prévue ce soir à 22h")
                .title("Maintenance")
                .build();

        assertThat(request.getMessage()).isEqualTo("Maintenance prévue ce soir à 22h");
        assertThat(request.getTitle()).isEqualTo("Maintenance");
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet vide")
    void noArgsConstructor_shouldCreateEmptyRequest() {
        BroadcastRequest request = new BroadcastRequest();

        assertThat(request.getMessage()).isNull();
        assertThat(request.getTitle()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        BroadcastRequest request = new BroadcastRequest("Alerte système", "Alerte");

        assertThat(request.getMessage()).isEqualTo("Alerte système");
        assertThat(request.getTitle()).isEqualTo("Alerte");
    }

    @Test
    @DisplayName("Setter message fonctionne correctement")
    void setter_shouldSetMessage() {
        BroadcastRequest request = new BroadcastRequest();
        request.setMessage("Nouveau message");

        assertThat(request.getMessage()).isEqualTo("Nouveau message");
    }

    @Test
    @DisplayName("Setter title fonctionne correctement")
    void setter_shouldSetTitle() {
        BroadcastRequest request = new BroadcastRequest();
        request.setTitle("Nouveau titre");

        assertThat(request.getTitle()).isEqualTo("Nouveau titre");
    }


    @Test
    @DisplayName("Validation OK — message et title valides")
    void validation_shouldPassWhenAllFieldsValid() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message("Message valide")
                .title("Titre valide")
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation KO — message null")
    void validation_shouldFailWhenMessageIsNull() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message(null)
                .title("Titre valide")
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("message") &&
                        v.getMessage().equals("Le message est obligatoire")
        );
    }

    @Test
    @DisplayName("Validation KO — message vide")
    void validation_shouldFailWhenMessageIsBlank() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message("   ")
                .title("Titre valide")
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("message")
        );
    }

    @Test
    @DisplayName("Validation KO — message dépasse 500 caractères")
    void validation_shouldFailWhenMessageExceeds500Chars() {
        String longMessage = "A".repeat(501);
        BroadcastRequest request = BroadcastRequest.builder()
                .message(longMessage)
                .title("Titre valide")
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("message") &&
                        v.getMessage().equals("Le message ne peut pas dépasser 500 caractères")
        );
    }

    @Test
    @DisplayName("Validation OK — message exactement 500 caractères")
    void validation_shouldPassWhenMessageIsExactly500Chars() {
        String exactMessage = "A".repeat(500);
        BroadcastRequest request = BroadcastRequest.builder()
                .message(exactMessage)
                .title("Titre valide")
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }


    @Test
    @DisplayName("Validation KO — title null")
    void validation_shouldFailWhenTitleIsNull() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message("Message valide")
                .title(null)
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("title") &&
                        v.getMessage().equals("Le titre est obligatoire")
        );
    }

    @Test
    @DisplayName("Validation KO — title vide")
    void validation_shouldFailWhenTitleIsBlank() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message("Message valide")
                .title("   ")
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("title")
        );
    }

    @Test
    @DisplayName("Validation KO — title dépasse 100 caractères")
    void validation_shouldFailWhenTitleExceeds100Chars() {
        String longTitle = "T".repeat(101);
        BroadcastRequest request = BroadcastRequest.builder()
                .message("Message valide")
                .title(longTitle)
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("title") &&
                        v.getMessage().equals("Le titre ne peut pas dépasser 100 caractères")
        );
    }

    @Test
    @DisplayName("Validation OK — title exactement 100 caractères")
    void validation_shouldPassWhenTitleIsExactly100Chars() {
        String exactTitle = "T".repeat(100);
        BroadcastRequest request = BroadcastRequest.builder()
                .message("Message valide")
                .title(exactTitle)
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation KO — message et title tous les deux invalides")
    void validation_shouldReturnTwoViolationsWhenBothFieldsInvalid() {
        BroadcastRequest request = BroadcastRequest.builder()
                .message(null)
                .title(null)
                .build();

        Set<ConstraintViolation<BroadcastRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }


    @Test
    @DisplayName("Deux requests identiques sont égales")
    void equals_shouldReturnTrueForIdenticalRequests() {
        BroadcastRequest r1 = BroadcastRequest.builder()
                .message("msg").title("titre").build();
        BroadcastRequest r2 = BroadcastRequest.builder()
                .message("msg").title("titre").build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux requests différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentRequests() {
        BroadcastRequest r1 = BroadcastRequest.builder()
                .message("msg1").title("titre1").build();
        BroadcastRequest r2 = BroadcastRequest.builder()
                .message("msg2").title("titre2").build();

        assertThat(r1).isNotEqualTo(r2);
    }
}