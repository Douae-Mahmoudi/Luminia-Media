package com.example.notification.Integration;

import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("[IT] NotificationPreferenceRepository")
class NotificationPreferenceRepositoryIT {

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @BeforeEach
    void setUp() {
        preferenceRepository.deleteAll();
    }


    private NotificationPreference save(Long userId, NotificationType type,
                                        boolean inApp, boolean email) {
        return preferenceRepository.save(
                NotificationPreference.builder()
                        .userId(userId)
                        .type(type)
                        .inAppEnabled(inApp)
                        .emailEnabled(email)
                        .build());
    }



    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("retourne toutes les préférences d'un user")
        void returnsAllUserPreferences() {
            save(1L, NotificationType.MEDIA_LIKED, true, false);
            save(1L, NotificationType.BROADCAST, false, true);
            save(2L, NotificationType.MEDIA_LIKED, true, true); // autre user

            List<NotificationPreference> result = preferenceRepository.findByUserId(1L);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(p -> p.getUserId().equals(1L));
        }

        @Test
        @DisplayName("retourne liste vide si l'user n'a pas de préférences")
        void emptyListWhenNoneExist() {
            List<NotificationPreference> result = preferenceRepository.findByUserId(999L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retourne uniquement les préférences de l'user ciblé")
        void isolatedByUser() {
            save(1L, NotificationType.MEDIA_LIKED, true, false);
            save(2L, NotificationType.BROADCAST, false, true);

            assertThat(preferenceRepository.findByUserId(1L)).hasSize(1);
            assertThat(preferenceRepository.findByUserId(2L)).hasSize(1);
        }
    }


    @Nested
    @DisplayName("findByUserIdAndType()")
    class FindByUserIdAndType {

        @Test
        @DisplayName("retourne la préférence si elle existe")
        void returnsExistingPreference() {
            save(1L, NotificationType.MEDIA_LIKED, true, false);

            Optional<NotificationPreference> result =
                    preferenceRepository.findByUserIdAndType(1L, NotificationType.MEDIA_LIKED);

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(1L);
            assertThat(result.get().getType()).isEqualTo(NotificationType.MEDIA_LIKED);
            assertThat(result.get().isInAppEnabled()).isTrue();  // @PrePersist force true
            assertThat(result.get().isEmailEnabled()).isFalse(); // @PrePersist force false
        }

        @Test
        @DisplayName("retourne Optional.empty() si la préférence n'existe pas")
        void returnsEmptyWhenNotFound() {
            Optional<NotificationPreference> result =
                    preferenceRepository.findByUserIdAndType(1L, NotificationType.BROADCAST);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("ne retourne pas la préférence d'un autre user pour le même type")
        void doesNotReturnOtherUserPreference() {
            save(2L, NotificationType.MEDIA_LIKED, false, false);

            Optional<NotificationPreference> result =
                    preferenceRepository.findByUserIdAndType(1L, NotificationType.MEDIA_LIKED);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("ne retourne pas la préférence du même user pour un autre type")
        void doesNotReturnDifferentType() {
            save(1L, NotificationType.MEDIA_LIKED, true, false);

            Optional<NotificationPreference> result =
                    preferenceRepository.findByUserIdAndType(1L, NotificationType.BROADCAST);

            assertThat(result).isEmpty();
        }
    }


    @Nested
    @DisplayName("Contrainte unique (userId, type)")
    class UniqueConstraint {

        @Test
        @DisplayName("lève une exception si on insère deux fois le même (userId, type)")
        void throwsOnDuplicateUserIdAndType() {
            save(1L, NotificationType.MEDIA_LIKED, true, false);

            assertThatThrownBy(() ->
                    save(1L, NotificationType.MEDIA_LIKED, false, true)
            ).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("autorise le même type pour deux users différents")
        void allowsSameTypeForDifferentUsers() {
            assertThatCode(() -> {
                save(1L, NotificationType.MEDIA_LIKED, true, false);
                save(2L, NotificationType.MEDIA_LIKED, false, true);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("autorise le même user avec des types différents")
        void allowsDifferentTypesForSameUser() {
            assertThatCode(() -> {
                save(1L, NotificationType.MEDIA_LIKED, true, false);
                save(1L, NotificationType.BROADCAST, false, true);
                save(1L, NotificationType.COMMENT_ADDED, true, true);
            }).doesNotThrowAnyException();
        }
    }


    @Nested
    @DisplayName("@PrePersist")
    class PrePersistBehavior {

        @Test
        @DisplayName("inAppEnabled=true et emailEnabled=false par défaut via @PrePersist si non spécifiés")
        void setsDefaultsViaPersist() {
            NotificationPreference pref = NotificationPreference.builder()
                    .userId(1L)
                    .type(NotificationType.RECO_READY)
                    .build();

            NotificationPreference saved = preferenceRepository.save(pref);

            assertThat(saved.isInAppEnabled()).isTrue();
            assertThat(saved.isEmailEnabled()).isFalse();
        }
    }


    @Nested
    @DisplayName("Mise à jour (upsert)")
    class Update {

        @Test
        @DisplayName("mise à jour des flags fonctionne correctement")
        void updatesFlagsCorrectly() {
            NotificationPreference pref = save(1L, NotificationType.BROADCAST, true, false);

            pref.setInAppEnabled(false);
            pref.setEmailEnabled(true);
            preferenceRepository.save(pref);

            NotificationPreference updated =
                    preferenceRepository.findByUserIdAndType(1L, NotificationType.BROADCAST).orElseThrow();

            assertThat(updated.isInAppEnabled()).isFalse();
            assertThat(updated.isEmailEnabled()).isTrue();
        }

        @Test
        @DisplayName("un seul enregistrement après upsert")
        void singleRecordAfterUpsert() {
            NotificationPreference pref = save(1L, NotificationType.BROADCAST, true, false);
            pref.setEmailEnabled(true);
            preferenceRepository.save(pref);

            List<NotificationPreference> result = preferenceRepository.findByUserId(1L);
            assertThat(result).hasSize(1);
        }
    }
}
