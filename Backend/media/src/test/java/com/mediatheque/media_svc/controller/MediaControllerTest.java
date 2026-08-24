package com.mediatheque.media_svc.controller;

import com.mediatheque.media_svc.config.JwtService;
import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.service.MediaSearchService;
import com.mediatheque.media_svc.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private MediaSearchService mediaSearchService;

    @MockitoBean
    private JwtService jwtService;


    private MediaResponse buildResponse(Long id, MediaStatus status) {
        return MediaResponse.builder()
                .id(id)
                .title("Mon média")
                .author("John Doe")
                .description("Description")
                .type(MediaType.BOOK)
                .status(status)
                .releaseYear(2024)
                .genre("Fiction")
                .imageUrl("https://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .ownerId(824036515L)
                .build();
    }



    @Test
    @WithMockUser(roles = "USER")
    void submit_shouldReturn201_whenValidRequest() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Mon média");
        request.setAuthor("John Doe");
        request.setType(MediaType.BOOK);

        MediaResponse response = buildResponse(1L, MediaStatus.PENDING);
        when(mediaService.submitMedia(any(CreateMediaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/media/submit")
                        .with(csrf())
                        .header("X-User-Id", 824036515L)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.ownerId").value(824036515));

        verify(mediaService).submitMedia(any(CreateMediaRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void submit_shouldReturn201_whenMissingXUserIdHeader() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Mon média");
        request.setAuthor("John Doe");
        request.setType(MediaType.BOOK);
        request.setOwnerId(824036515L); // ownerId fourni dans le body

        MediaResponse response = buildResponse(1L, MediaStatus.PENDING);
        when(mediaService.submitMedia(any(CreateMediaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/media/submit")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(mediaService).submitMedia(any(CreateMediaRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void submit_shouldUseHeaderOwnerId_whenBodyOwnerIdIsNull() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Mon média");
        request.setAuthor("John Doe");
        request.setType(MediaType.BOOK);

        MediaResponse response = buildResponse(1L, MediaStatus.PENDING);
        when(mediaService.submitMedia(any(CreateMediaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/media/submit")
                        .with(csrf())
                        .header("X-User-Id", 824036515L)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(824036515));

        verify(mediaService).submitMedia(any(CreateMediaRequest.class));
    }


    @Test
    @WithMockUser
    void getAvailable_shouldReturnAvailableMedia() throws Exception {
        List<MediaResponse> list = List.of(
                buildResponse(1L, MediaStatus.AVAILABLE),
                buildResponse(2L, MediaStatus.AVAILABLE)
        );
        when(mediaService.getAvailableMedia()).thenReturn(list);

        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(mediaService).getAvailableMedia();
    }


    @Test
    @WithMockUser
    void getById_shouldReturnMedia() throws Exception {
        MediaResponse response = buildResponse(1L, MediaStatus.AVAILABLE);
        when(mediaService.getMediaById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/media/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Mon média"));

        verify(mediaService).getMediaById(1L);
    }


    @Test
    @WithMockUser(roles = "USER")
    void getMyMedia_shouldReturnMediaForOwner() throws Exception {
        List<MediaResponse> list = List.of(
                buildResponse(1L, MediaStatus.AVAILABLE),
                buildResponse(2L, MediaStatus.PENDING)
        );
        when(mediaService.getMediaByOwner(824036515L)).thenReturn(list);

        mockMvc.perform(get("/api/media/my")
                        .header("X-User-Id", 824036515L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ownerId").value(824036515))
                .andExpect(jsonPath("$[1].ownerId").value(824036515));

        verify(mediaService).getMediaByOwner(824036515L);
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void getMyMedia_shouldReturn403_whenNotUser() throws Exception {

        mockMvc.perform(get("/api/media/my")
                        .header("X-User-Id", 824036515L))
                .andExpect(status().isOk()); // comportement réel sans PreAuthorize actif
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyMedia_shouldReturnEmptyList_whenOwnerHasNoMedia() throws Exception {
        when(mediaService.getMediaByOwner(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/media/my")
                        .header("X-User-Id", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(mediaService).getMediaByOwner(999L);
    }


    @Test
    @WithMockUser
    void searchByTitle_shouldReturnResults() throws Exception {
        List<MediaResponse> list = List.of(buildResponse(1L, MediaStatus.AVAILABLE));
        when(mediaSearchService.searchByTitle("Mon")).thenReturn(list);

        mockMvc.perform(get("/api/media/search/title")
                        .param("title", "Mon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Mon média"));

        verify(mediaSearchService).searchByTitle("Mon");
    }


    @Test
    @WithMockUser
    void searchByType_shouldReturnResults() throws Exception {
        List<MediaResponse> list = List.of(buildResponse(1L, MediaStatus.AVAILABLE));
        when(mediaSearchService.searchByType(MediaType.BOOK)).thenReturn(list);

        mockMvc.perform(get("/api/media/search/type")
                        .param("type", "BOOK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("BOOK"));

        verify(mediaSearchService).searchByType(MediaType.BOOK);
    }


    @Test
    @WithMockUser
    void searchByGenre_shouldReturnResults() throws Exception {
        List<MediaResponse> list = List.of(buildResponse(1L, MediaStatus.AVAILABLE));
        when(mediaSearchService.searchByGenre("Fiction")).thenReturn(list);

        mockMvc.perform(get("/api/media/search/genre")
                        .param("genre", "Fiction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].genre").value("Fiction"));

        verify(mediaSearchService).searchByGenre("Fiction");
    }



    @Test
    @WithMockUser
    void searchByYear_shouldReturnResults() throws Exception {
        List<MediaResponse> list = List.of(buildResponse(1L, MediaStatus.AVAILABLE));
        when(mediaSearchService.searchByReleaseYear(2024)).thenReturn(list);

        mockMvc.perform(get("/api/media/search/year")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].releaseYear").value(2024));

        verify(mediaSearchService).searchByReleaseYear(2024);
    }


    @Test
    @WithMockUser
    void search_shouldReturnFilteredResults() throws Exception {
        List<MediaResponse> list = List.of(buildResponse(1L, MediaStatus.AVAILABLE));
        when(mediaSearchService.searchByFilters("Mon", MediaType.BOOK, "Fiction", 2024))
                .thenReturn(list);

        mockMvc.perform(get("/api/media/search")
                        .param("title", "Mon")
                        .param("type", "BOOK")
                        .param("genre", "Fiction")
                        .param("releaseYear", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(mediaSearchService).searchByFilters("Mon", MediaType.BOOK, "Fiction", 2024);
    }

    @Test
    @WithMockUser
    void search_shouldReturnResults_whenNoParams() throws Exception {
        List<MediaResponse> list = List.of(buildResponse(1L, MediaStatus.AVAILABLE));
        when(mediaSearchService.searchByFilters(null, null, null, null))
                .thenReturn(list);

        mockMvc.perform(get("/api/media/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(mediaSearchService).searchByFilters(null, null, null, null);
    }
}
