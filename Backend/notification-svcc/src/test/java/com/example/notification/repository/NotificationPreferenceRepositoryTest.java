package com.example.notification.repository;

import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class NotificationPreferenceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationPreferenceRepository repository;

    private NotificationPreference pref1;
    private NotificationPreference pref2;
    private NotificationPreference pref3;

    @BeforeEach
    void setUp() {
        pref1 = NotificationPreference.builder()
                .userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        pref2 = NotificationPreference.builder()
                .userId(1L)
                .type(NotificationType.COMMENT_ADDED)
                .inAppEnabled(true)
                .emailEnabled(true)
                .build();

        pref3 = NotificationPreference.builder()
                .userId(2L)
                .type(NotificationType.RECO_READY)
                .inAppEnabled(false)
                .emailEnabled(true)
                .build();

        entityManager.persist(pref1);
        entityManager.persist(pref2);
        entityManager.persist(pref3);
        entityManager.flush();
    }



    @Test
    @DisplayName("findByUserId — returns all preferences for an existing user")
    void findByUserId_existingUser_returnsAllPreferences() {
        List<NotificationPreference> result = repository.findByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationPreference::getType)
                .containsExactlyInAnyOrder(NotificationType.MEDIA_LIKED, NotificationType.COMMENT_ADDED);
    }

    @Test
    @DisplayName("findByUserId — returns empty list for unknown user")
    void findByUserId_unknownUser_returnsEmptyList() {
        List<NotificationPreference> result = repository.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserId — does not return preferences belonging to another user")
    void findByUserId_doesNotReturnOtherUsersPreferences() {
        List<NotificationPreference> result = repository.findByUserId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(2L);
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.RECO_READY);
    }


    @Test
    @DisplayName("findByUserIdAndType — returns the preference when both user and type match")
    void findByUserIdAndType_existingCombo_returnsPreference() {
        Optional<NotificationPreference> result =
                repository.findByUserIdAndType(1L, NotificationType.MEDIA_LIKED);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getType()).isEqualTo(NotificationType.MEDIA_LIKED);
    }

    @Test
    @DisplayName("findByUserIdAndType — returns empty when user exists but type doesn't match")
    void findByUserIdAndType_wrongType_returnsEmpty() {
        Optional<NotificationPreference> result =
                repository.findByUserIdAndType(1L, NotificationType.BROADCAST);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndType — returns empty when user doesn't exist")
    void findByUserIdAndType_unknownUser_returnsEmpty() {
        Optional<NotificationPreference> result =
                repository.findByUserIdAndType(999L, NotificationType.MEDIA_LIKED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndType — returns empty when both user and type don't exist")
    void findByUserIdAndType_unknownUserAndType_returnsEmpty() {
        Optional<NotificationPreference> result =
                repository.findByUserIdAndType(999L, NotificationType.BROADCAST);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("@PrePersist — inAppEnabled defaults to true, emailEnabled to false")
    void prePersist_setsDefaultValues() {
        NotificationPreference bare = NotificationPreference.builder()
                .userId(3L)
                .type(NotificationType.BROADCAST)
                .inAppEnabled(false)
                .emailEnabled(true)
                .build();

        NotificationPreference saved = entityManager.persistFlushFind(bare);

        assertThat(saved.isInAppEnabled()).isTrue();
        assertThat(saved.isEmailEnabled()).isFalse();
    }


    @Test
    @DisplayName("Unique constraint — persisting a duplicate (userId + type) throws an exception")
    void uniqueConstraint_duplicateUserIdAndType_throwsException() {
        NotificationPreference duplicate = NotificationPreference.builder()
                .userId(1L)
                .type(NotificationType.MEDIA_LIKED) // already exists for user 1
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicate);
        });
    }
}

