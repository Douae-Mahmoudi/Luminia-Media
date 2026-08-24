package com.example.notification.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("NotificationType Enum Tests")
class NotificationTypeTest {

    @Test
    @DisplayName("Contient exactement 7 valeurs")
    void shouldHaveExactlySevenValues() {
        assertThat(NotificationType.values()).hasSize(7);
    }

    @Test
    @DisplayName("Contient tous les événements media-svc")
    void shouldContainMediaSvcEvents() {
        assertThat(NotificationType.values())
                .contains(
                        NotificationType.MEDIA_LIKED,
                        NotificationType.MEDIA_ACCEPTED,
                        NotificationType.MEDIA_REFUSED
                );
    }

    @Test
    @DisplayName("Contient tous les événements collection-svc")
    void shouldContainCollectionSvcEvents() {
        assertThat(NotificationType.values())
                .contains(
                        NotificationType.COMMENT_ADDED,
                        NotificationType.MEDIA_ADDED_TO_COLLECTION
                );
    }

    @Test
    @DisplayName("Contient l'événement ai-svc RECO_READY")
    void shouldContainAiSvcEvent() {
        assertThat(NotificationType.values())
                .contains(NotificationType.RECO_READY);
    }

    @Test
    @DisplayName("Contient BROADCAST pour admin")
    void shouldContainBroadcast() {
        assertThat(NotificationType.values())
                .contains(NotificationType.BROADCAST);
    }

    @Test
    @DisplayName("valueOf retourne la bonne constante pour chaque valeur")
    void valueOfShouldReturnCorrectEnum() {
        assertThat(NotificationType.valueOf("MEDIA_LIKED")).isEqualTo(NotificationType.MEDIA_LIKED);
        assertThat(NotificationType.valueOf("MEDIA_ACCEPTED")).isEqualTo(NotificationType.MEDIA_ACCEPTED);
        assertThat(NotificationType.valueOf("MEDIA_REFUSED")).isEqualTo(NotificationType.MEDIA_REFUSED);
        assertThat(NotificationType.valueOf("COMMENT_ADDED")).isEqualTo(NotificationType.COMMENT_ADDED);
        assertThat(NotificationType.valueOf("MEDIA_ADDED_TO_COLLECTION")).isEqualTo(NotificationType.MEDIA_ADDED_TO_COLLECTION);
        assertThat(NotificationType.valueOf("RECO_READY")).isEqualTo(NotificationType.RECO_READY);
        assertThat(NotificationType.valueOf("BROADCAST")).isEqualTo(NotificationType.BROADCAST);
    }

    @Test
    @DisplayName("valueOf lève IllegalArgumentException pour valeur inconnue")
    void valueOfShouldThrowForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> NotificationType.valueOf("EMAIL"));
        assertThrows(IllegalArgumentException.class, () -> NotificationType.valueOf("SMS"));
    }

    @Test
    @DisplayName("ordinal() retourne le bon index pour chaque constante")
    void ordinalShouldReturnCorrectIndex() {
        assertThat(NotificationType.MEDIA_LIKED.ordinal()).isEqualTo(0);
        assertThat(NotificationType.MEDIA_ACCEPTED.ordinal()).isEqualTo(1);
        assertThat(NotificationType.MEDIA_REFUSED.ordinal()).isEqualTo(2);
        assertThat(NotificationType.COMMENT_ADDED.ordinal()).isEqualTo(3);
        assertThat(NotificationType.MEDIA_ADDED_TO_COLLECTION.ordinal()).isEqualTo(4);
        assertThat(NotificationType.RECO_READY.ordinal()).isEqualTo(5);
        assertThat(NotificationType.BROADCAST.ordinal()).isEqualTo(6);
    }

    @Test
    @DisplayName("name() retourne le bon nom en String")
    void nameShouldReturnCorrectString() {
        assertThat(NotificationType.MEDIA_LIKED.name()).isEqualTo("MEDIA_LIKED");
        assertThat(NotificationType.MEDIA_ACCEPTED.name()).isEqualTo("MEDIA_ACCEPTED");
        assertThat(NotificationType.MEDIA_REFUSED.name()).isEqualTo("MEDIA_REFUSED");
        assertThat(NotificationType.COMMENT_ADDED.name()).isEqualTo("COMMENT_ADDED");
        assertThat(NotificationType.MEDIA_ADDED_TO_COLLECTION.name()).isEqualTo("MEDIA_ADDED_TO_COLLECTION");
        assertThat(NotificationType.RECO_READY.name()).isEqualTo("RECO_READY");
        assertThat(NotificationType.BROADCAST.name()).isEqualTo("BROADCAST");
    }
}