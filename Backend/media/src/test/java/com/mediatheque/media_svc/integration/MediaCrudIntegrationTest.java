package com.mediatheque.media_svc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaCrudIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MediaRepository mediaRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void clean() {
        mediaRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoAvailableMedia() throws Exception {
        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldSubmitMediaAndReturnPending() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Inception");
        request.setAuthor("Christopher Nolan");
        request.setType(MediaType.FILM);

        mockMvc.perform(post("/api/media/submit")
                        .header("X-User-Id", "1")   // ← ajouter
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetMediaById() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Dune");
        request.setAuthor("Frank Herbert");
        request.setType(MediaType.BOOK);

        String response = mockMvc.perform(post("/api/media/submit")
                        .header("X-User-Id", "1")   // ← ajouter
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/media/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void shouldReturn404WhenMediaNotFound() throws Exception {
        mockMvc.perform(get("/api/media/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenSubmitWithoutToken() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Film sans token");
        request.setAuthor("Auteur");
        request.setType(MediaType.FILM);

        mockMvc.perform(post("/api/media/submit")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}