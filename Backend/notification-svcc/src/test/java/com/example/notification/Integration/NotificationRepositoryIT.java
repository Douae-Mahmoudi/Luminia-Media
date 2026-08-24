package com.example.notification.Integration;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.repository.NotificationRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("[IT] NotificationRepository")
class NotificationRepositoryIT {

    @Autowired
    private NotificationRepository notificationRepository;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }


    private Notification save(Long userId, NotificationType type, NotificationStatus status) {
        Notification n = Notification.builder()
                .userId(userId)
                .type(type)
                .message("Test message")
                .referenceId(1L)
                .referenceType(ReferenceType.MEDIA)
                .build();
        Notification saved = notificationRepository.save(n);
        if (status == NotificationStatus.READ) {
            notificationRepository.markAsRead(saved.getId(), userId, LocalDateTime.now());
            em.flush();
            em.clear();
            return notificationRepository.findById(saved.getId()).orElseThrow();
        }
        return saved;
    }


    @Nested
    @DisplayName("findByUserIdOrderByCreatedAtDesc()")
    class FindByUserId {

        @Test
        @DisplayName("retourne uniquement les notifications de l'user")
        void returnsOnlyUserNotifications() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(2L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            Page<Notification> result = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("retourne page vide si aucune notification pour cet user")
        void emptyPageForUnknownUser() {
            Page<Notification> result = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(999L, PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("pagination fonctionne correctement")
        void paginationWorks() {
            for (int i = 0; i < 5; i++) save(1L, NotificationType.BROADCAST, NotificationStatus.UNREAD);

            Page<Notification> page0 = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 3));
            Page<Notification> page1 = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(1, 3));

            assertThat(page0.getContent()).hasSize(3);
            assertThat(page1.getContent()).hasSize(2);
            assertThat(page0.getTotalElements()).isEqualTo(5);
            assertThat(page0.isFirst()).isTrue();
            assertThat(page1.isLast()).isTrue();
        }
    }


    @Nested
    @DisplayName("countByUserIdAndStatus()")
    class CountByUserIdAndStatus {

        @Test
        @DisplayName("compte correctement les UNREAD")
        void countsUnread() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(1L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            save(1L, NotificationType.COMMENT_ADDED, NotificationStatus.READ);

            long count = notificationRepository.countByUserIdAndStatus(1L, NotificationStatus.UNREAD);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("isole par userId")
        void isolatedByUser() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(2L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            assertThat(notificationRepository.countByUserIdAndStatus(1L, NotificationStatus.UNREAD)).isEqualTo(1);
            assertThat(notificationRepository.countByUserIdAndStatus(2L, NotificationStatus.UNREAD)).isEqualTo(1);
        }

        @Test
        @DisplayName("retourne 0 si aucune notification UNREAD")
        void zeroWhenAllRead() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.READ);

            long count = notificationRepository.countByUserIdAndStatus(1L, NotificationStatus.UNREAD);

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("marque la notification comme lue et pose readAt")
        void setsStatusReadAndReadAt() {
            Notification notif = save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            LocalDateTime now = LocalDateTime.now();

            int updated = notificationRepository.markAsRead(notif.getId(), 1L, now);
            em.flush();
            em.clear();

            assertThat(updated).isEqualTo(1);
            Notification refreshed = notificationRepository.findById(notif.getId()).orElseThrow();
            assertThat(refreshed.getStatus()).isEqualTo(NotificationStatus.READ);
            assertThat(refreshed.getReadAt()).isNotNull();
        }

        @Test
        @DisplayName("retourne 0 si la notif n'appartient pas à l'user (userId mismatch)")
        void returnsZeroWhenUserIdMismatch() {
            Notification notif = save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            int updated = notificationRepository.markAsRead(notif.getId(), 999L, LocalDateTime.now());
            em.flush();
            em.clear();

            assertThat(updated).isEqualTo(0);
            assertThat(notificationRepository.findById(notif.getId()).orElseThrow().getStatus())
                    .isEqualTo(NotificationStatus.UNREAD);
        }

        @Test
        @DisplayName("retourne 0 si notificationId inexistant")
        void returnsZeroWhenNotifNotFound() {
            int updated = notificationRepository.markAsRead(99999L, 1L, LocalDateTime.now());
            assertThat(updated).isEqualTo(0);
        }
    }


    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsRead {

        @Test
        @DisplayName("marque toutes les UNREAD de l'user comme lues")
        void marksAllUnreadAsRead() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(1L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            save(1L, NotificationType.COMMENT_ADDED, NotificationStatus.UNREAD);

            int updated = notificationRepository.markAllAsRead(1L, LocalDateTime.now());
            em.flush();
            em.clear();

            assertThat(updated).isEqualTo(3);
            long stillUnread = notificationRepository.countByUserIdAndStatus(1L, NotificationStatus.UNREAD);
            assertThat(stillUnread).isEqualTo(0);
        }

        @Test
        @DisplayName("n'affecte pas les notifications des autres users")
        void doesNotAffectOtherUsers() {
            Notification other = save(2L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            notificationRepository.markAllAsRead(1L, LocalDateTime.now());
            em.flush();
            em.clear();

            assertThat(notificationRepository.findById(other.getId()).orElseThrow().getStatus())
                    .isEqualTo(NotificationStatus.UNREAD);
        }

        @Test
        @DisplayName("retourne 0 si aucune UNREAD à marquer")
        void returnsZeroWhenNothingToMark() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.READ);

            int updated = notificationRepository.markAllAsRead(1L, LocalDateTime.now());

            assertThat(updated).isEqualTo(0);
        }
    }



    @Nested
    @DisplayName("countByType()")
    class CountByType {

        @Test
        @DisplayName("retourne liste vide quand aucune notification")
        void emptyWhenNoNotifications() {
            List<Object[]> result = notificationRepository.countByType();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("compte correctement par type")
        void countsCorrectlyPerType() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(1L, NotificationType.BROADCAST, NotificationStatus.UNREAD);

            List<Object[]> result = notificationRepository.countByType();

            assertThat(result).hasSize(2);

            boolean foundMediaLiked = result.stream()
                    .anyMatch(r -> r[0].toString().equals("MEDIA_LIKED") && ((Long) r[1]) == 2L);
            boolean foundBroadcast = result.stream()
                    .anyMatch(r -> r[0].toString().equals("BROADCAST") && ((Long) r[1]) == 1L);

            assertThat(foundMediaLiked).isTrue();
            assertThat(foundBroadcast).isTrue();
        }
    }


    @Nested
    @DisplayName("countRead()")
    class CountRead {

        @Test
        @DisplayName("compte uniquement les notifications lues")
        void countsOnlyRead() {
            save(1L, NotificationType.MEDIA_LIKED, NotificationStatus.READ);
            save(1L, NotificationType.BROADCAST, NotificationStatus.READ);
            save(1L, NotificationType.COMMENT_ADDED, NotificationStatus.UNREAD);

            long count = notificationRepository.countRead();
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("retourne 0 si toutes UNREAD")
        void zeroWhenNoneRead() {
            save(1L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            assertThat(notificationRepository.countRead()).isEqualTo(0);
        }
    }


    @Nested
    @DisplayName("findDistinctUserIds()")
    class FindDistinctUserIds {

        @Test
        @DisplayName("retourne les userId distincts")
        void returnsDistinctUserIds() {
            save(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            save(10L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            save(20L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            List<Long> ids = notificationRepository.findDistinctUserIds();

            assertThat(ids).hasSize(2);
            assertThat(ids).containsExactlyInAnyOrder(10L, 20L);
        }

        @Test
        @DisplayName("retourne liste vide si aucune notification")
        void emptyWhenNoNotifications() {
            List<Long> ids = notificationRepository.findDistinctUserIds();
            assertThat(ids).isEmpty();
        }
    }


    @Nested
    @DisplayName("@PrePersist")
    class PrePersistBehavior {

        @Test
        @DisplayName("status est UNREAD et createdAt est renseigné après persist")
        void setsDefaultsOnPersist() {
            Notification n = Notification.builder()
                    .userId(1L)
                    .type(NotificationType.MEDIA_LIKED)
                    .message("Test")
                    .referenceType(ReferenceType.MEDIA)
                    .build();

            Notification saved = notificationRepository.save(n);

            assertThat(saved.getStatus()).isEqualTo(NotificationStatus.UNREAD);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getReadAt()).isNull();
        }
    }
}
