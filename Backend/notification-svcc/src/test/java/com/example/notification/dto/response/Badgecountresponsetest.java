package com.example.notification.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BadgeCountResponse Tests")
class BadgeCountResponseTest {

    @Test
    @DisplayName("Builder crée un objet avec unreadCount")
    void builder_shouldCreateResponseWithUnreadCount() {
        BadgeCountResponse response = BadgeCountResponse.builder()
                .unreadCount(5L)
                .build();

        assertThat(response.getUnreadCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("NoArgsConstructor initialise unreadCount à 0")
    void noArgsConstructor_shouldInitializeUnreadCountToZero() {
        BadgeCountResponse response = new BadgeCountResponse();

        assertThat(response.getUnreadCount()).isZero();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise unreadCount")
    void allArgsConstructor_shouldSetUnreadCount() {
        BadgeCountResponse response = new BadgeCountResponse(12L);

        assertThat(response.getUnreadCount()).isEqualTo(12L);
    }

    @Test
    @DisplayName("Setter fonctionne correctement")
    void setter_shouldSetUnreadCount() {
        BadgeCountResponse response = new BadgeCountResponse();
        response.setUnreadCount(8L);

        assertThat(response.getUnreadCount()).isEqualTo(8L);
    }

    @Test
    @DisplayName("unreadCount peut être 0")
    void unreadCount_canBeZero() {
        BadgeCountResponse response = BadgeCountResponse.builder()
                .unreadCount(0L)
                .build();

        assertThat(response.getUnreadCount()).isZero();
    }

    @Test
    @DisplayName("unreadCount peut être une grande valeur")
    void unreadCount_canBeLargeValue() {
        BadgeCountResponse response = BadgeCountResponse.builder()
                .unreadCount(Long.MAX_VALUE)
                .build();

        assertThat(response.getUnreadCount()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Deux responses identiques sont égales")
    void equals_shouldReturnTrueForIdenticalResponses() {
        BadgeCountResponse r1 = BadgeCountResponse.builder().unreadCount(3L).build();
        BadgeCountResponse r2 = BadgeCountResponse.builder().unreadCount(3L).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux responses différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentResponses() {
        BadgeCountResponse r1 = BadgeCountResponse.builder().unreadCount(3L).build();
        BadgeCountResponse r2 = BadgeCountResponse.builder().unreadCount(10L).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}