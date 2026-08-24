package com.example.notification.service;

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
import com.example.notification.sender.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationPreferenceRepository preferenceRepository;
    @Mock private NotificationSender senderA;
    @Mock private NotificationSender senderB;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(
                notificationRepository,
                preferenceRepository,
                List.of(senderA, senderB)
        );
    }

    @Nested
    @DisplayName("send()")
    class SendTests {

        @Test
        @DisplayName("utilise la préférence existante si elle existe")
        void send_usesExistingPreference() throws Exception {
            NotificationPreference existingPref = buildPref(1L, NotificationType.MEDIA_LIKED, true, true);
            when(preferenceRepository.findByUserIdAndType(1L, NotificationType.MEDIA_LIKED))
                    .thenReturn(Optional.of(existingPref));
            when(notificationRepository.save(any())).thenReturn(buildNotif(10L, 1L));

            service.send(1L, NotificationType.MEDIA_LIKED, "Liked!", 5L, ReferenceType.MEDIA);

            verify(preferenceRepository, never()).save(any());
            verify(senderA).send(any(), eq(existingPref));
            verify(senderB).send(any(), eq(existingPref));
        }

        @Test
        @DisplayName("crée une préférence par défaut si aucune n'existe")
        void send_createsDefaultPreferenceWhenMissing() {
            when(preferenceRepository.findByUserIdAndType(2L, NotificationType.RECO_READY))
                    .thenReturn(Optional.empty());

            NotificationPreference defaultPref = buildPref(2L, NotificationType.RECO_READY, true, false);
            when(preferenceRepository.save(any())).thenReturn(defaultPref);
            when(notificationRepository.save(any())).thenReturn(buildNotif(20L, 2L));

            service.send(2L, NotificationType.RECO_READY, "Reco prête", null, ReferenceType.SYSTEM);

            ArgumentCaptor<NotificationPreference> prefCaptor =
                    ArgumentCaptor.forClass(NotificationPreference.class);
            verify(preferenceRepository).save(prefCaptor.capture());

            NotificationPreference captured = prefCaptor.getValue();
            assertThat(captured.getUserId()).isEqualTo(2L);
            assertThat(captured.getType()).isEqualTo(NotificationType.RECO_READY);
            assertThat(captured.isInAppEnabled()).isTrue();
            assertThat(captured.isEmailEnabled()).isFalse();
        }

        @Test
        @DisplayName("sauvegarde la notification avec les bons champs")
        void send_savesNotificationWithCorrectFields() {
            when(preferenceRepository.findByUserIdAndType(any(), any()))
                    .thenReturn(Optional.of(buildPref(3L, NotificationType.COMMENT_ADDED, true, false)));
            when(notificationRepository.save(any())).thenReturn(buildNotif(30L, 3L));

            service.send(3L, NotificationType.COMMENT_ADDED, "Nouveau commentaire", 99L, ReferenceType.COMMENT);

            ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(notifCaptor.capture());

            Notification captured = notifCaptor.getValue();
            assertThat(captured.getUserId()).isEqualTo(3L);
            assertThat(captured.getType()).isEqualTo(NotificationType.COMMENT_ADDED);
            assertThat(captured.getMessage()).isEqualTo("Nouveau commentaire");
            assertThat(captured.getReferenceId()).isEqualTo(99L);
            assertThat(captured.getReferenceType()).isEqualTo(ReferenceType.COMMENT);
        }

        @Test
        @DisplayName("appelle chaque sender avec la notification sauvegardée")
        void send_callsEachSenderWithSavedNotification() throws Exception {
            NotificationPreference pref = buildPref(4L, NotificationType.BROADCAST, true, true);
            when(preferenceRepository.findByUserIdAndType(any(), any())).thenReturn(Optional.of(pref));

            Notification saved = buildNotif(40L, 4L);
            when(notificationRepository.save(any())).thenReturn(saved);

            service.send(4L, NotificationType.BROADCAST, "Annonce", null, ReferenceType.SYSTEM);

            verify(senderA).send(eq(saved), eq(pref));
            verify(senderB).send(eq(saved), eq(pref));
        }

        @Test
        @DisplayName("continue avec le sender suivant si l'un lève une exception")
        void send_continuesWhenOneSenderFails() throws Exception {
            when(preferenceRepository.findByUserIdAndType(any(), any()))
                    .thenReturn(Optional.of(buildPref(5L, NotificationType.MEDIA_LIKED, true, false)));
            when(notificationRepository.save(any())).thenReturn(buildNotif(50L, 5L));
            doThrow(new RuntimeException("SMTP down")).when(senderA).send(any(), any());

            assertThatCode(() ->
                    service.send(5L, NotificationType.MEDIA_LIKED, "msg", null, ReferenceType.MEDIA)
            ).doesNotThrowAnyException();

            verify(senderB).send(any(), any());
        }

        @Test
        @DisplayName("fonctionne sans referenceId (null autorisé)")
        void send_worksWithNullReferenceId() {
            when(preferenceRepository.findByUserIdAndType(any(), any()))
                    .thenReturn(Optional.of(buildPref(6L, NotificationType.BROADCAST, true, false)));
            when(notificationRepository.save(any())).thenReturn(buildNotif(60L, 6L));

            assertThatCode(() ->
                    service.send(6L, NotificationType.BROADCAST, "Broadcast", null, ReferenceType.SYSTEM)
            ).doesNotThrowAnyException();

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getReferenceId()).isNull();
        }
    }

    @Nested
    @DisplayName("getMyNotifications()")
    class GetMyNotificationsTests {

        private final Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        @Test
        @DisplayName("retourne une PageResponse correctement mappée")
        void getMyNotifications_returnsMappedPage() {
            Notification notif = buildNotif(1L, 10L);
            Page<Notification> page = new PageImpl<>(List.of(notif), pageable, 1);
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L, pageable))
                    .thenReturn(page);

            PageResponse<NotificationResponse> result = service.getMyNotifications(10L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("mappe correctement tous les champs de NotificationResponse")
        void getMyNotifications_mapsAllFields() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime readAt = now.plusMinutes(3);

            Notification notif = Notification.builder()
                    .id(7L)
                    .userId(10L)
                    .type(NotificationType.MEDIA_LIKED)
                    .status(NotificationStatus.READ)
                    .message("On a aimé ton média")
                    .referenceId(42L)
                    .referenceType(ReferenceType.MEDIA)
                    .createdAt(now)
                    .readAt(readAt)
                    .build();

            Page<Notification> page = new PageImpl<>(List.of(notif));
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L, pageable))
                    .thenReturn(page);

            PageResponse<NotificationResponse> result = service.getMyNotifications(10L, pageable);
            NotificationResponse resp = result.getContent().get(0);

            assertThat(resp.getId()).isEqualTo(7L);
            assertThat(resp.getUserId()).isEqualTo(10L);
            assertThat(resp.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
            assertThat(resp.getStatus()).isEqualTo(NotificationStatus.READ);
            assertThat(resp.getMessage()).isEqualTo("On a aimé ton média");
            assertThat(resp.getReferenceId()).isEqualTo(42L);
            assertThat(resp.getReferenceType()).isEqualTo(ReferenceType.MEDIA);
            assertThat(resp.getCreatedAt()).isEqualTo(now);
            assertThat(resp.getReadAt()).isEqualTo(readAt);
            assertThat(resp.isRead()).isTrue();
        }

        @Test
        @DisplayName("le champ read est false quand le statut est UNREAD")
        void getMyNotifications_readIsFalseWhenUnread() {
            Notification notif = buildNotif(1L, 10L);
            Page<Notification> page = new PageImpl<>(List.of(notif));
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L, pageable))
                    .thenReturn(page);

            NotificationResponse resp = service.getMyNotifications(10L, pageable).getContent().get(0);

            assertThat(resp.isRead()).isFalse();
            assertThat(resp.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        }

        @Test
        @DisplayName("retourne une page vide quand il n'y a pas de notifications")
        void getMyNotifications_returnsEmptyPage() {
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(99L, pageable))
                    .thenReturn(Page.empty(pageable));

            PageResponse<NotificationResponse> result = service.getMyNotifications(99L, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getBadgeCount()")
    class GetBadgeCountTests {

        @Test
        @DisplayName("retourne le nombre de notifications non lues")
        void getBadgeCount_returnsUnreadCount() {
            when(notificationRepository.countByUserIdAndStatus(1L, NotificationStatus.UNREAD))
                    .thenReturn(5L);

            BadgeCountResponse response = service.getBadgeCount(1L);

            assertThat(response.getUnreadCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("retourne 0 si aucune notification non lue")
        void getBadgeCount_returnsZeroWhenAllRead() {
            when(notificationRepository.countByUserIdAndStatus(2L, NotificationStatus.UNREAD))
                    .thenReturn(0L);

            BadgeCountResponse response = service.getBadgeCount(2L);

            assertThat(response.getUnreadCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("appelle le repository avec le bon userId et le bon statut")
        void getBadgeCount_callsRepositoryWithCorrectArgs() {
            when(notificationRepository.countByUserIdAndStatus(anyLong(), any()))
                    .thenReturn(3L);

            service.getBadgeCount(42L);

            verify(notificationRepository).countByUserIdAndStatus(42L, NotificationStatus.UNREAD);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsReadTests {

        @Test
        @DisplayName("appelle markAsRead sur le repository avec le bon notificationId et userId")
        void markAsRead_callsRepositoryWithCorrectArgs() {
            when(notificationRepository.markAsRead(eq(1L), eq(10L), any(LocalDateTime.class)))
                    .thenReturn(1);

            service.markAsRead(1L, 10L);

            verify(notificationRepository).markAsRead(eq(1L), eq(10L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("ne lève pas d'exception si la notification n'appartient pas à l'utilisateur (0 updated)")
        void markAsRead_doesNotThrowWhenNotFound() {
            when(notificationRepository.markAsRead(anyLong(), anyLong(), any())).thenReturn(0);

            assertThatCode(() -> service.markAsRead(999L, 10L)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("appelle markAllAsRead sur le repository avec le bon userId")
        void markAllAsRead_callsRepositoryWithCorrectUserId() {
            when(notificationRepository.markAllAsRead(eq(7L), any(LocalDateTime.class)))
                    .thenReturn(4);

            service.markAllAsRead(7L);

            verify(notificationRepository).markAllAsRead(eq(7L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("ne lève pas d'exception si aucune notification à marquer")
        void markAllAsRead_doesNotThrowWhenNothingToMark() {
            when(notificationRepository.markAllAsRead(anyLong(), any())).thenReturn(0);

            assertThatCode(() -> service.markAllAsRead(123L)).doesNotThrowAnyException();
        }
    }

    private Notification buildNotif(Long id, Long userId) {
        return Notification.builder()
                .id(id)
                .userId(userId)
                .type(NotificationType.BROADCAST)
                .status(NotificationStatus.UNREAD)
                .message("Test message")
                .referenceType(ReferenceType.SYSTEM)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private NotificationPreference buildPref(Long userId, NotificationType type,
                                             boolean inApp, boolean email) {
        return NotificationPreference.builder()
                .userId(userId)
                .type(type)
                .inAppEnabled(inApp)
                .emailEnabled(email)
                .build();
    }
}
