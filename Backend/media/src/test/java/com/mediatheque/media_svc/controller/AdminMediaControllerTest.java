package com.mediatheque.media_svc.controller;

import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.dto.UpdateMediaRequest;
import com.mediatheque.media_svc.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminMediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MediaService mediaService;

    private MediaResponse buildResponse(Long id, String title) {
        MediaResponse r = new MediaResponse();
        r.setId(id);
        r.setTitle(title);
        return r;
    }


    @Test
    void getAll_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPending_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/media/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approve_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(put("/api/admin/media/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reject_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(put("/api/admin/media/1/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(put("/api/admin/media/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(delete("/api/admin/media/1"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "USER")
    void getAll_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_shouldReturn200_whenAdmin() throws Exception {
        when(mediaService.getAllMedia())
                .thenReturn(List.of(buildResponse(1L, "Inception")));

        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPending_shouldReturn200_whenAdmin() throws Exception {
        when(mediaService.getPendingMedia())
                .thenReturn(List.of(buildResponse(1L, "Inception")));

        mockMvc.perform(get("/api/admin/media/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_shouldReturn200_whenAdmin() throws Exception {
        when(mediaService.approveMedia(1L))
                .thenReturn(buildResponse(1L, "Inception"));

        mockMvc.perform(put("/api/admin/media/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_shouldReturn200_whenAdmin() throws Exception {
        when(mediaService.rejectMedia(1L))
                .thenReturn(buildResponse(1L, "Inception"));

        mockMvc.perform(put("/api/admin/media/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn200_whenAdmin() throws Exception {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Inception modifié");

        when(mediaService.updateMedia(eq(1L), any(UpdateMediaRequest.class)))
                .thenReturn(buildResponse(1L, "Inception modifié"));

        mockMvc.perform(put("/api/admin/media/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception modifié"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204_whenAdmin() throws Exception {
        doNothing().when(mediaService).deleteMedia(1L);

        mockMvc.perform(delete("/api/admin/media/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_shouldReturnEmptyList_whenNoMedia() throws Exception {
        when(mediaService.getAllMedia()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
