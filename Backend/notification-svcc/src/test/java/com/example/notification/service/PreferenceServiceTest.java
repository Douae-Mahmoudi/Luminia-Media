package com.example.notification.service;

import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.dto.request.PreferenceUpdateRequest;
import com.example.notification.dto.response.PreferenceResponse;
import com.example.notification.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PreferenceService")
class PreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    private PreferenceService service;

    @BeforeEach
    void setUp() {
        service = new PreferenceService(preferenceRepository);
    }


    @Nested
    @DisplayName("getMyPreferences()")
    class GetMyPreferencesTests {

        @Test
        @DisplayName("retourne les préférences existantes de l'utilisateur")
        void getMyPreferences_returnsExistingPreferences() {
            NotificationPreference pref1 = buildPref(1L, 10L, NotificationType.MEDIA_LIKED, true, false);
            NotificationPreference pref2 = buildPref(2L, 10L, NotificationType.BROADCAST, false, true);
            when(preferenceRepository.findByUserId(10L)).thenReturn(List.of(pref1, pref2));

            List<PreferenceResponse> result = service.getMyPreferences(10L);

            assertThat(result).hasSize(2);

            PreferenceResponse r1 = result.get(0);
            assertThat(r1.getId()).isEqualTo(1L);
            assertThat(r1.getUserId()).isEqualTo(10L);
            assertThat(r1.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
            assertThat(r1.isInAppEnabled()).isTrue();
            assertThat(r1.isEmailEnabled()).isFalse();

            PreferenceResponse r2 = result.get(1);
            assertThat(r2.getType()).isEqualTo(NotificationType.BROADCAST);
            assertThat(r2.isInAppEnabled()).isFalse();
            assertThat(r2.isEmailEnabled()).isTrue();
        }

        @Test
        @DisplayName("retourne des préférences par défaut pour tous les types si aucune n'existe")
        void getMyPreferences_returnsDefaultsWhenNoneExist() {
            when(preferenceRepository.findByUserId(99L)).thenReturn(List.of());

            List<PreferenceResponse> result = service.getMyPreferences(99L);

            int expectedCount = NotificationType.values().length;
            assertThat(result).hasSize(expectedCount);

            assertThat(result).allSatisfy(r -> {
                assertThat(r.getUserId()).isEqualTo(99L);
                assertThat(r.isInAppEnabled()).isTrue();
                assertThat(r.isEmailEnabled()).isFalse();
                assertThat(r.getId()).isNull();
            });
        }

        @Test
        @DisplayName("les préférences par défaut couvrent exactement tous les NotificationType")
        void getMyPreferences_defaultsCoversAllTypes() {
            when(preferenceRepository.findByUserId(99L)).thenReturn(List.of());

            List<PreferenceResponse> result = service.getMyPreferences(99L);

            List<NotificationType> returnedTypes = result.stream()
                    .map(PreferenceResponse::getType)
                    .toList();

            assertThat(returnedTypes).containsExactlyInAnyOrder(NotificationType.values());
        }

        @Test
        @DisplayName("ne touche pas au repository pour sauvegarder lors du retour des défauts")
        void getMyPreferences_doesNotPersistDefaults() {
            when(preferenceRepository.findByUserId(anyLong())).thenReturn(List.of());

            service.getMyPreferences(5L);

            verify(preferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("mappe correctement tous les champs depuis l'entité")
        void getMyPreferences_mapsAllFieldsCorrectly() {
            NotificationPreference pref = buildPref(42L, 7L, NotificationType.RECO_READY, false, true);
            when(preferenceRepository.findByUserId(7L)).thenReturn(List.of(pref));

            PreferenceResponse resp = service.getMyPreferences(7L).get(0);

            assertThat(resp.getId()).isEqualTo(42L);
            assertThat(resp.getUserId()).isEqualTo(7L);
            assertThat(resp.getType()).isEqualTo(NotificationType.RECO_READY);
            assertThat(resp.isInAppEnabled()).isFalse();
            assertThat(resp.isEmailEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("updatePreference()")
    class UpdatePreferenceTests {

        @Test
        @DisplayName("met à jour une préférence existante")
        void updatePreference_updatesExistingPreference() {
            NotificationPreference existing = buildPref(1L, 10L, NotificationType.MEDIA_LIKED, true, false);
            when(preferenceRepository.findByUserIdAndType(10L, NotificationType.MEDIA_LIKED))
                    .thenReturn(Optional.of(existing));

            NotificationPreference saved = buildPref(1L, 10L, NotificationType.MEDIA_LIKED, false, true);
            when(preferenceRepository.save(any())).thenReturn(saved);

            PreferenceUpdateRequest request = buildRequest(NotificationType.MEDIA_LIKED, false, true);

            PreferenceResponse result = service.updatePreference(10L, request);

            ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);
            verify(preferenceRepository).save(captor.capture());
            assertThat(captor.getValue().isInAppEnabled()).isFalse();
            assertThat(captor.getValue().isEmailEnabled()).isTrue();

            assertThat(result.isInAppEnabled()).isFalse();
            assertThat(result.isEmailEnabled()).isTrue();
        }

        @Test
        @DisplayName("crée une nouvelle préférence si elle n'existe pas encore")
        void updatePreference_createsNewPreferenceWhenMissing() {
            when(preferenceRepository.findByUserIdAndType(20L, NotificationType.BROADCAST))
                    .thenReturn(Optional.empty());

            NotificationPreference saved = buildPref(5L, 20L, NotificationType.BROADCAST, true, true);
            when(preferenceRepository.save(any())).thenReturn(saved);

            PreferenceUpdateRequest request = buildRequest(NotificationType.BROADCAST, true, true);
            PreferenceResponse result = service.updatePreference(20L, request);

            ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);
            verify(preferenceRepository).save(captor.capture());

            NotificationPreference built = captor.getValue();
            assertThat(built.getUserId()).isEqualTo(20L);
            assertThat(built.getType()).isEqualTo(NotificationType.BROADCAST);
            assertThat(built.isInAppEnabled()).isTrue();
            assertThat(built.isEmailEnabled()).isTrue();

            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("retourne la réponse avec les valeurs sauvegardées")
        void updatePreference_returnsResponseFromSavedEntity() {
            when(preferenceRepository.findByUserIdAndType(any(), any())).thenReturn(Optional.empty());

            NotificationPreference saved = buildPref(99L, 3L, NotificationType.COMMENT_ADDED, false, false);
            when(preferenceRepository.save(any())).thenReturn(saved);

            PreferenceUpdateRequest request = buildRequest(NotificationType.COMMENT_ADDED, false, false);
            PreferenceResponse result = service.updatePreference(3L, request);

            assertThat(result.getId()).isEqualTo(99L);
            assertThat(result.getUserId()).isEqualTo(3L);
            assertThat(result.getType()).isEqualTo(NotificationType.COMMENT_ADDED);
            assertThat(result.isInAppEnabled()).isFalse();
            assertThat(result.isEmailEnabled()).isFalse();
        }

        @Test
        @DisplayName("appelle save exactement une seule fois")
        void updatePreference_callsSaveExactlyOnce() {
            when(preferenceRepository.findByUserIdAndType(any(), any())).thenReturn(Optional.empty());
            when(preferenceRepository.save(any())).thenReturn(buildPref(1L, 1L, NotificationType.BROADCAST, true, false));

            service.updatePreference(1L, buildRequest(NotificationType.BROADCAST, true, false));

            verify(preferenceRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("applique inAppEnabled=false et emailEnabled=false correctement")
        void updatePreference_canDisableBothChannels() {
            NotificationPreference existing = buildPref(1L, 5L, NotificationType.MEDIA_ACCEPTED, true, true);
            when(preferenceRepository.findByUserIdAndType(5L, NotificationType.MEDIA_ACCEPTED))
                    .thenReturn(Optional.of(existing));

            NotificationPreference saved = buildPref(1L, 5L, NotificationType.MEDIA_ACCEPTED, false, false);
            when(preferenceRepository.save(any())).thenReturn(saved);

            PreferenceResponse result = service.updatePreference(5L,
                    buildRequest(NotificationType.MEDIA_ACCEPTED, false, false));

            assertThat(result.isInAppEnabled()).isFalse();
            assertThat(result.isEmailEnabled()).isFalse();
        }
    }

    private NotificationPreference buildPref(Long id, Long userId,
                                             NotificationType type,
                                             boolean inApp, boolean email) {
        return NotificationPreference.builder()
                .id(id)
                .userId(userId)
                .type(type)
                .inAppEnabled(inApp)
                .emailEnabled(email)
                .build();
    }

    private PreferenceUpdateRequest buildRequest(NotificationType type,
                                                 boolean inApp, boolean email) {
        return PreferenceUpdateRequest.builder()
                .type(type)
                .inAppEnabled(inApp)
                .emailEnabled(email)
                .build();
    }
}

