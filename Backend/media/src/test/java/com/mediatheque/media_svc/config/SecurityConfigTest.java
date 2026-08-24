package com.mediatheque.media_svc.config;

import com.mediatheque.media_svc.controller.AdminMediaController;
import com.mediatheque.media_svc.controller.ExternalApiController;
import com.mediatheque.media_svc.controller.MediaController;
import com.mediatheque.media_svc.service.ExternalApiService;
import com.mediatheque.media_svc.service.MediaSearchService;
import com.mediatheque.media_svc.service.MediaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {MediaController.class, AdminMediaController.class, ExternalApiController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean JwtAuthFilter      jwtAuthFilter;
    @MockitoBean JwtService         jwtService;
    @MockitoBean MediaService       mediaService;
    @MockitoBean MediaSearchService mediaSearchService;
    @MockitoBean ExternalApiService externalApiService;

    @BeforeEach
    void passFilterChain() throws Exception {
        doAnswer(inv -> {
            inv.getArgument(2, FilterChain.class)
                    .doFilter(inv.getArgument(0, ServletRequest.class),
                            inv.getArgument(1, ServletResponse.class));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }


    @Test
    void getMedia_public() throws Exception {
        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk());
    }

    @Test
    void getExternal_public() throws Exception {
        mockMvc.perform(get("/api/external/books").param("query", "test"))
                .andExpect(status().isOk());
    }


    @Test
    void submit_blockedWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/media/submit")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void submit_allowedForUser() throws Exception {
        mockMvc.perform(post("/api/media/submit")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()); // 400 = validation OK, sécurité passée ✅
    }

    @Test
    void admin_blockedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void admin_forbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_allowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser
    void noSessionCookie() throws Exception {
        mockMvc.perform(get("/api/media"))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}