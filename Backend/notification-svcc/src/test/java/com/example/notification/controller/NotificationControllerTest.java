package com.example.notification.controller;

import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.request.PreferenceUpdateRequest;
import com.example.notification.dto.response.BadgeCountResponse;
import com.example.notification.dto.response.NotificationResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.dto.response.PreferenceResponse;
import com.example.notification.service.NotificationService;
import com.example.notification.service.PreferenceService;
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
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController")
class NotificationControllerTest {

    private static final Long USER_ID = 42L;

    @Mock private NotificationService notificationService;
    @Mock private PreferenceService preferenceService;

    @InjectMocks
    private NotificationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(Long.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return USER_ID;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        principalResolver,
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }


    @Nested
    @DisplayName("GET /api/notifications")
    class GetMyNotificationsTests {

        @Test
        @DisplayName("retourne 200 avec la page de notifications")
        void getMyNotifications_returns200WithPage() throws Exception {
            NotificationResponse notif = buildNotifResponse(1L, false);
            PageResponse<NotificationResponse> page = buildPage(List.of(notif), 0, 1, 1, true);
            when(notificationService.getMyNotifications(eq(USER_ID), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        @DisplayName("délègue au service avec le userId du principal")
        void getMyNotifications_delegatesWithUserId() throws Exception {
            when(notificationService.getMyNotifications(any(), any()))
                    .thenReturn(buildPage(List.of(), 0, 0, 0, true));

            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().isOk());

            verify(notificationService).getMyNotifications(eq(USER_ID), any(Pageable.class));
        }

        @Test
        @DisplayName("transmet les paramètres de pagination page/size")
        void getMyNotifications_forwardsPageableParams() throws Exception {
            when(notificationService.getMyNotifications(any(), any()))
                    .thenReturn(buildPage(List.of(), 0, 0, 0, true));

            mockMvc.perform(get("/api/notifications")
                            .param("page", "2")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            verify(notificationService).getMyNotifications(
                    eq(USER_ID),
                    argThat(p -> p.getPageNumber() == 2 && p.getPageSize() == 10)
            );
        }

        @Test
        @DisplayName("retourne une page vide avec 200")
        void getMyNotifications_returnsEmptyPage() throws Exception {
            when(notificationService.getMyNotifications(any(), any()))
                    .thenReturn(buildPage(List.of(), 0, 0, 0, true));

            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }


    @Nested
    @DisplayName("GET /api/notifications/badge")
    class GetBadgeCountTests {

        @Test
        @DisplayName("retourne 200 avec le badge count")
        void getBadgeCount_returns200WithCount() throws Exception {
            when(notificationService.getBadgeCount(USER_ID))
                    .thenReturn(BadgeCountResponse.builder().unreadCount(7L).build());

            mockMvc.perform(get("/api/notifications/badge"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(7));
        }

        @Test
        @DisplayName("délègue au service avec le userId du principal")
        void getBadgeCount_delegatesWithUserId() throws Exception {
            when(notificationService.getBadgeCount(USER_ID))
                    .thenReturn(BadgeCountResponse.builder().unreadCount(0L).build());

            mockMvc.perform(get("/api/notifications/badge"))
                    .andExpect(status().isOk());

            verify(notificationService).getBadgeCount(USER_ID);
        }

        @Test
        @DisplayName("retourne 0 quand aucune notification non lue")
        void getBadgeCount_returnsZeroWhenAllRead() throws Exception {
            when(notificationService.getBadgeCount(USER_ID))
                    .thenReturn(BadgeCountResponse.builder().unreadCount(0L).build());

            mockMvc.perform(get("/api/notifications/badge"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(0));
        }
    }


    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkOneAsReadTests {

        @Test
        @DisplayName("retourne 204 No Content")
        void markOneAsRead_returns204() throws Exception {
            mockMvc.perform(patch("/api/notifications/5/read"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("délègue au service avec le bon notificationId et userId")
        void markOneAsRead_delegatesWithCorrectIds() throws Exception {
            mockMvc.perform(patch("/api/notifications/5/read"))
                    .andExpect(status().isNoContent());

            verify(notificationService).markAsRead(5L, USER_ID);
        }

        @Test
        @DisplayName("le body de la réponse est vide")
        void markOneAsRead_returnsEmptyBody() throws Exception {
            mockMvc.perform(patch("/api/notifications/99/read"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("appelle markAsRead exactement une fois")
        void markOneAsRead_callsServiceOnce() throws Exception {
            mockMvc.perform(patch("/api/notifications/1/read"))
                    .andExpect(status().isNoContent());

            verify(notificationService, times(1)).markAsRead(anyLong(), anyLong());
        }
    }


    @Nested
    @DisplayName("PATCH /api/notifications/read-all")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("retourne 204 No Content")
        void markAllAsRead_returns204() throws Exception {
            mockMvc.perform(patch("/api/notifications/read-all"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("délègue au service avec le userId du principal")
        void markAllAsRead_delegatesWithUserId() throws Exception {
            mockMvc.perform(patch("/api/notifications/read-all"))
                    .andExpect(status().isNoContent());

            verify(notificationService).markAllAsRead(USER_ID);
        }

        @Test
        @DisplayName("le body de la réponse est vide")
        void markAllAsRead_returnsEmptyBody() throws Exception {
            mockMvc.perform(patch("/api/notifications/read-all"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }
    }


    @Nested
    @DisplayName("GET /api/notifications/preferences")
    class GetPreferencesTests {

        @Test
        @DisplayName("retourne 200 avec la liste des préférences")
        void getPreferences_returns200WithList() throws Exception {
            List<PreferenceResponse> prefs = List.of(
                    buildPrefResponse(1L, NotificationType.MEDIA_LIKED, true, false),
                    buildPrefResponse(2L, NotificationType.BROADCAST, false, true)
            );
            when(preferenceService.getMyPreferences(USER_ID)).thenReturn(prefs);

            mockMvc.perform(get("/api/notifications/preferences"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].type").value("MEDIA_LIKED"))
                    .andExpect(jsonPath("$[0].inAppEnabled").value(true))
                    .andExpect(jsonPath("$[0].emailEnabled").value(false))
                    .andExpect(jsonPath("$[1].type").value("BROADCAST"));
        }

        @Test
        @DisplayName("délègue au service avec le userId du principal")
        void getPreferences_delegatesWithUserId() throws Exception {
            when(preferenceService.getMyPreferences(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/notifications/preferences"))
                    .andExpect(status().isOk());

            verify(preferenceService).getMyPreferences(USER_ID);
        }

        @Test
        @DisplayName("retourne une liste vide avec 200")
        void getPreferences_returnsEmptyList() throws Exception {
            when(preferenceService.getMyPreferences(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/notifications/preferences"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }


    @Nested
    @DisplayName("PUT /api/notifications/preferences")
    class UpdatePreferenceTests {

        @Test
        @DisplayName("retourne 200 avec la préférence mise à jour")
        void updatePreference_returns200WithUpdatedPref() throws Exception {
            PreferenceUpdateRequest request = buildUpdateRequest(NotificationType.BROADCAST, true, true);
            PreferenceResponse updated = buildPrefResponse(5L, NotificationType.BROADCAST, true, true);
            when(preferenceService.updatePreference(eq(USER_ID), any(PreferenceUpdateRequest.class)))
                    .thenReturn(updated);

            mockMvc.perform(put("/api/notifications/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.type").value("BROADCAST"))
                    .andExpect(jsonPath("$.inAppEnabled").value(true))
                    .andExpect(jsonPath("$.emailEnabled").value(true));
        }

        @Test
        @DisplayName("délègue au service avec le userId du principal et le bon request")
        void updatePreference_delegatesWithCorrectArgs() throws Exception {
            PreferenceUpdateRequest request = buildUpdateRequest(NotificationType.MEDIA_LIKED, false, true);
            when(preferenceService.updatePreference(any(), any()))
                    .thenReturn(buildPrefResponse(1L, NotificationType.MEDIA_LIKED, false, true));

            mockMvc.perform(put("/api/notifications/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(preferenceService).updatePreference(
                    eq(USER_ID),
                    argThat(r -> r.getType() == NotificationType.MEDIA_LIKED
                            && !r.isInAppEnabled()
                            && r.isEmailEnabled())
            );
        }

        @Test
        @DisplayName("appelle updatePreference exactement une fois")
        void updatePreference_callsServiceOnce() throws Exception {
            when(preferenceService.updatePreference(any(), any()))
                    .thenReturn(buildPrefResponse(1L, NotificationType.BROADCAST, true, false));

            mockMvc.perform(put("/api/notifications/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    buildUpdateRequest(NotificationType.BROADCAST, true, false))))
                    .andExpect(status().isOk());

            verify(preferenceService, times(1)).updatePreference(any(), any());
        }
    }


    private NotificationResponse buildNotifResponse(Long id, boolean read) {
        return NotificationResponse.builder()
                .id(id)
                .userId(USER_ID)
                .type(NotificationType.BROADCAST)
                .status(read ? NotificationStatus.READ : NotificationStatus.UNREAD)
                .message("Test")
                .referenceType(ReferenceType.SYSTEM)
                .createdAt(LocalDateTime.now())
                .read(read)
                .build();
    }

    private PreferenceResponse buildPrefResponse(Long id, NotificationType type,
                                                 boolean inApp, boolean email) {
        return PreferenceResponse.builder()
                .id(id)
                .userId(USER_ID)
                .type(type)
                .inAppEnabled(inApp)
                .emailEnabled(email)
                .build();
    }

    private PreferenceUpdateRequest buildUpdateRequest(NotificationType type,
                                                       boolean inApp, boolean email) {
        return PreferenceUpdateRequest.builder()
                .type(type)
                .inAppEnabled(inApp)
                .emailEnabled(email)
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