package com.example.notification.domain.entity;

import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notification Entity Tests")
class NotificationTest {


    @Test
    @DisplayName("Builder crée une notification avec tous les champs")
    void builder_shouldCreateNotificationWithAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Notification notification = Notification.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD)
                .message("Quelqu'un a aimé votre média")
                .referenceId(100L)
                .referenceType(ReferenceType.MEDIA)
                .createdAt(now)
                .readAt(null)
                .build();

        assertThat(notification.getId()).isEqualTo(1L);
        assertThat(notification.getUserId()).isEqualTo(42L);
        assertThat(notification.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(notification.getMessage()).isEqualTo("Quelqu'un a aimé votre média");
        assertThat(notification.getReferenceId()).isEqualTo(100L);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.MEDIA);
        assertThat(notification.getCreatedAt()).isEqualTo(now);
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    @DisplayName("Builder crée une notification sans champs optionnels")
    void builder_shouldCreateNotificationWithoutOptionalFields() {
        Notification notification = Notification.builder()
                .userId(1L)
                .type(NotificationType.BROADCAST)
                .status(NotificationStatus.UNREAD)
                .message("Message système")
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getReferenceId()).isNull();
        assertThat(notification.getReferenceType()).isNull();
        assertThat(notification.getReadAt()).isNull();
    }


    @Test
    @DisplayName("prePersist initialise createdAt et status UNREAD")
    void prePersist_shouldSetCreatedAtAndStatusUnread() {
        Notification notification = new Notification();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        notification.prePersist();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(notification.getCreatedAt())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    @DisplayName("prePersist écrase un status READ déjà défini")
    void prePersist_shouldOverrideExistingStatus() {
        Notification notification = Notification.builder()
                .status(NotificationStatus.READ)
                .build();

        notification.prePersist();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    @DisplayName("prePersist écrase un createdAt déjà défini")
    void prePersist_shouldOverrideExistingCreatedAt() {
        LocalDateTime oldDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        Notification notification = Notification.builder()
                .createdAt(oldDate)
                .build();

        notification.prePersist();

        assertThat(notification.getCreatedAt()).isNotEqualTo(oldDate);
        assertThat(notification.getCreatedAt()).isAfter(oldDate);
    }


    @Test
    @DisplayName("setReadAt marque la notification comme lue")
    void setReadAt_shouldMarkNotificationAsRead() {
        Notification notification = Notification.builder()
                .userId(1L)
                .type(NotificationType.COMMENT_ADDED)
                .status(NotificationStatus.UNREAD)
                .message("Un commentaire a été ajouté")
                .referenceId(10L)
                .referenceType(ReferenceType.COMMENT)
                .createdAt(LocalDateTime.now())
                .build();

        LocalDateTime readTime = LocalDateTime.now();
        notification.setReadAt(readTime);
        notification.setStatus(NotificationStatus.READ);

        assertThat(notification.getReadAt()).isEqualTo(readTime);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    @DisplayName("NoArgsConstructor crée une notification vide")
    void noArgsConstructor_shouldCreateEmptyNotification() {
        Notification notification = new Notification();

        assertThat(notification.getId()).isNull();
        assertThat(notification.getUserId()).isNull();
        assertThat(notification.getMessage()).isNull();
        assertThat(notification.getCreatedAt()).isNull();
        assertThat(notification.getStatus()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = now.plusMinutes(5);

        Notification notification = new Notification(
                1L,
                10L,
                NotificationType.RECO_READY,
                NotificationStatus.READ,
                "Vos recommandations sont prêtes",
                50L,
                ReferenceType.COLLECTION,
                now,
                readAt
        );

        assertThat(notification.getId()).isEqualTo(1L);
        assertThat(notification.getUserId()).isEqualTo(10L);
        assertThat(notification.getType()).isEqualTo(NotificationType.RECO_READY);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(notification.getMessage()).isEqualTo("Vos recommandations sont prêtes");
        assertThat(notification.getReferenceId()).isEqualTo(50L);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.COLLECTION);
        assertThat(notification.getCreatedAt()).isEqualTo(now);
        assertThat(notification.getReadAt()).isEqualTo(readAt);
    }



    @Test
    @DisplayName("Notification MEDIA_ACCEPTED avec ReferenceType.MEDIA")
    void builder_shouldCreateMediaAcceptedNotification() {
        Notification notification = Notification.builder()
                .userId(5L)
                .type(NotificationType.MEDIA_ACCEPTED)
                .status(NotificationStatus.UNREAD)
                .message("Votre média a été accepté")
                .referenceId(99L)
                .referenceType(ReferenceType.MEDIA)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getType()).isEqualTo(NotificationType.MEDIA_ACCEPTED);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.MEDIA);
    }

    @Test
    @DisplayName("Notification MEDIA_REFUSED avec ReferenceType.MEDIA")
    void builder_shouldCreateMediaRefusedNotification() {
        Notification notification = Notification.builder()
                .userId(5L)
                .type(NotificationType.MEDIA_REFUSED)
                .status(NotificationStatus.UNREAD)
                .message("Votre média a été refusé")
                .referenceId(88L)
                .referenceType(ReferenceType.MEDIA)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getType()).isEqualTo(NotificationType.MEDIA_REFUSED);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.MEDIA);
    }

    @Test
    @DisplayName("Notification MEDIA_ADDED_TO_COLLECTION avec ReferenceType.COLLECTION")
    void builder_shouldCreateMediaAddedToCollectionNotification() {
        Notification notification = Notification.builder()
                .userId(7L)
                .type(NotificationType.MEDIA_ADDED_TO_COLLECTION)
                .status(NotificationStatus.UNREAD)
                .message("Un média a été ajouté à votre collection")
                .referenceId(77L)
                .referenceType(ReferenceType.COLLECTION)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getType()).isEqualTo(NotificationType.MEDIA_ADDED_TO_COLLECTION);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.COLLECTION);
    }

    @Test
    @DisplayName("Notification BROADCAST avec ReferenceType.SYSTEM sans referenceId")
    void builder_shouldCreateBroadcastNotification() {
        Notification notification = Notification.builder()
                .userId(1L)
                .type(NotificationType.BROADCAST)
                .status(NotificationStatus.UNREAD)
                .message("Maintenance prévue ce soir à 22h")
                .referenceType(ReferenceType.SYSTEM)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getType()).isEqualTo(NotificationType.BROADCAST);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.SYSTEM);
        assertThat(notification.getReferenceId()).isNull();
    }

    @Test
    @DisplayName("Notification RECO_READY avec ReferenceType.COLLECTION")
    void builder_shouldCreateRecoReadyNotification() {
        Notification notification = Notification.builder()
                .userId(3L)
                .type(NotificationType.RECO_READY)
                .status(NotificationStatus.UNREAD)
                .message("Vos recommandations sont prêtes")
                .referenceId(55L)
                .referenceType(ReferenceType.COLLECTION)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getType()).isEqualTo(NotificationType.RECO_READY);
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.COLLECTION);
    }



    @Test
    @DisplayName("Deux notifications identiques sont égales")
    void equals_shouldReturnTrueForIdenticalNotifications() {
        LocalDateTime now = LocalDateTime.now();

        Notification n1 = Notification.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD)
                .message("msg").createdAt(now).build();

        Notification n2 = Notification.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD)
                .message("msg").createdAt(now).build();

        assertThat(n1).isEqualTo(n2);
        assertThat(n1.hashCode()).isEqualTo(n2.hashCode());
    }

    @Test
    @DisplayName("Deux notifications différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentNotifications() {
        LocalDateTime now = LocalDateTime.now();

        Notification n1 = Notification.builder()
                .id(1L).userId(1L)
                .type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD)
                .message("msg1").createdAt(now).build();

        Notification n2 = Notification.builder()
                .id(2L).userId(2L)
                .type(NotificationType.COMMENT_ADDED)
                .status(NotificationStatus.UNREAD)
                .message("msg2").createdAt(now).build();

        assertThat(n1).isNotEqualTo(n2);
    }
}
