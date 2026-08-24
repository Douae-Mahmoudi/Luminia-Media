package com.example.notification.Integration;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.request.PreferenceUpdateRequest;
import com.example.notification.repository.NotificationPreferenceRepository;
import com.example.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("[IT] NotificationController")
class NotificationControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationPreferenceRepository preferenceRepository;
    @Autowired private NotificationTestHelper notificationTestHelper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private ObjectMapper objectMapper;

    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        notificationRepository.deleteAll();
        preferenceRepository.deleteAll();
    }

    private String bearerToken(Long userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        String token = Jwts.builder()
                .setSubject("user@test.com")
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return "Bearer " + token;
    }


    @Nested
    @DisplayName("GET /api/notifications")
    class GetMyNotifications {

        @Test
        @Order(1)
        @DisplayName("401 si pas de token")
        void noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @Order(2)
        @DisplayName("200 avec page vide si aucune notification")
        void emptyPage_whenNoNotifications() throws Exception {
            mockMvc.perform(get("/api/notifications")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }

        @Test
        @Order(3)
        @DisplayName("200 avec les notifications triées par date décroissante")
        void returnsNotificationsSortedByDateDesc() throws Exception {
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/notifications")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)));
        }

        @Test
        @Order(4)
        @DisplayName("ne retourne que les notifications de l'utilisateur connecté")
        void returnsOnlyCurrentUserNotifications() throws Exception {
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(999L, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/notifications")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].userId", is(USER_ID.intValue())));
        }

        @Test
        @Order(5)
        @DisplayName("pagination fonctionne correctement")
        void paginationWorks() throws Exception {
            for (int i = 0; i < 5; i++) notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/notifications")
                            .param("page", "0")
                            .param("size", "3")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements", is(5)))
                    .andExpect(jsonPath("$.last", is(false)));
        }
    }


    @Nested
    @DisplayName("GET /api/notifications/badge")
    class GetBadgeCount {

        @Test
        @DisplayName("retourne 0 si aucune notification non lue")
        void zeroWhenNoUnread() throws Exception {
            mockMvc.perform(get("/api/notifications/badge")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount", is(0)));
        }

        @Test
        @DisplayName("retourne le bon count de non lues")
        void correctUnreadCount() throws Exception {
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.READ);

            mockMvc.perform(get("/api/notifications/badge")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount", is(2)));
        }

        @Test
        @DisplayName("isole le count par utilisateur")
        void isolatedByUser() throws Exception {
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(999L, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/notifications/badge")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount", is(1)));
        }
    }



    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkOneAsRead {

        @Test
        @DisplayName("204 et notification marquée comme lue")
        void marksNotificationAsRead() throws Exception {
            Notification notif = notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);

            mockMvc.perform(patch("/api/notifications/{id}/read", notif.getId())
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isNoContent());

            Notification updated = notificationRepository.findById(notif.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(NotificationStatus.READ);
            assertThat(updated.getReadAt()).isNotNull();
        }

        @Test
        @DisplayName("204 sans erreur si la notification n'appartient pas à l'user")
        void noErrorWhenNotificationNotOwned() throws Exception {
            Notification notif = notificationTestHelper.saveNotif(999L, NotificationStatus.UNREAD);

            mockMvc.perform(patch("/api/notifications/{id}/read", notif.getId())
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isNoContent());

            Notification unchanged = notificationRepository.findById(notif.getId()).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        }

        @Test
        @DisplayName("204 si la notification est déjà lue (idempotent)")
        void idempotentWhenAlreadyRead() throws Exception {
            Notification notif = notificationTestHelper.saveNotif(USER_ID, NotificationStatus.READ);

            mockMvc.perform(patch("/api/notifications/{id}/read", notif.getId())
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isNoContent());
        }
    }



    @Nested
    @DisplayName("PATCH /api/notifications/read-all")
    class MarkAllAsRead {

        @Test
        @DisplayName("204 et toutes les notifications de l'user sont marquées lues")
        void marksAllAsRead() throws Exception {
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);
            notificationTestHelper.saveNotif(USER_ID, NotificationStatus.UNREAD);

            mockMvc.perform(patch("/api/notifications/read-all")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isNoContent());

            List<Notification> all = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(USER_ID,
                            org.springframework.data.domain.Pageable.unpaged())
                    .getContent();

            assertThat(all).allMatch(n -> n.getStatus() == NotificationStatus.READ);
        }

        @Test
        @DisplayName("n'affecte pas les notifications des autres users")
        void doesNotAffectOtherUsers() throws Exception {
            Notification other = notificationTestHelper.saveNotif(999L, NotificationStatus.UNREAD);

            mockMvc.perform(patch("/api/notifications/read-all")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isNoContent());

            Notification unchanged = notificationRepository.findById(other.getId()).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        }
    }



    @Nested
    @DisplayName("GET /api/notifications/preferences")
    class GetPreferences {

        @Test
        @DisplayName("retourne les préférences par défaut quand aucune n'est persistée")
        void returnsDefaultPreferencesWhenNoneExist() throws Exception {
            int expectedCount = NotificationType.values().length;

            mockMvc.perform(get("/api/notifications/preferences")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedCount)))
                    .andExpect(jsonPath("$[0].inAppEnabled", is(true)))
                    .andExpect(jsonPath("$[0].emailEnabled", is(false)));
        }

        @Test
        @DisplayName("retourne les préférences persistées si elles existent")
        void returnsPersistedPreferences() throws Exception {
            NotificationPreference pref = NotificationPreference.builder()
                    .userId(USER_ID)
                    .type(NotificationType.MEDIA_LIKED)
                    .inAppEnabled(false)
                    .emailEnabled(true)
                    .build();
            preferenceRepository.save(pref);

            mockMvc.perform(get("/api/notifications/preferences")
                            .header("Authorization", bearerToken(USER_ID, "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].type", is("MEDIA_LIKED")))
                    .andExpect(jsonPath("$[0].inAppEnabled", is(true)))   // @PrePersist force true
                    .andExpect(jsonPath("$[0].emailEnabled", is(false))); // @PrePersist force false
        }
    }


    @Nested
    @DisplayName("PUT /api/notifications/preferences")
    class UpdatePreference {

        @Test
        @DisplayName("200 et persistance de la nouvelle préférence")
        void createsNewPreference() throws Exception {
            PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                    .type(NotificationType.COMMENT_ADDED)
                    .inAppEnabled(true)
                    .emailEnabled(true)
                    .build();

            mockMvc.perform(put("/api/notifications/preferences")
                            .header("Authorization", bearerToken(USER_ID, "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type", is("COMMENT_ADDED")))
                    .andExpect(jsonPath("$.inAppEnabled", is(true)))
                    .andExpect(jsonPath("$.emailEnabled", is(false))); // @PrePersist force false à la création

            assertThat(preferenceRepository.findByUserIdAndType(USER_ID, NotificationType.COMMENT_ADDED))
                    .isPresent()
                    .get()
                    .satisfies(p -> {
                        assertThat(p.isInAppEnabled()).isTrue();
                        assertThat(p.isEmailEnabled()).isFalse(); // @PrePersist force false
                    });
        }

        @Test
        @DisplayName("200 et mise à jour d'une préférence existante (upsert)")
        void updatesExistingPreference() throws Exception {
            NotificationPreference existing = NotificationPreference.builder()
                    .userId(USER_ID)
                    .type(NotificationType.MEDIA_LIKED)
                    .inAppEnabled(true)
                    .emailEnabled(false)
                    .build();
            preferenceRepository.save(existing);

            PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                    .type(NotificationType.MEDIA_LIKED)
                    .inAppEnabled(false)
                    .emailEnabled(true)
                    .build();

            mockMvc.perform(put("/api/notifications/preferences")
                            .header("Authorization", bearerToken(USER_ID, "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inAppEnabled", is(false)))
                    .andExpect(jsonPath("$.emailEnabled", is(true)));
        }
    }
}
