package com.example.notification.dto.response;

import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdminNotificationResponse Tests")
class AdminNotificationResponseTest {


    @Test
    @DisplayName("Builder crée un objet avec tous les champs")
    void builder_shouldCreateResponseWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = now.plusMinutes(10);

        AdminNotificationResponse response = AdminNotificationResponse.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.READ)
                .message("Quelqu'un a aimé votre média")
                .referenceId(100L)
                .referenceType(ReferenceType.MEDIA)
                .createdAt(now)
                .readAt(readAt)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(42L);
        assertThat(response.getType()).isEqualTo(NotificationType.MEDIA_LIKED);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(response.getMessage()).isEqualTo("Quelqu'un a aimé votre média");
        assertThat(response.getReferenceId()).isEqualTo(100L);
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.MEDIA);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getReadAt()).isEqualTo(readAt);
    }

    @Test
    @DisplayName("Builder crée un objet sans champs optionnels")
    void builder_shouldCreateResponseWithoutOptionalFields() {
        AdminNotificationResponse response = AdminNotificationResponse.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.BROADCAST)
                .status(NotificationStatus.UNREAD)
                .message("Message broadcast")
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(response.getReferenceId()).isNull();
        assertThat(response.getReferenceType()).isNull();
        assertThat(response.getReadAt()).isNull();
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet vide")
    void noArgsConstructor_shouldCreateEmptyResponse() {
        AdminNotificationResponse response = new AdminNotificationResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getType()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getReadAt()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = now.plusMinutes(5);

        AdminNotificationResponse response = new AdminNotificationResponse(
                1L, 10L,
                NotificationType.COMMENT_ADDED,
                NotificationStatus.READ,
                "Un commentaire a été ajouté",
                50L, ReferenceType.COMMENT,
                now, readAt
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getType()).isEqualTo(NotificationType.COMMENT_ADDED);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(response.getMessage()).isEqualTo("Un commentaire a été ajouté");
        assertThat(response.getReferenceId()).isEqualTo(50L);
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.COMMENT);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getReadAt()).isEqualTo(readAt);
    }

    @Test
    @DisplayName("Setters fonctionnent correctement")
    void setters_shouldWorkCorrectly() {
        AdminNotificationResponse response = new AdminNotificationResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setId(5L);
        response.setUserId(20L);
        response.setType(NotificationType.RECO_READY);
        response.setStatus(NotificationStatus.UNREAD);
        response.setMessage("Recommandations prêtes");
        response.setReferenceId(77L);
        response.setReferenceType(ReferenceType.COLLECTION);
        response.setCreatedAt(now);
        response.setReadAt(null);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getUserId()).isEqualTo(20L);
        assertThat(response.getType()).isEqualTo(NotificationType.RECO_READY);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(response.getMessage()).isEqualTo("Recommandations prêtes");
        assertThat(response.getReferenceId()).isEqualTo(77L);
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.COLLECTION);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getReadAt()).isNull();
    }


    @Test
    @DisplayName("Deux responses identiques sont égales")
    void equals_shouldReturnTrueForIdenticalResponses() {
        LocalDateTime now = LocalDateTime.now();

        AdminNotificationResponse r1 = AdminNotificationResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD).message("msg").createdAt(now).build();

        AdminNotificationResponse r2 = AdminNotificationResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD).message("msg").createdAt(now).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux responses différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentResponses() {
        LocalDateTime now = LocalDateTime.now();

        AdminNotificationResponse r1 = AdminNotificationResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD).message("msg1").createdAt(now).build();

        AdminNotificationResponse r2 = AdminNotificationResponse.builder()
                .id(2L).userId(2L).type(NotificationType.BROADCAST)
                .status(NotificationStatus.READ).message("msg2").createdAt(now).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}

