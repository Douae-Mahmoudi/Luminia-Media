package com.mediatheque.media_svc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.event.MediaEventPublisher;
import com.mediatheque.media_svc.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaModerationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MediaRepository mediaRepository;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private MediaEventPublisher mediaEventPublisher;  // ← Kafka mocké

    @BeforeEach
    void clean() {
        mediaRepository.deleteAll();
    }

    private Long submitMedia(String title,
                             com.mediatheque.media_svc.model.MediaType type)
            throws Exception {

        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle(title);
        request.setAuthor("Auteur");
        request.setType(type);

        String response = mockMvc.perform(post("/api/media/submit")
                        .header("Authorization", "Bearer " + JwtTestHelper.userToken())
                        .header("X-User-Id", JwtTestHelper.userId())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void shouldApprovePendingMedia() throws Exception {
        Long id = submitMedia("Zelda",
                com.mediatheque.media_svc.model.MediaType.GAME);

        mockMvc.perform(put("/api/admin/media/" + id + "/approve")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void shouldRejectPendingMedia() throws Exception {
        Long id = submitMedia("Bad Podcast",
                com.mediatheque.media_svc.model.MediaType.PODCAST);

        mockMvc.perform(put("/api/admin/media/" + id + "/reject")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldListPendingMedia() throws Exception {
        submitMedia("Film A", com.mediatheque.media_svc.model.MediaType.FILM);
        submitMedia("Film B", com.mediatheque.media_svc.model.MediaType.FILM);

        mockMvc.perform(get("/api/admin/media/pending")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldDeleteMedia() throws Exception {
        Long id = submitMedia("A supprimer",
                com.mediatheque.media_svc.model.MediaType.BOOK);

        mockMvc.perform(delete("/api/admin/media/" + id)
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/media/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenUserTriesAdminEndpoint() throws Exception {
        Long id = submitMedia("Film protégé",
                com.mediatheque.media_svc.model.MediaType.FILM);

        mockMvc.perform(put("/api/admin/media/" + id + "/approve")
                        .header("Authorization", "Bearer " + JwtTestHelper.userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldApprovedMediaAppearInPublicList() throws Exception {
        Long id = submitMedia("Film public",
                com.mediatheque.media_svc.model.MediaType.FILM);

        mockMvc.perform(put("/api/admin/media/" + id + "/approve")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Film public"));
    }
}