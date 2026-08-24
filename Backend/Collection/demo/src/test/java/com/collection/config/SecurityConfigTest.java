package com.collection.config;

import com.collection.config.security.JwtUtil;
import com.collection.event.CommentAddedEvent;
import com.collection.event.MediaLikedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KafkaTemplate<String, CommentAddedEvent> kafkaTemplateComment;

    @MockitoBean
    private KafkaTemplate<String, MediaLikedEvent> kafkaTemplateMediaLiked;

    @MockitoBean
    private JwtUtil jwtUtil;


    @Test
    @DisplayName("GET /api/collections without token → 403")
    void apiCollections_noToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/collections"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/favorites without token → 403")
    void apiFavorites_noToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/favorites without token → 403")
    void apiFavoritesPost_noToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/favorites/media001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/favorites without token → 403")
    void apiFavoritesDelete_noToken_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/favorites/media001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/watchlist without token → 403")
    void apiWatchlist_noToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/likes/{id}/count without token → 403")
    void apiLikesCount_noToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/likes/media001/count"))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("GET /actuator/health without token → 200")
    void actuatorHealth_noToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /v3/api-docs without token → 200")
    void apiDocs_noToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /swagger-ui/index.html without token → 200")
    void swaggerUi_noToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
