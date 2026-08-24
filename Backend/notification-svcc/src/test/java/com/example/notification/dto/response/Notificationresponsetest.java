package com.example.notification.dto.response;

import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationResponse Tests")
class NotificationResponseTest {

    @Test
    @DisplayName("Builder crée un objet avec tous les champs")
    void builder_shouldCreateResponseWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = now.plusMinutes(5);

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.READ)
                .message("Quelqu'un a aimé votre média")
                .referenceId(100L)
                .referenceType(ReferenceType.MEDIA)
                .createdAt(now)
                .readAt(readAt)
                .read(true)
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
        assertThat(response.isRead()).isTrue();
    }

    @Test
    @DisplayName("Builder crée un objet UNREAD sans readAt")
    void builder_shouldCreateUnreadResponseWithoutReadAt() {
        NotificationResponse response = NotificationResponse.builder()
                .id(2L)
                .userId(10L)
                .type(NotificationType.BROADCAST)
                .status(NotificationStatus.UNREAD)
                .message("Message broadcast")
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();

        assertThat(response.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(response.getReadAt()).isNull();
        assertThat(response.isRead()).isFalse();
        assertThat(response.getReferenceId()).isNull();
        assertThat(response.getReferenceType()).isNull();
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet vide")
    void noArgsConstructor_shouldCreateEmptyResponse() {
        NotificationResponse response = new NotificationResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getType()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.isRead()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = now.plusMinutes(2);

        NotificationResponse response = new NotificationResponse(
                1L, 5L,
                NotificationType.RECO_READY,
                NotificationStatus.READ,
                "Recommandations prêtes",
                99L, ReferenceType.COLLECTION,
                now, readAt, true
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(5L);
        assertThat(response.getType()).isEqualTo(NotificationType.RECO_READY);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(response.getMessage()).isEqualTo("Recommandations prêtes");
        assertThat(response.getReferenceId()).isEqualTo(99L);
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.COLLECTION);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getReadAt()).isEqualTo(readAt);
        assertThat(response.isRead()).isTrue();
    }

    @Test
    @DisplayName("setRead met à jour le champ read")
    void setter_shouldUpdateReadField() {
        NotificationResponse response = new NotificationResponse();
        response.setRead(true);

        assertThat(response.isRead()).isTrue();
    }

    @Test
    @DisplayName("Deux responses identiques sont égales")
    void equals_shouldReturnTrueForIdenticalResponses() {
        LocalDateTime now = LocalDateTime.now();

        NotificationResponse r1 = NotificationResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD).message("msg")
                .createdAt(now).read(false).build();

        NotificationResponse r2 = NotificationResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD).message("msg")
                .createdAt(now).read(false).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux responses différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentResponses() {
        LocalDateTime now = LocalDateTime.now();

        NotificationResponse r1 = NotificationResponse.builder()
                .id(1L).userId(1L).type(NotificationType.MEDIA_LIKED)
                .status(NotificationStatus.UNREAD).message("msg1")
                .createdAt(now).read(false).build();

        NotificationResponse r2 = NotificationResponse.builder()
                .id(2L).userId(2L).type(NotificationType.BROADCAST)
                .status(NotificationStatus.READ).message("msg2")
                .createdAt(now).read(true).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}
