package com.example.notification.Integration;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.response.BadgeCountResponse;
import com.example.notification.dto.response.NotificationResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.repository.NotificationPreferenceRepository;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.service.NotificationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("[IT] NotificationService – End-to-End")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.enabled=false"
})
class NotificationServiceIT {

    @Autowired private NotificationService notificationService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationPreferenceRepository preferenceRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        preferenceRepository.deleteAll();
    }

    @Nested
    @DisplayName("send() – persistance")
    class SendPersistence {

        @Test
        @DisplayName("persiste la notification en base avec les bons champs")
        void persistsNotificationWithCorrectFields() {
            notificationService.send(1L, NotificationType.MEDIA_LIKED,
                    "Tu as eu un like", 42L, ReferenceType.MEDIA);

            List<Notification> all = notificationRepository.findAll();
            assertThat(all).hasSize(1);
            assertThat(all.get(0).getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("utilise la préférence existante (ne la persiste pas en double)")
        void usesExistingPreferenceWithoutDuplication() {
            preferenceRepository.save(NotificationPreference.builder()
                    .userId(1L)
                    .type(NotificationType.COMMENT_ADDED)
                    .inAppEnabled(true)
                    .emailEnabled(true)
                    .build());

            notificationService.send(1L, NotificationType.COMMENT_ADDED,
                    "Nouveau commentaire", 10L, ReferenceType.COMMENT);

            assertThat(preferenceRepository.findByUserId(1L)).hasSize(1);
        }

        @Test
        @DisplayName("persiste une préférence par défaut lors du premier envoi")
        void persistsDefaultPreferenceOnFirstSend() {
            notificationService.send(1L, NotificationType.RECO_READY,
                    "Reco prête", null, ReferenceType.SYSTEM);

            List<NotificationPreference> prefs = preferenceRepository.findAll();
            assertThat(prefs).hasSize(1);
            assertThat(prefs.get(0).getType()).isEqualTo(NotificationType.RECO_READY);
            assertThat(prefs.get(0).getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getMyNotifications()")
    class GetMyNotifications {
        @Test
        void returnsSortedNotificationsForUser() {
            notificationService.send(1L, NotificationType.MEDIA_LIKED, "msg1", 1L, ReferenceType.MEDIA);
            notificationService.send(1L, NotificationType.BROADCAST, "msg2", null, ReferenceType.SYSTEM);

            PageResponse<NotificationResponse> page = notificationService.getMyNotifications(
                    1L, PageRequest.of(0, 10, Sort.by("createdAt").descending()));

            assertThat(page.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getBadgeCount()")
    class GetBadgeCount {
        @Test
        void returnsCorrectUnreadCount() {
            notificationService.send(1L, NotificationType.MEDIA_LIKED, "m1", 1L, ReferenceType.MEDIA);
            BadgeCountResponse badge = notificationService.getBadgeCount(1L);
            assertThat(badge.getUnreadCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {
        @Test
        void marksNotificationAsReadInDb() {
            notificationService.send(1L, NotificationType.MEDIA_LIKED, "msg", 1L, ReferenceType.MEDIA);
            Notification notif = notificationRepository.findAll().get(0);

            notificationService.markAsRead(notif.getId(), 1L);

            Notification updated = notificationRepository.findById(notif.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(NotificationStatus.READ);
        }
    }

    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsRead {
        @Test
        void marksAllNotificationsRead() {
            notificationService.send(1L, NotificationType.MEDIA_LIKED, "m1", 1L, ReferenceType.MEDIA);
            notificationService.markAllAsRead(1L);
            assertThat(notificationService.getBadgeCount(1L).getUnreadCount()).isEqualTo(0);
        }
    }
}