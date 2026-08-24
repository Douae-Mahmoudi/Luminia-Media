package com.example.notification.dto.response;

import com.example.notification.domain.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PreferenceResponse Tests")
class PreferenceResponseTest {

    @Test
    @DisplayName("Builder crée un objet avec tous les champs")
    void builder_shouldCreateResponseWithAllFields() {
        PreferenceResponse response = PreferenceResponse.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(42L);
        assertThat(response.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
        assertThat(response.isInAppEnabled()).isTrue();
        assertThat(response.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet vide")
    void noArgsConstructor_shouldCreateEmptyResponse() {
        PreferenceResponse response = new PreferenceResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getType()).isNull();
        assertThat(response.isInAppEnabled()).isFalse();
        assertThat(response.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        PreferenceResponse response = new PreferenceResponse(
                1L, 10L, NotificationType.BROADCAST, true, true
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getType()).isEqualTo(NotificationType.BROADCAST);
        assertThat(response.isInAppEnabled()).isTrue();
        assertThat(response.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("Setters fonctionnent correctement")
    void setters_shouldWorkCorrectly() {
        PreferenceResponse response = new PreferenceResponse();
        response.setId(5L);
        response.setUserId(20L);
        response.setType(NotificationType.RECO_READY);
        response.setInAppEnabled(true);
        response.setEmailEnabled(false);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getUserId()).isEqualTo(20L);
        assertThat(response.getType()).isEqualTo(NotificationType.RECO_READY);
        assertThat(response.isInAppEnabled()).isTrue();
        assertThat(response.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("inAppEnabled=true, emailEnabled=false")
    void shouldSupportInAppOnlyEnabled() {
        PreferenceResponse response = PreferenceResponse.builder()
                .type(NotificationType.COMMENT_ADDED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(response.isInAppEnabled()).isTrue();
        assertThat(response.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("inAppEnabled=false, emailEnabled=true")
    void shouldSupportEmailOnlyEnabled() {
        PreferenceResponse response = PreferenceResponse.builder()
                .type(NotificationType.MEDIA_ACCEPTED)
                .inAppEnabled(false).emailEnabled(true).build();

        assertThat(response.isInAppEnabled()).isFalse();
        assertThat(response.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("Les deux canaux désactivés")
    void shouldSupportBothDisabled() {
        PreferenceResponse response = PreferenceResponse.builder()
                .type(NotificationType.MEDIA_REFUSED)
                .inAppEnabled(false).emailEnabled(false).build();

        assertThat(response.isInAppEnabled()).isFalse();
        assertThat(response.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("Les deux canaux activés")
    void shouldSupportBothEnabled() {
        PreferenceResponse response = PreferenceResponse.builder()
                .type(NotificationType.MEDIA_ADDED_TO_COLLECTION)
                .inAppEnabled(true).emailEnabled(true).build();

        assertThat(response.isInAppEnabled()).isTrue();
        assertThat(response.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("Deux responses identiques sont égales")
    void equals_shouldReturnTrueForIdenticalResponses() {
        PreferenceResponse r1 = PreferenceResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        PreferenceResponse r2 = PreferenceResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux responses différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentResponses() {
        PreferenceResponse r1 = PreferenceResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        PreferenceResponse r2 = PreferenceResponse.builder()
                .id(2L).userId(2L).type(NotificationType.BROADCAST)
                .inAppEnabled(false).emailEnabled(true).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}