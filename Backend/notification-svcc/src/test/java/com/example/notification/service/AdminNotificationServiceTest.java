package com.example.notification.service;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.request.BroadcastRequest;
import com.example.notification.dto.response.AdminNotificationResponse;
import com.example.notification.dto.response.NotificationStatsResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.sender.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminNotificationService")
class AdminNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender senderA;

    @Mock
    private NotificationSender senderB;

    private AdminNotificationService service;

    @BeforeEach
    void setUp() {
        service = new AdminNotificationService(notificationRepository, List.of(senderA, senderB));
    }

    @Nested
    @DisplayName("broadcast()")
    class BroadcastTests {

        private final BroadcastRequest request = BroadcastRequest.builder()
                .message("Hello everyone")
                .title("Announcement")
                .build();

        @Test
        @DisplayName("envoie une notif et appelle chaque sender pour chaque userId")
        void broadcast_sendsToAllUsers() throws Exception {
            when(notificationRepository.findDistinctUserIds()).thenReturn(List.of(1L, 2L));

            Notification saved1 = buildNotification(10L, 1L);
            Notification saved2 = buildNotification(11L, 2L);
            when(notificationRepository.save(any()))
                    .thenReturn(saved1)
                    .thenReturn(saved2);

            service.broadcast(request);

            verify(notificationRepository, times(2)).save(any(Notification.class));

            verify(senderA, times(2)).send(any(Notification.class), any(NotificationPreference.class));
            verify(senderB, times(2)).send(any(Notification.class), any(NotificationPreference.class));
        }

        @Test
        @DisplayName("la notif sauvegardée a le bon type, message et referenceType")
        void broadcast_notificationHasCorrectFields() {
            when(notificationRepository.findDistinctUserIds()).thenReturn(List.of(42L));
            Notification saved = buildNotification(99L, 42L);
            when(notificationRepository.save(any())).thenReturn(saved);

            service.broadcast(request);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification captured = captor.getValue();
            assertThat(captured.getUserId()).isEqualTo(42L);
            assertThat(captured.getType()).isEqualTo(NotificationType.BROADCAST);
            assertThat(captured.getMessage()).isEqualTo("Hello everyone");
            assertThat(captured.getReferenceType()).isEqualTo(ReferenceType.SYSTEM);
        }

        @Test
        @DisplayName("la preference passée aux senders a inAppEnabled=true et emailEnabled=true")
        void broadcast_preferenceHasCorrectDefaults() throws Exception {
            when(notificationRepository.findDistinctUserIds()).thenReturn(List.of(5L));
            when(notificationRepository.save(any())).thenReturn(buildNotification(1L, 5L));

            service.broadcast(request);

            ArgumentCaptor<NotificationPreference> prefCaptor =
                    ArgumentCaptor.forClass(NotificationPreference.class);
            verify(senderA).send(any(), prefCaptor.capture());

            NotificationPreference pref = prefCaptor.getValue();
            assertThat(pref.isInAppEnabled()).isTrue();
            assertThat(pref.isEmailEnabled()).isTrue();
            assertThat(pref.getType()).isEqualTo(NotificationType.BROADCAST);
        }

        @Test
        @DisplayName("ne fait rien si aucun userId trouvé")
        void broadcast_doesNothingWhenNoUsers() throws Exception {
            when(notificationRepository.findDistinctUserIds()).thenReturn(List.of());

            service.broadcast(request);

            verify(notificationRepository, never()).save(any());
            verify(senderA, never()).send(any(), any());
            verify(senderB, never()).send(any(), any());
        }

        @Test
        @DisplayName("continue avec les autres senders si l'un lève une exception")
        void broadcast_continuesWhenOneSenderFails() throws Exception {
            when(notificationRepository.findDistinctUserIds()).thenReturn(List.of(1L));
            when(notificationRepository.save(any())).thenReturn(buildNotification(1L, 1L));
            doThrow(new RuntimeException("SMTP error")).when(senderA).send(any(), any());

            assertThatCode(() -> service.broadcast(request)).doesNotThrowAnyException();

            verify(senderB).send(any(), any());
        }

        @Test
        @DisplayName("continue avec les autres userId si un sender échoue sur l'un d'eux")
        void broadcast_continuesForOtherUsersOnSenderFailure() throws Exception {
            when(notificationRepository.findDistinctUserIds()).thenReturn(List.of(1L, 2L));
            Notification n1 = buildNotification(10L, 1L);
            Notification n2 = buildNotification(11L, 2L);
            when(notificationRepository.save(any())).thenReturn(n1).thenReturn(n2);

            doThrow(new RuntimeException("fail"))
                    .when(senderA).send(eq(n1), any());

            assertThatCode(() -> service.broadcast(request)).doesNotThrowAnyException();
            verify(senderA, times(2)).send(any(), any());
        }
    }


    @Nested
    @DisplayName("getAllNotifications()")
    class GetAllNotificationsTests {

        private final Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        @Test
        @DisplayName("retourne une PageResponse correctement mappée")
        void getAllNotifications_returnsMappedPage() {
            Notification notif = buildNotification(1L, 10L);
            Page<Notification> page = new PageImpl<>(List.of(notif), pageable, 1);

            when(notificationRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(page);

            PageResponse<AdminNotificationResponse> result =
                    service.getAllNotifications(10L, NotificationType.BROADCAST,
                            null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
            assertThat(result.isLast()).isTrue();

            AdminNotificationResponse resp = result.getContent().get(0);
            assertThat(resp.getId()).isEqualTo(1L);
            assertThat(resp.getUserId()).isEqualTo(10L);
            assertThat(resp.getType()).isEqualTo(NotificationType.BROADCAST);
            assertThat(resp.getStatus()).isEqualTo(NotificationStatus.UNREAD);
            assertThat(resp.getMessage()).isEqualTo("Test message");
        }

        @Test
        @DisplayName("retourne une page vide quand il n'y a pas de résultats")
        void getAllNotifications_returnsEmptyPage() {
            Page<Notification> emptyPage = Page.empty(pageable);
            when(notificationRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            PageResponse<AdminNotificationResponse> result =
                    service.getAllNotifications(null, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("mappe correctement tous les champs de AdminNotificationResponse")
        void getAllNotifications_mapsAllFields() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime readAt = now.plusMinutes(5);

            Notification notif = Notification.builder()
                    .id(7L)
                    .userId(3L)
                    .type(NotificationType.MEDIA_LIKED)
                    .status(NotificationStatus.READ)
                    .message("Your media was liked")
                    .referenceId(42L)
                    .referenceType(ReferenceType.MEDIA)
                    .createdAt(now)
                    .readAt(readAt)
                    .build();

            Page<Notification> page = new PageImpl<>(List.of(notif));
            when(notificationRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(page);

            PageResponse<AdminNotificationResponse> result =
                    service.getAllNotifications(3L, null, null, null, pageable);

            AdminNotificationResponse resp = result.getContent().get(0);
            assertThat(resp.getId()).isEqualTo(7L);
            assertThat(resp.getUserId()).isEqualTo(3L);
            assertThat(resp.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
            assertThat(resp.getStatus()).isEqualTo(NotificationStatus.READ);
            assertThat(resp.getMessage()).isEqualTo("Your media was liked");
            assertThat(resp.getReferenceId()).isEqualTo(42L);
            assertThat(resp.getReferenceType()).isEqualTo(ReferenceType.MEDIA);
            assertThat(resp.getCreatedAt()).isEqualTo(now);
            assertThat(resp.getReadAt()).isEqualTo(readAt);
        }

        @Test
        @DisplayName("appelle bien le repository avec la Specification et le Pageable fournis")
        void getAllNotifications_callsRepositoryWithSpecAndPageable() {
            Page<Notification> emptyPage = Page.empty(pageable);
            when(notificationRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            service.getAllNotifications(1L, NotificationType.RECO_READY,
                    LocalDateTime.now().minusDays(7), LocalDateTime.now(), pageable);

            verify(notificationRepository).findAll(any(Specification.class), eq(pageable));
        }
    }


    @Nested
    @DisplayName("getStats()")
    class GetStatsTests {

        @Test
        @DisplayName("calcule correctement unreadCount et openRatePercent")
        void getStats_computesCorrectly() {
            when(notificationRepository.count()).thenReturn(200L);
            when(notificationRepository.countRead()).thenReturn(150L);
            when(notificationRepository.countByType()).thenReturn(List.of(
                    new Object[]{"BROADCAST", 100L},
                    new Object[]{"MEDIA_LIKED", 100L}
            ));

            NotificationStatsResponse stats = service.getStats();

            assertThat(stats.getTotalCount()).isEqualTo(200L);
            assertThat(stats.getReadCount()).isEqualTo(150L);
            assertThat(stats.getUnreadCount()).isEqualTo(50L);
            assertThat(stats.getOpenRatePercent()).isEqualTo(75.0);
            assertThat(stats.getCountByType())
                    .containsEntry("BROADCAST", 100L)
                    .containsEntry("MEDIA_LIKED", 100L);
        }

        @Test
        @DisplayName("openRatePercent = 0.0 quand il n'y a aucune notification")
        void getStats_openRateIsZeroWhenNoNotifications() {
            when(notificationRepository.count()).thenReturn(0L);
            when(notificationRepository.countRead()).thenReturn(0L);
            when(notificationRepository.countByType()).thenReturn(List.of());

            NotificationStatsResponse stats = service.getStats();

            assertThat(stats.getOpenRatePercent()).isEqualTo(0.0);
            assertThat(stats.getTotalCount()).isEqualTo(0L);
            assertThat(stats.getUnreadCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("arrondit openRatePercent à une décimale")
        void getStats_roundsOpenRateToOneDecimal() {
            when(notificationRepository.count()).thenReturn(3L);
            when(notificationRepository.countRead()).thenReturn(1L);
            when(notificationRepository.countByType()).thenReturn(List.of());

            NotificationStatsResponse stats = service.getStats();

            assertThat(stats.getOpenRatePercent()).isEqualTo(33.3);
        }

        @Test
        @DisplayName("openRatePercent = 100.0 quand tout est lu")
        void getStats_openRateIsFullWhenAllRead() {
            when(notificationRepository.count()).thenReturn(50L);
            when(notificationRepository.countRead()).thenReturn(50L);
            when(notificationRepository.countByType()).thenReturn(List.of());

            NotificationStatsResponse stats = service.getStats();

            assertThat(stats.getOpenRatePercent()).isEqualTo(100.0);
            assertThat(stats.getUnreadCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("countByType retourne une Map avec les bons types")
        void getStats_countByTypeMapIsCorrect() {
            when(notificationRepository.count()).thenReturn(10L);
            when(notificationRepository.countRead()).thenReturn(0L);
            when(notificationRepository.countByType()).thenReturn(List.of(
                    new Object[]{"RECO_READY", 5L},
                    new Object[]{"COMMENT_ADDED", 3L},
                    new Object[]{"BROADCAST", 2L}
            ));

            NotificationStatsResponse stats = service.getStats();

            assertThat(stats.getCountByType())
                    .hasSize(3)
                    .containsEntry("RECO_READY", 5L)
                    .containsEntry("COMMENT_ADDED", 3L)
                    .containsEntry("BROADCAST", 2L);
        }
    }

    private Notification buildNotification(Long id, Long userId) {
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
}
