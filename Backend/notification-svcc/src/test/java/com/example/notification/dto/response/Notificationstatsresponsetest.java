package com.example.notification.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("NotificationStatsResponse Tests")
class NotificationStatsResponseTest {

    @Test
    @DisplayName("Builder crée un objet avec tous les champs")
    void builder_shouldCreateResponseWithAllFields() {
        Map<String, Long> countByType = new HashMap<>();
        countByType.put("MEDIA_LIKED", 10L);
        countByType.put("BROADCAST", 5L);
        countByType.put("RECO_READY", 3L);

        NotificationStatsResponse response = NotificationStatsResponse.builder()
                .totalCount(100L)
                .readCount(60L)
                .unreadCount(40L)
                .openRatePercent(60.0)
                .countByType(countByType)
                .build();

        assertThat(response.getTotalCount()).isEqualTo(100L);
        assertThat(response.getReadCount()).isEqualTo(60L);
        assertThat(response.getUnreadCount()).isEqualTo(40L);
        assertThat(response.getOpenRatePercent()).isCloseTo(60.0, within(0.01));
        assertThat(response.getCountByType()).containsKeys("MEDIA_LIKED", "BROADCAST", "RECO_READY");
        assertThat(response.getCountByType().get("MEDIA_LIKED")).isEqualTo(10L);
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet avec valeurs par défaut")
    void noArgsConstructor_shouldCreateResponseWithDefaults() {
        NotificationStatsResponse response = new NotificationStatsResponse();

        assertThat(response.getTotalCount()).isZero();
        assertThat(response.getReadCount()).isZero();
        assertThat(response.getUnreadCount()).isZero();
        assertThat(response.getOpenRatePercent()).isZero();
        assertThat(response.getCountByType()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        Map<String, Long> countByType = Map.of("BROADCAST", 2L);

        NotificationStatsResponse response = new NotificationStatsResponse(
                50L, 30L, 20L, 60.0, countByType
        );

        assertThat(response.getTotalCount()).isEqualTo(50L);
        assertThat(response.getReadCount()).isEqualTo(30L);
        assertThat(response.getUnreadCount()).isEqualTo(20L);
        assertThat(response.getOpenRatePercent()).isCloseTo(60.0, within(0.01));
        assertThat(response.getCountByType()).isEqualTo(countByType);
    }

    @Test
    @DisplayName("openRatePercent à 0 quand aucune notif lue")
    void openRatePercent_shouldBeZeroWhenNoReadNotifications() {
        NotificationStatsResponse response = NotificationStatsResponse.builder()
                .totalCount(10L).readCount(0L).unreadCount(10L)
                .openRatePercent(0.0).build();

        assertThat(response.getOpenRatePercent()).isZero();
    }

    @Test
    @DisplayName("openRatePercent à 100 quand toutes lues")
    void openRatePercent_shouldBeHundredWhenAllRead() {
        NotificationStatsResponse response = NotificationStatsResponse.builder()
                .totalCount(10L).readCount(10L).unreadCount(0L)
                .openRatePercent(100.0).build();

        assertThat(response.getOpenRatePercent()).isCloseTo(100.0, within(0.01));
    }

    @Test
    @DisplayName("countByType peut contenir tous les NotificationType")
    void countByType_shouldSupportAllNotificationTypes() {
        Map<String, Long> countByType = new HashMap<>();
        countByType.put("MEDIA_LIKED", 10L);
        countByType.put("MEDIA_ACCEPTED", 5L);
        countByType.put("MEDIA_REFUSED", 2L);
        countByType.put("COMMENT_ADDED", 8L);
        countByType.put("MEDIA_ADDED_TO_COLLECTION", 3L);
        countByType.put("RECO_READY", 7L);
        countByType.put("BROADCAST", 1L);

        NotificationStatsResponse response = NotificationStatsResponse.builder()
                .totalCount(36L).readCount(20L).unreadCount(16L)
                .openRatePercent(55.5).countByType(countByType).build();

        assertThat(response.getCountByType()).hasSize(7);
        assertThat(response.getCountByType().get("COMMENT_ADDED")).isEqualTo(8L);
    }

    @Test
    @DisplayName("countByType peut être null")
    void countByType_canBeNull() {
        NotificationStatsResponse response = NotificationStatsResponse.builder()
                .totalCount(0L).readCount(0L).unreadCount(0L)
                .openRatePercent(0.0).countByType(null).build();

        assertThat(response.getCountByType()).isNull();
    }

    @Test
    @DisplayName("Setters fonctionnent correctement")
    void setters_shouldWorkCorrectly() {
        NotificationStatsResponse response = new NotificationStatsResponse();
        Map<String, Long> countByType = Map.of("BROADCAST", 1L);

        response.setTotalCount(20L);
        response.setReadCount(15L);
        response.setUnreadCount(5L);
        response.setOpenRatePercent(75.0);
        response.setCountByType(countByType);

        assertThat(response.getTotalCount()).isEqualTo(20L);
        assertThat(response.getReadCount()).isEqualTo(15L);
        assertThat(response.getUnreadCount()).isEqualTo(5L);
        assertThat(response.getOpenRatePercent()).isCloseTo(75.0, within(0.01));
        assertThat(response.getCountByType()).isEqualTo(countByType);
    }

    @Test
    @DisplayName("Deux responses identiques sont égales")
    void equals_shouldReturnTrueForIdenticalResponses() {
        Map<String, Long> map = Map.of("BROADCAST", 1L);

        NotificationStatsResponse r1 = NotificationStatsResponse.builder()
                .totalCount(10L).readCount(5L).unreadCount(5L)
                .openRatePercent(50.0).countByType(map).build();

        NotificationStatsResponse r2 = NotificationStatsResponse.builder()
                .totalCount(10L).readCount(5L).unreadCount(5L)
                .openRatePercent(50.0).countByType(map).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux responses différentes ne sont pas égales")
    void equals_shouldReturnFalseForDifferentResponses() {
        NotificationStatsResponse r1 = NotificationStatsResponse.builder()
                .totalCount(10L).readCount(5L).unreadCount(5L)
                .openRatePercent(50.0).build();

        NotificationStatsResponse r2 = NotificationStatsResponse.builder()
                .totalCount(20L).readCount(10L).unreadCount(10L)
                .openRatePercent(50.0).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}
