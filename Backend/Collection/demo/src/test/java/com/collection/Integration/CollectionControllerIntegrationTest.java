package com.collection.Integration;

import com.collection.config.security.JwtUtil;
import com.collection.domain.Collection;
import com.collection.usecase.collection.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {
                com.collection.controller.CollectionController.class,
                com.collection.config.GlobalExceptionHandler.class
        },
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = com.collection.config.SecurityConfig.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = com.collection.config.security.JwtAuthenticationFilter.class
                )
        }
)
class CollectionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateCollectionUseCase createCollectionUseCase;
    @MockitoBean private GetCollectionUseCase getCollectionUseCase;
    @MockitoBean private AddMediaToCollectionUseCase addMediaToCollectionUseCase;
    @MockitoBean private RemoveMediaFromCollectionUseCase removeMediaFromCollectionUseCase;
    @MockitoBean private DeleteCollectionUseCase deleteCollectionUseCase; // ← AJOUTÉ
    @MockitoBean private JwtUtil jwtUtil;

    private Collection fakeCollection;

    private void setAuthentication(String userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        fakeCollection = new Collection("col-001", "user-72", "Ma collection", "Films préférés", true);
        setAuthentication("user-72");
    }


    @Test
    void createCollection_shouldReturn200() throws Exception {
        when(createCollectionUseCase.execute(any())).thenReturn(fakeCollection);

        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ma collection\",\"description\":\"Films préférés\",\"isPublic\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("col-001"))
                .andExpect(jsonPath("$.name").value("Ma collection"));
    }

    @Test
    void createCollection_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ma collection\",\"description\":\"Films préférés\",\"isPublic\":true}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCollection_useCaseThrows_shouldReturn500() throws Exception {
        when(createCollectionUseCase.execute(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ma collection\",\"description\":\"Films préférés\",\"isPublic\":true}"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getCollectionById_shouldReturn200() throws Exception {
        when(getCollectionUseCase.getById("col-001")).thenReturn(fakeCollection);

        mockMvc.perform(get("/api/collections/col-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("col-001"))
                .andExpect(jsonPath("$.userId").value("user-72"));
    }

    @Test
    void getCollectionById_notFound_shouldReturn500() throws Exception {
        when(getCollectionUseCase.getById("not-exist"))
                .thenThrow(new RuntimeException("Collection not found"));

        mockMvc.perform(get("/api/collections/not-exist"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getCollectionsByUserId_shouldReturn200() throws Exception {
        when(getCollectionUseCase.getByUserId("user-72")).thenReturn(List.of(fakeCollection));

        mockMvc.perform(get("/api/collections/user/user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-72"));
    }

    @Test
    void getCollectionsByUserId_empty_shouldReturnEmptyList() throws Exception {
        when(getCollectionUseCase.getByUserId("user-unknown")).thenReturn(List.of());

        mockMvc.perform(get("/api/collections/user/user-unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }



    @Test
    void getPublicCollections_shouldReturn200() throws Exception {
        when(getCollectionUseCase.getPublicByUserId("user-72")).thenReturn(List.of(fakeCollection));

        mockMvc.perform(get("/api/collections/user/user-72/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isPublic").value(true));
    }


    @Test
    void getAllPublicCollections_shouldReturn200() throws Exception {
        when(getCollectionUseCase.getAllPublic()).thenReturn(List.of(fakeCollection));

        mockMvc.perform(get("/api/collections/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isPublic").value(true));
    }

    @Test
    void getPublicCollections_empty_shouldReturnEmptyList() throws Exception {
        when(getCollectionUseCase.getPublicByUserId("user-unknown")).thenReturn(List.of());

        mockMvc.perform(get("/api/collections/user/user-unknown/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }




    @Test
    void getAllPublicCollections_empty_shouldReturnEmptyList() throws Exception {
        when(getCollectionUseCase.getAllPublic()).thenReturn(List.of());

        mockMvc.perform(get("/api/collections/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void addMedia_shouldReturn200() throws Exception {
        fakeCollection.addMedia("film-001");
        when(addMediaToCollectionUseCase.execute(any())).thenReturn(fakeCollection);

        mockMvc.perform(post("/api/collections/col-001/media/film-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaIds[0]").value("film-001"));
    }

    @Test
    void addMedia_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/collections/col-001/media/film-001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addMedia_accessDenied_shouldReturn500() throws Exception {
        setAuthentication("user-99");
        when(addMediaToCollectionUseCase.execute(any()))
                .thenThrow(new RuntimeException("Access denied"));

        mockMvc.perform(post("/api/collections/col-001/media/film-001"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void removeMedia_shouldReturn200() throws Exception {
        when(removeMediaFromCollectionUseCase.execute(any())).thenReturn(fakeCollection);

        mockMvc.perform(delete("/api/collections/col-001/media/film-001"))
                .andExpect(status().isOk());
    }

    @Test
    void removeMedia_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/collections/col-001/media/film-001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void removeMedia_notFound_shouldReturn500() throws Exception {
        when(removeMediaFromCollectionUseCase.execute(any()))
                .thenThrow(new RuntimeException("Collection not found"));

        mockMvc.perform(delete("/api/collections/col-001/media/film-001"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void deleteCollection_shouldReturn204() throws Exception {
        doNothing().when(deleteCollectionUseCase).execute("col-001");

        mockMvc.perform(delete("/api/collections/col-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCollection_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Collection not found"))
                .when(deleteCollectionUseCase).execute("not-exist");

        mockMvc.perform(delete("/api/collections/not-exist"))
                .andExpect(status().isInternalServerError());
    }
}