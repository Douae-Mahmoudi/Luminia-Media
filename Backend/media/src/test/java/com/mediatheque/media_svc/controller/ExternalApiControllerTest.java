package com.mediatheque.media_svc.controller;

import com.mediatheque.media_svc.config.JwtService;
import com.mediatheque.media_svc.dto.ExternalMediaResponse;
import com.mediatheque.media_svc.service.ExternalApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExternalApiController.class)
class ExternalApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExternalApiService externalApiService;

    @MockitoBean
    private JwtService jwtService;


    private ExternalMediaResponse buildResponse(String title) {
        return ExternalMediaResponse.builder()
                .title(title)
                .author("Auteur Test")
                .genre("Fiction")
                .releaseYear(2024)
                .description("Description test")
                .coverUrl("https://example.com/cover.jpg")
                .source("TEST_API")
                .externalId("ext-123")
                .build();
    }


    @Test
    @WithMockUser
    void searchBooks_shouldReturn200_withResults() throws Exception {
        List<ExternalMediaResponse> list = List.of(
                buildResponse("Harry Potter"),
                buildResponse("Le Seigneur des Anneaux")
        );
        when(externalApiService.searchBooks("harry")).thenReturn(list);

        mockMvc.perform(get("/api/external/books")
                        .param("query", "harry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Harry Potter"))
                .andExpect(jsonPath("$[1].title").value("Le Seigneur des Anneaux"));

        verify(externalApiService).searchBooks("harry");
    }

    @Test
    @WithMockUser
    void searchBooks_shouldReturn200_withEmptyList() throws Exception {
        when(externalApiService.searchBooks("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/api/external/books")
                        .param("query", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(externalApiService).searchBooks("xyz");
    }

    @Test
    @WithMockUser
    void searchBooks_shouldReturn400_whenQueryMissing() throws Exception {
        mockMvc.perform(get("/api/external/books"))
                .andExpect(status().is4xxClientError());

        verify(externalApiService, never()).searchBooks(any());
    }


    @Test
    @WithMockUser
    void searchFilms_shouldReturn200_withResults() throws Exception {
        List<ExternalMediaResponse> list = List.of(
                buildResponse("Inception"),
                buildResponse("Interstellar")
        );
        when(externalApiService.searchFilms("nolan")).thenReturn(list);

        mockMvc.perform(get("/api/external/films")
                        .param("query", "nolan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[1].title").value("Interstellar"));

        verify(externalApiService).searchFilms("nolan");
    }

    @Test
    @WithMockUser
    void searchFilms_shouldReturn200_withEmptyList() throws Exception {
        when(externalApiService.searchFilms("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/api/external/films")
                        .param("query", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(externalApiService).searchFilms("xyz");
    }

    @Test
    @WithMockUser
    void searchFilms_shouldReturn400_whenQueryMissing() throws Exception {
        mockMvc.perform(get("/api/external/films"))
                .andExpect(status().is4xxClientError());

        verify(externalApiService, never()).searchFilms(any());
    }


    @Test
    @WithMockUser
    void searchGames_shouldReturn200_withResults() throws Exception {
        List<ExternalMediaResponse> list = List.of(
                buildResponse("The Witcher 3"),
                buildResponse("Elden Ring")
        );
        when(externalApiService.searchGames("rpg")).thenReturn(list);

        mockMvc.perform(get("/api/external/games")
                        .param("query", "rpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("The Witcher 3"))
                .andExpect(jsonPath("$[1].title").value("Elden Ring"));

        verify(externalApiService).searchGames("rpg");
    }

    @Test
    @WithMockUser
    void searchGames_shouldReturn200_withEmptyList() throws Exception {
        when(externalApiService.searchGames("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/api/external/games")
                        .param("query", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(externalApiService).searchGames("xyz");
    }

    @Test
    @WithMockUser
    void searchGames_shouldReturn400_whenQueryMissing() throws Exception {
        mockMvc.perform(get("/api/external/games"))
                .andExpect(status().is4xxClientError());

        verify(externalApiService, never()).searchGames(any());
    }


    @Test
    @WithMockUser
    void searchPodcasts_shouldReturn200_withResults() throws Exception {
        List<ExternalMediaResponse> list = List.of(
                buildResponse("Podcast Tech"),
                buildResponse("Podcast Science")
        );
        when(externalApiService.searchPodcasts("tech")).thenReturn(list);

        mockMvc.perform(get("/api/external/podcasts")
                        .param("query", "tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Podcast Tech"))
                .andExpect(jsonPath("$[1].title").value("Podcast Science"));

        verify(externalApiService).searchPodcasts("tech");
    }

    @Test
    @WithMockUser
    void searchPodcasts_shouldReturn200_withEmptyList() throws Exception {
        when(externalApiService.searchPodcasts("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/api/external/podcasts")
                        .param("query", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(externalApiService).searchPodcasts("xyz");
    }

    @Test
    @WithMockUser
    void searchPodcasts_shouldReturn400_whenQueryMissing() throws Exception {
        mockMvc.perform(get("/api/external/podcasts"))
                .andExpect(status().is4xxClientError());

        verify(externalApiService, never()).searchPodcasts(any());
    }
}