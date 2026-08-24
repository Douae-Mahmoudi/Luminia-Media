package com.example.notification.domain.entity;

import com.example.notification.domain.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationPreference Entity Tests")
class NotificationPreferenceTest {


    @Test
    @DisplayName("Builder crée une préférence avec tous les champs")
    void builder_shouldCreatePreferenceWithAllFields() {
        NotificationPreference preference = NotificationPreference.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(true)
                .build();

        assertThat(preference.getId()).isEqualTo(1L);
        assertThat(preference.getUserId()).isEqualTo(42L);
        assertThat(preference.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
        assertThat(preference.isInAppEnabled()).isTrue();
        assertThat(preference.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("Builder crée une préférence avec emailEnabled à false")
    void builder_shouldCreatePreferenceWithEmailDisabled() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(10L)
                .type(NotificationType.BROADCAST)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        assertThat(preference.isInAppEnabled()).isTrue();
        assertThat(preference.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("Builder crée une préférence avec inAppEnabled à false")
    void builder_shouldCreatePreferenceWithInAppDisabled() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(10L)
                .type(NotificationType.RECO_READY)
                .inAppEnabled(false)
                .emailEnabled(true)
                .build();

        assertThat(preference.isInAppEnabled()).isFalse();
        assertThat(preference.isEmailEnabled()).isTrue();
    }


    @Test
    @DisplayName("prePersist initialise inAppEnabled=true et emailEnabled=false")
    void prePersist_shouldSetDefaultValues() {
        NotificationPreference preference = new NotificationPreference();

        preference.prePersist();

        assertThat(preference.isInAppEnabled()).isTrue();
        assertThat(preference.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("prePersist écrase inAppEnabled=false par true")
    void prePersist_shouldOverrideInAppEnabledToTrue() {
        NotificationPreference preference = NotificationPreference.builder()
                .inAppEnabled(false)
                .emailEnabled(false)
                .build();

        preference.prePersist();

        assertThat(preference.isInAppEnabled()).isTrue();
    }

    @Test
    @DisplayName("prePersist écrase emailEnabled=true par false")
    void prePersist_shouldOverrideEmailEnabledToFalse() {
        NotificationPreference preference = NotificationPreference.builder()
                .inAppEnabled(true)
                .emailEnabled(true)
                .build();

        preference.prePersist();

        assertThat(preference.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("prePersist ne modifie pas userId ni type")
    void prePersist_shouldNotAffectUserIdAndType() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(99L)
                .type(NotificationType.COMMENT_ADDED)
                .build();

        preference.prePersist();

        assertThat(preference.getUserId()).isEqualTo(99L);
        assertThat(preference.getType()).isEqualTo(NotificationType.COMMENT_ADDED);
    }


    @Test
    @DisplayName("NoArgsConstructor crée une préférence vide")
    void noArgsConstructor_shouldCreateEmptyPreference() {
        NotificationPreference preference = new NotificationPreference();

        assertThat(preference.getId()).isNull();
        assertThat(preference.getUserId()).isNull();
        assertThat(preference.getType()).isNull();
        assertThat(preference.isInAppEnabled()).isFalse();
        assertThat(preference.isEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        NotificationPreference preference = new NotificationPreference(
                1L,
                20L,
                NotificationType.MEDIA_ACCEPTED,
                true,
                false
        );

        assertThat(preference.getId()).isEqualTo(1L);
        assertThat(preference.getUserId()).isEqualTo(20L);
        assertThat(preference.getType()).isEqualTo(NotificationType.MEDIA_ACCEPTED);
        assertThat(preference.isInAppEnabled()).isTrue();
        assertThat(preference.isEmailEnabled()).isFalse();
    }


    @Test
    @DisplayName("setEmailEnabled peut activer l'email après création")
    void setEmailEnabled_shouldEnableEmail() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(5L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        preference.setEmailEnabled(true);

        assertThat(preference.isEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("setInAppEnabled peut désactiver l'inApp")
    void setInAppEnabled_shouldDisableInApp() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(5L)
                .type(NotificationType.MEDIA_REFUSED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        preference.setInAppEnabled(false);

        assertThat(preference.isInAppEnabled()).isFalse();
    }


    @Test
    @DisplayName("Préférence pour MEDIA_LIKED")
    void builder_shouldCreatePreferenceForMediaLiked() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(1L).type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(preference.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
    }

    @Test
    @DisplayName("Préférence pour MEDIA_REFUSED")
    void builder_shouldCreatePreferenceForMediaRefused() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(1L).type(NotificationType.MEDIA_REFUSED)
                .inAppEnabled(true).emailEnabled(true).build();

        assertThat(preference.getType()).isEqualTo(NotificationType.MEDIA_REFUSED);
    }

    @Test
    @DisplayName("Préférence pour COMMENT_ADDED")
    void builder_shouldCreatePreferenceForCommentAdded() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(1L).type(NotificationType.COMMENT_ADDED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(preference.getType()).isEqualTo(NotificationType.COMMENT_ADDED);
    }

    @Test
    @DisplayName("Préférence pour MEDIA_ADDED_TO_COLLECTION")
    void builder_shouldCreatePreferenceForMediaAddedToCollection() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(1L).type(NotificationType.MEDIA_ADDED_TO_COLLECTION)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(preference.getType()).isEqualTo(NotificationType.MEDIA_ADDED_TO_COLLECTION);
    }

    @Test
    @DisplayName("Préférence pour RECO_READY")
    void builder_shouldCreatePreferenceForRecoReady() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(1L).type(NotificationType.RECO_READY)
                .inAppEnabled(true).emailEnabled(true).build();

        assertThat(preference.getType()).isEqualTo(NotificationType.RECO_READY);
    }

    @Test
    @DisplayName("Préférence pour BROADCAST")
    void builder_shouldCreatePreferenceForBroadcast() {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(1L).type(NotificationType.BROADCAST)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(preference.getType()).isEqualTo(NotificationType.BROADCAST);
    }


    @Test
    @DisplayName("Deux préférences identiques sont égales")
    void equals_shouldReturnTrueForIdenticalPreferences() {
        NotificationPreference p1 = NotificationPreference.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        NotificationPreference p2 = NotificationPreference.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    @DisplayName("Deux préférences avec types différents ne sont pas égales")
    void equals_shouldReturnFalseForDifferentTypes() {
        NotificationPreference p1 = NotificationPreference.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        NotificationPreference p2 = NotificationPreference.builder()
                .id(2L).userId(1L)
                .type(NotificationType.BROADCAST)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    @DisplayName("Deux préférences avec userId différents ne sont pas égales")
    void equals_shouldReturnFalseForDifferentUsers() {
        NotificationPreference p1 = NotificationPreference.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        NotificationPreference p2 = NotificationPreference.builder()
                .id(1L).userId(99L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true).emailEnabled(false).build();

        assertThat(p1).isNotEqualTo(p2);
    }
}

