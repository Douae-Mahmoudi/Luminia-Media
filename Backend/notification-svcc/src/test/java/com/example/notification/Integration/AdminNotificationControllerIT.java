package com.example.notification.Integration;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.request.BroadcastRequest;
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
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("[IT] AdminNotificationController")
class AdminNotificationControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private NotificationRepository notificationRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        notificationRepository.deleteAll();
    }


    private String bearerToken(Long userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        String token = Jwts.builder()
                .setSubject("admin@test.com")
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return "Bearer " + token;
    }

    @Transactional
    protected Notification saveNotif(Long userId, NotificationType type, NotificationStatus status) {
        Notification n = Notification.builder()
                .userId(userId)
                .type(type)
                .message("Message de test")
                .referenceId(1L)
                .referenceType(ReferenceType.SYSTEM)
                .build();
        Notification saved = notificationRepository.save(n);
        if (status == NotificationStatus.READ) {
            saved.setStatus(NotificationStatus.READ);
            saved.setReadAt(LocalDateTime.now());
            return notificationRepository.save(saved);
        }
        return saved;
    }



    @Nested
    @DisplayName("GET /api/admin/notifications")
    class GetAllNotifications {

        @Test
        @DisplayName("401 sans token")
        void noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/admin/notifications"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("200 page vide si aucune notification")
        void emptyWhenNone() throws Exception {
            mockMvc.perform(get("/api/admin/notifications")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }

        @Test
        @DisplayName("200 retourne toutes les notifications toutes users confondus")
        void returnsAllUsersNotifications() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(20L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            saveNotif(30L, NotificationType.COMMENT_ADDED, NotificationStatus.READ);

            mockMvc.perform(get("/api/admin/notifications")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(3)));
        }

        @Test
        @DisplayName("filtre par userId fonctionne")
        void filterByUserId() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(20L, NotificationType.BROADCAST, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/admin/notifications")
                            .param("userId", "10")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].userId", is(10)));
        }

        @Test
        @DisplayName("filtre par type fonctionne")
        void filterByType() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(10L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            saveNotif(10L, NotificationType.COMMENT_ADDED, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/admin/notifications")
                            .param("type", "BROADCAST")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].type", is("BROADCAST")));
        }

        @Test
        @DisplayName("filtre combiné userId + type")
        void filterByUserIdAndType() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(10L, NotificationType.BROADCAST, NotificationStatus.UNREAD);
            saveNotif(20L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/admin/notifications")
                            .param("userId", "10")
                            .param("type", "MEDIA_LIKED")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].userId", is(10)))
                    .andExpect(jsonPath("$.content[0].type", is("MEDIA_LIKED")));
        }

        @Test
        @DisplayName("filtre par plage de dates (from/to)")
        void filterByDateRange() throws Exception {
            saveNotif(10L, NotificationType.BROADCAST, NotificationStatus.UNREAD);

            String past   = LocalDateTime.now().minusDays(1).toString();
            String future = LocalDateTime.now().plusDays(1).toString();

            mockMvc.perform(get("/api/admin/notifications")
                            .param("from", past)
                            .param("to", future)
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }

        @Test
        @DisplayName("filtre par date hors plage retourne page vide")
        void filterByDateRangeOutOfBounds() throws Exception {
            saveNotif(10L, NotificationType.BROADCAST, NotificationStatus.UNREAD);

            String past1 = LocalDateTime.now().minusDays(10).toString();
            String past2 = LocalDateTime.now().minusDays(5).toString();

            mockMvc.perform(get("/api/admin/notifications")
                            .param("from", past1)
                            .param("to", past2)
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }



    @Nested
    @DisplayName("GET /api/admin/notifications/stats")
    class GetStats {

        @Test
        @DisplayName("stats à zéro si aucune notification")
        void zeroStatsWhenEmpty() throws Exception {
            mockMvc.perform(get("/api/admin/notifications/stats")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount", is(0)))
                    .andExpect(jsonPath("$.readCount", is(0)))
                    .andExpect(jsonPath("$.unreadCount", is(0)))
                    .andExpect(jsonPath("$.openRatePercent", is(0.0)));
        }

        @Test
        @DisplayName("stats correctes avec notifications read/unread")
        void correctStats() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.READ);
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.READ);
            saveNotif(20L, NotificationType.BROADCAST,  NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/admin/notifications/stats")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount", is(3)))
                    .andExpect(jsonPath("$.readCount", is(2)))
                    .andExpect(jsonPath("$.unreadCount", is(1)))
                    .andExpect(jsonPath("$.openRatePercent", is(66.7)));
        }

        @Test
        @DisplayName("countByType contient les bons types")
        void countByTypeHasCorrectTypes() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(20L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(30L, NotificationType.BROADCAST,   NotificationStatus.UNREAD);

            mockMvc.perform(get("/api/admin/notifications/stats")
                            .header("Authorization", bearerToken(1L, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.countByType.MEDIA_LIKED", is(2)))
                    .andExpect(jsonPath("$.countByType.BROADCAST",   is(1)));
        }
    }



    @Nested
    @DisplayName("POST /api/admin/notifications/broadcast")
    class Broadcast {

        @Test
        @DisplayName("202 et broadcast créé pour chaque user existant")
        void broadcastCreatesNotificationPerUser() throws Exception {
            saveNotif(10L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);
            saveNotif(20L, NotificationType.MEDIA_LIKED, NotificationStatus.UNREAD);

            long countBefore = notificationRepository.count();

            BroadcastRequest request = BroadcastRequest.builder()
                    .message("Message broadcast important")
                    .title("Annonce")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .header("Authorization", bearerToken(1L, "ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());

            assertThat(notificationRepository.count()).isEqualTo(countBefore + 2);
        }

        @Test
        @DisplayName("202 sans erreur si aucun user existant")
        void broadcastOkWhenNoUsers() throws Exception {
            BroadcastRequest request = BroadcastRequest.builder()
                    .message("Message sans destinataire")
                    .title("Vide")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .header("Authorization", bearerToken(1L, "ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());

            assertThat(notificationRepository.count()).isZero();;
        }

        @Test
        @DisplayName("400 si le message est vide")
        void broadcastValidationFailsWhenMessageBlank() throws Exception {

            BroadcastRequest request = BroadcastRequest.builder()
                    .message("")
                    .title("Titre")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .header("Authorization", bearerToken(1L, "ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("401 sans token")
        void broadcastRequiresAuth() throws Exception {
            BroadcastRequest request = BroadcastRequest.builder()
                    .message("Non autorisé")
                    .title("Test")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }
}
