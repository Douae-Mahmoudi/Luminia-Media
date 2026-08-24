package com.example.notification.consumer;

import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.event.MediaLikedEvent;
import com.example.notification.event.MediaStatusEvent;
import com.example.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MediaEventConsumer mediaEventConsumer;


    private MediaLikedEvent likedEvent(Long mediaId, Long ownerId, String username, String title) {
        MediaLikedEvent e = new MediaLikedEvent();
        e.setMediaId(mediaId);
        e.setOwnerId(ownerId);
        e.setLikedByUsername(username);
        e.setMediaTitle(title);
        return e;
    }

    private MediaStatusEvent statusEvent(Long mediaId, Long ownerId, String status, String title, String reason) {
        MediaStatusEvent e = new MediaStatusEvent();
        e.setMediaId(mediaId);
        e.setOwnerId(ownerId);
        e.setStatus(status);
        e.setMediaTitle(title);
        e.setReason(reason);
        return e;
    }



    @Nested
    @DisplayName("onMediaLiked()")
    class OnMediaLikedTests {

        @Test
        @DisplayName("should send MEDIA_LIKED notification with correct fields")
        void shouldSendMediaLikedNotification() {
            mediaEventConsumer.onMediaLiked(
                    likedEvent(10L, 1L, "alice", "Mon super film"), "media-liked");

            verify(notificationService).send(
                    eq(1L),
                    eq(NotificationType.MEDIA_LIKED),
                    eq("alice a aimé votre média \"Mon super film\""),
                    eq(10L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should format message with correct username and title")
        void shouldFormatMessageCorrectly() {
            mediaEventConsumer.onMediaLiked(
                    likedEvent(5L, 2L, "bob", "Le voyage"), "media-liked");

            verify(notificationService).send(
                    eq(2L),
                    eq(NotificationType.MEDIA_LIKED),
                    eq("bob a aimé votre média \"Le voyage\""),
                    eq(5L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should call notificationService exactly once")
        void shouldCallServiceExactlyOnce() {
            mediaEventConsumer.onMediaLiked(
                    likedEvent(1L, 1L, "x", "y"), "media-liked");

            verify(notificationService, times(1)).send(any(), any(), any(), any(), any());
        }
    }



    @Nested
    @DisplayName("onMediaDecision() — ACCEPTED")
    class OnMediaDecisionAcceptedTests {

        @Test
        @DisplayName("should send MEDIA_ACCEPTED notification")
        void shouldSendAcceptedNotification() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(20L, 3L, "ACCEPTED", "Court métrage", null), "media-decision");

            verify(notificationService).send(
                    eq(3L),
                    eq(NotificationType.MEDIA_ACCEPTED),
                    eq("Votre média \"Court métrage\" a été accepté et publié !"),
                    eq(20L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should never send MEDIA_REFUSED when status is ACCEPTED")
        void shouldNotSendRefusedWhenAccepted() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(20L, 3L, "ACCEPTED", "Film", null), "media-decision");

            verify(notificationService, never()).send(
                    any(), eq(NotificationType.MEDIA_REFUSED), any(), any(), any());
        }
    }



    @Nested
    @DisplayName("onMediaDecision() — REFUSED")
    class OnMediaDecisionRefusedTests {

        @Test
        @DisplayName("should send MEDIA_REFUSED with reason when provided")
        void shouldSendRefusedWithReason() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(30L, 4L, "REFUSED", "Clip vidéo", "Contenu inapproprié"), "media-decision");

            verify(notificationService).send(
                    eq(4L),
                    eq(NotificationType.MEDIA_REFUSED),
                    eq("Votre média \"Clip vidéo\" a été refusé. — Raison : Contenu inapproprié"),
                    eq(30L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should send MEDIA_REFUSED without reason suffix when reason is null")
        void shouldSendRefusedWithoutReasonWhenNull() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(31L, 4L, "REFUSED", "Documentaire", null), "media-decision");

            verify(notificationService).send(
                    eq(4L),
                    eq(NotificationType.MEDIA_REFUSED),
                    eq("Votre média \"Documentaire\" a été refusé."),
                    eq(31L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should never send MEDIA_ACCEPTED when status is REFUSED")
        void shouldNotSendAcceptedWhenRefused() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(30L, 4L, "REFUSED", "Film", null), "media-decision");

            verify(notificationService, never()).send(
                    any(), eq(NotificationType.MEDIA_ACCEPTED), any(), any(), any());
        }
    }



    @Nested
    @DisplayName("onMediaDecision() — status inconnu")
    class OnMediaDecisionUnknownTests {

        @Test
        @DisplayName("should not call notificationService when status is unknown")
        void shouldNotSendForUnknownStatus() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(99L, 5L, "PENDING", "Draft", null), "media-decision");

            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("should not call notificationService when status is empty string")
        void shouldNotSendForEmptyStatus() {
            mediaEventConsumer.onMediaDecision(
                    statusEvent(99L, 5L, "", "Draft", null), "media-decision");

            verifyNoInteractions(notificationService);
        }
    }
}
