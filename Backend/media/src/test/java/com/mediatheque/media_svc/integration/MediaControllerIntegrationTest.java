package com.mediatheque.media_svc.integration;
import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.dto.UpdateMediaRequest;
import com.mediatheque.media_svc.event.MediaEventPublisher;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MediaRepository mediaRepository;

    @MockitoBean

    private MediaEventPublisher mediaEventPublisher;  // ← Kafka mocké, aucune connexion réelle

    private Long availableId;
    private Long pendingId;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();

        Media available = mediaRepository.save(Media.builder()
                .title("Inception").author("Nolan")
                .type(com.mediatheque.media_svc.model.MediaType.FILM)
                .genre("SciFi").releaseYear(2010)
                .status(MediaStatus.AVAILABLE).build());

        Media pending = mediaRepository.save(Media.builder()
                .title("Dune").author("Villeneuve")
                .type(com.mediatheque.media_svc.model.MediaType.FILM)
                .status(MediaStatus.PENDING).build());

        availableId = available.getId();
        pendingId   = pending.getId();
    }


    @Test
    void getAvailable_shouldReturn200_andOnlyAvailable() throws Exception {
        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }

    @Test
    void getById_shouldReturn200_whenExists() throws Exception {
        mockMvc.perform(get("/api/media/" + availableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/media/9999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void submit_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/media/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Interstellar"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void submit_shouldReturn201_withUserToken() throws Exception {
        mockMvc.perform(post("/api/media/submit")
                        .header("Authorization", "Bearer " + JwtTestHelper.userToken())
                        .header("X-User-Id", JwtTestHelper.userId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Interstellar"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void submit_shouldReturn201_withAdminToken() throws Exception {
        mockMvc.perform(post("/api/media/submit")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken())
                        .header("X-User-Id", JwtTestHelper.adminId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Tenet"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void submit_shouldReturn400_whenTitleMissing() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setAuthor("Nolan");
        request.setType(com.mediatheque.media_svc.model.MediaType.FILM);

        mockMvc.perform(post("/api/media/submit")
                        .header("Authorization", "Bearer " + JwtTestHelper.userToken())
                        .header("X-User-Id", JwtTestHelper.userId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void searchByTitle_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/media/search/title?title=inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }

    @Test
    void searchByType_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/media/search/type?type=FILM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchByGenre_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/media/search/genre?genre=SciFi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("SciFi"));
    }

    @Test
    void searchByYear_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/media/search/year?year=2010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].releaseYear").value(2010));
    }


    @Test
    void adminGetAll_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGetAll_shouldReturn403_whenUserToken() throws Exception {
        mockMvc.perform(get("/api/admin/media")
                        .header("Authorization", "Bearer " + JwtTestHelper.userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGetAll_shouldReturn200_withAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/media")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void adminGetPending_shouldReturn200_withAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/media/pending")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune"));
    }

    @Test
    void adminApprove_shouldReturn200_andStatusAvailable() throws Exception {
        mockMvc.perform(put("/api/admin/media/" + pendingId + "/approve")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void adminReject_shouldReturn200_andStatusRejected() throws Exception {
        mockMvc.perform(put("/api/admin/media/" + pendingId + "/reject")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void adminUpdate_shouldReturn200_withChanges() throws Exception {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Inception Directors Cut");

        mockMvc.perform(put("/api/admin/media/" + availableId)
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception Directors Cut"));
    }

    @Test
    void adminDelete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/admin/media/" + availableId)
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminDelete_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/media/9999")
                        .header("Authorization", "Bearer " + JwtTestHelper.adminToken()))
                .andExpect(status().isNotFound());
    }


    private CreateMediaRequest buildRequest(String title) {
        CreateMediaRequest r = new CreateMediaRequest();
        r.setTitle(title);
        r.setAuthor("Nolan");
        r.setType(com.mediatheque.media_svc.model.MediaType.FILM);
        r.setGenre("SciFi");
        r.setReleaseYear(2014);
        r.setDescription("Description test");
        return r;
    }
}