package com.example.notification.dto.request;

import com.example.notification.domain.enums.NotificationType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PreferenceUpdateRequest Tests")
class PreferenceUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    @DisplayName("Builder crée un PreferenceUpdateRequest avec tous les champs")
    void builder_shouldCreateRequestWithAllFields() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        assertThat(request.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
        assertThat(request.isInAppEnabled()).isTrue();
        assertThat(request.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet avec valeurs par défaut")
    void noArgsConstructor_shouldCreateRequestWithDefaults() {
        PreferenceUpdateRequest request = new PreferenceUpdateRequest();

        assertThat(request.getType()).isNull();
        assertThat(request.isInAppEnabled()).isFalse();
        assertThat(request.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        PreferenceUpdateRequest request = new PreferenceUpdateRequest(
                NotificationType.BROADCAST, true, true
        );

        assertThat(request.getType()).isEqualTo(NotificationType.BROADCAST);
        assertThat(request.isInAppEnabled()).isTrue();
        assertThat(request.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("Setters fonctionnent correctement")
    void setters_shouldWorkCorrectly() {
        PreferenceUpdateRequest request = new PreferenceUpdateRequest();
        request.setType(NotificationType.RECO_READY);
        request.setInAppEnabled(true);
        request.setEmailEnabled(true);

        assertThat(request.getType()).isEqualTo(NotificationType.RECO_READY);
        assertThat(request.isInAppEnabled()).isTrue();
        assertThat(request.isEmailEnabled()).isTrue();
    }



    @Test
    @DisplayName("Validation OK — type non null")
    void validation_shouldPassWhenTypeIsProvided() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        Set<ConstraintViolation<PreferenceUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation KO — type null")
    void validation_shouldFailWhenTypeIsNull() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(null)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        Set<ConstraintViolation<PreferenceUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("type") &&
                        v.getMessage().equals("Le type de notification est obligatoire")
        );
    }


    @Test
    @DisplayName("Validation OK — type MEDIA_LIKED")
    void validation_shouldPassForMediaLiked() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED).inAppEnabled(true).emailEnabled(false).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Validation OK — type MEDIA_ACCEPTED")
    void validation_shouldPassForMediaAccepted() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_ACCEPTED).inAppEnabled(true).emailEnabled(false).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Validation OK — type MEDIA_REFUSED")
    void validation_shouldPassForMediaRefused() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_REFUSED).inAppEnabled(false).emailEnabled(true).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Validation OK — type COMMENT_ADDED")
    void validation_shouldPassForCommentAdded() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.COMMENT_ADDED).inAppEnabled(true).emailEnabled(true).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Validation OK — type MEDIA_ADDED_TO_COLLECTION")
    void validation_shouldPassForMediaAddedToCollection() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_ADDED_TO_COLLECTION).inAppEnabled(true).emailEnabled(false).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Validation OK — type RECO_READY")
    void validation_shouldPassForRecoReady() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.RECO_READY).inAppEnabled(true).emailEnabled(true).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Validation OK — type BROADCAST")
    void validation_shouldPassForBroadcast() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.BROADCAST).inAppEnabled(true).emailEnabled(false).build();

        assertThat(validator.validate(request)).isEmpty();
    }


    @Test
    @DisplayName("inAppEnabled=true, emailEnabled=false est valide")
    void validation_shouldPassWithInAppOnlyEnabled() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.isInAppEnabled()).isTrue();
        assertThat(request.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("inAppEnabled=false, emailEnabled=true est valide")
    void validation_shouldPassWithEmailOnlyEnabled() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(false).emailEnabled(true).build();

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.isInAppEnabled()).isFalse();
        assertThat(request.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("inAppEnabled=true, emailEnabled=true est valide")
    void validation_shouldPassWithBothEnabled() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(true).build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("inAppEnabled=false, emailEnabled=false est valide")
    void validation_shouldPassWithBothDisabled() {
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(false).emailEnabled(false).build();

        assertThat(validator.validate(request)).isEmpty();
    }



    @Test
    @DisplayName("Deux requests identiques sont égales")
    void equals_shouldReturnTrueForIdenticalRequests() {
        PreferenceUpdateRequest r1 = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        PreferenceUpdateRequest r2 = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux requests différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentRequests() {
        PreferenceUpdateRequest r1 = PreferenceUpdateRequest.builder()
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        PreferenceUpdateRequest r2 = PreferenceUpdateRequest.builder()
                .type(NotificationType.BROADCAST)
                .inAppEnabled(false).emailEnabled(true).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}