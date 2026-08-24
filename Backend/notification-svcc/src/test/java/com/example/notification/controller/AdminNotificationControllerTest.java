package com.example.notification.controller;

import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.request.BroadcastRequest;
import com.example.notification.dto.response.AdminNotificationResponse;
import com.example.notification.dto.response.NotificationStatsResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.service.AdminNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminNotificationController")
class AdminNotificationControllerTest {

    @Mock
    private AdminNotificationService adminNotificationService;

    @InjectMocks
    private AdminNotificationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /api/admin/notifications")
    class GetAllNotificationsTests {

        @Test
        @DisplayName("retourne 200 avec la page de notifications")
        void getAllNotifications_returns200WithPage() throws Exception {
            AdminNotificationResponse notif = buildAdminResponse(1L, 10L);
            PageResponse<AdminNotificationResponse> page = buildPage(List.of(notif), 0, 1, 1, true);
            when(adminNotificationService.getAllNotifications(any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].userId").value(10))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        @DisplayName("passe les paramètres de filtre au service")
        void getAllNotifications_passesFiltersToService() throws Exception {
            when(adminNotificationService.getAllNotifications(any(), any(), any(), any(), any()))
                    .thenReturn(buildPage(List.of(), 0, 0, 0, true));

            mockMvc.perform(get("/api/admin/notifications")
                            .param("userId", "42")
                            .param("type", "BROADCAST")
                            .param("from", "2024-01-01T00:00:00")
                            .param("to", "2024-12-31T23:59:59"))
                    .andExpect(status().isOk());

            verify(adminNotificationService).getAllNotifications(
                    eq(42L),
                    eq(NotificationType.BROADCAST),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    any(Pageable.class)
            );
        }

        @Test
        @DisplayName("fonctionne sans paramètres de filtre (tous null)")
        void getAllNotifications_worksWithNoFilters() throws Exception {
            when(adminNotificationService.getAllNotifications(any(), any(), any(), any(), any()))
                    .thenReturn(buildPage(List.of(), 0, 0, 0, true));

            mockMvc.perform(get("/api/admin/notifications"))
                    .andExpect(status().isOk());

            verify(adminNotificationService).getAllNotifications(
                    isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
            );
        }

        @Test
        @DisplayName("retourne une page vide avec 200")
        void getAllNotifications_returnsEmptyPage() throws Exception {
            when(adminNotificationService.getAllNotifications(any(), any(), any(), any(), any()))
                    .thenReturn(buildPage(List.of(), 0, 0, 0, true));

            mockMvc.perform(get("/api/admin/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("passe le Pageable avec les paramètres page/size")
        void getAllNotifications_forwardsPageableParameters() throws Exception {
            when(adminNotificationService.getAllNotifications(any(), any(), any(), any(), any()))
                    .thenReturn(buildPage(List.of(), 1, 0, 0, true));

            mockMvc.perform(get("/api/admin/notifications")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk());

            verify(adminNotificationService).getAllNotifications(
                    any(), any(), any(), any(),
                    argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 5)
            );
        }
    }

    @Nested
    @DisplayName("GET /api/admin/notifications/stats")
    class GetStatsTests {

        @Test
        @DisplayName("retourne 200 avec les statistiques")
        void getStats_returns200WithStats() throws Exception {
            NotificationStatsResponse stats = NotificationStatsResponse.builder()
                    .totalCount(100L)
                    .readCount(75L)
                    .unreadCount(25L)
                    .openRatePercent(75.0)
                    .countByType(Map.of("BROADCAST", 50L, "MEDIA_LIKED", 50L))
                    .build();
            when(adminNotificationService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/api/admin/notifications/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(100))
                    .andExpect(jsonPath("$.readCount").value(75))
                    .andExpect(jsonPath("$.unreadCount").value(25))
                    .andExpect(jsonPath("$.openRatePercent").value(75.0))
                    .andExpect(jsonPath("$.countByType.BROADCAST").value(50));
        }

        @Test
        @DisplayName("appelle getStats() du service exactement une fois")
        void getStats_callsServiceOnce() throws Exception {
            when(adminNotificationService.getStats()).thenReturn(
                    NotificationStatsResponse.builder()
                            .totalCount(0L).readCount(0L).unreadCount(0L)
                            .openRatePercent(0.0).countByType(Map.of())
                            .build()
            );

            mockMvc.perform(get("/api/admin/notifications/stats"))
                    .andExpect(status().isOk());

            verify(adminNotificationService, times(1)).getStats();
        }

        @Test
        @DisplayName("retourne 200 avec openRatePercent à 0.0 quand aucune notification")
        void getStats_returnsZeroStatsWhenEmpty() throws Exception {
            when(adminNotificationService.getStats()).thenReturn(
                    NotificationStatsResponse.builder()
                            .totalCount(0L).readCount(0L).unreadCount(0L)
                            .openRatePercent(0.0).countByType(Map.of())
                            .build()
            );

            mockMvc.perform(get("/api/admin/notifications/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(0))
                    .andExpect(jsonPath("$.openRatePercent").value(0.0));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/notifications/broadcast")
    class BroadcastTests {

        @Test
        @DisplayName("retourne 202 Accepted après un broadcast valide")
        void broadcast_returns202() throws Exception {
            BroadcastRequest request = BroadcastRequest.builder()
                    .message("Mise à jour importante")
                    .title("Annonce")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("délègue au service avec le bon BroadcastRequest")
        void broadcast_delegatesToServiceWithCorrectRequest() throws Exception {
            BroadcastRequest request = BroadcastRequest.builder()
                    .message("Maintenance prévue ce soir")
                    .title("Info importante")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());

            verify(adminNotificationService).broadcast(
                    argThat(r -> r.getMessage().equals("Maintenance prévue ce soir")
                            && r.getTitle().equals("Info importante"))
            );
        }

        @Test
        @DisplayName("retourne 202 et le body est vide")
        void broadcast_returnsEmptyBody() throws Exception {
            BroadcastRequest request = BroadcastRequest.builder()
                    .message("Hello")
                    .title("Test")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("appelle broadcast() du service exactement une fois")
        void broadcast_callsServiceOnce() throws Exception {
            BroadcastRequest request = BroadcastRequest.builder()
                    .message("msg")
                    .title("titre")
                    .build();

            mockMvc.perform(post("/api/admin/notifications/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());

            verify(adminNotificationService, times(1)).broadcast(any());
        }
    }

    private AdminNotificationResponse buildAdminResponse(Long id, Long userId) {
        return AdminNotificationResponse.builder()
                .id(id)
                .userId(userId)
                .type(NotificationType.BROADCAST)
                .status(NotificationStatus.UNREAD)
                .message("Test message")
                .referenceType(ReferenceType.SYSTEM)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private <T> PageResponse<T> buildPage(List<T> content, int currentPage,
                                          int totalPages, long totalElements, boolean last) {
        return PageResponse.<T>builder()
                .content(content)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .last(last)
                .build();
    }
}
