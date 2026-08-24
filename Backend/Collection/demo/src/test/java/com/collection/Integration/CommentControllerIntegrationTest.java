package com.collection.Integration;

import com.collection.config.security.JwtUtil;
import com.collection.domain.Comment;
import com.collection.usecase.comment.AddCommentUseCase;
import com.collection.usecase.comment.DeleteCommentUseCase;
import com.collection.usecase.comment.GetCommentsByMediaUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = com.collection.controller.CommentController.class,
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
class CommentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AddCommentUseCase         addCommentUseCase;
    @MockitoBean private DeleteCommentUseCase      deleteCommentUseCase;
    @MockitoBean private GetCommentsByMediaUseCase getCommentsByMediaUseCase; // ← AJOUTÉ
    @MockitoBean private JwtUtil                   jwtUtil;

    private Comment fakeComment;

    @BeforeEach
    void setUp() {
        fakeComment = new Comment("cmt-001", "user-72", "media-001", "Super film !");
    }


    @Test
    void addComment_shouldReturn200() throws Exception {
        when(addCommentUseCase.execute(any())).thenReturn(fakeComment);

        mockMvc.perform(post("/api/comments")
                        .header("X-User-Id", "user-72")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaId\":\"media-001\",\"content\":\"Super film !\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cmt-001"))
                .andExpect(jsonPath("$.userId").value("user-72"))
                .andExpect(jsonPath("$.mediaId").value("media-001"))
                .andExpect(jsonPath("$.content").value("Super film !"));
    }

    @Test
    void addComment_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaId\":\"media-001\",\"content\":\"Super film !\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_blankContent_shouldReturn500() throws Exception {
        when(addCommentUseCase.execute(any()))
                .thenThrow(new IllegalArgumentException("Content cannot be blank"));

        mockMvc.perform(post("/api/comments")
                        .header("X-User-Id", "user-72")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaId\":\"media-001\",\"content\":\"\"}"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getByMedia_shouldReturn200() throws Exception {
        when(getCommentsByMediaUseCase.execute("media-001")).thenReturn(List.of(fakeComment));

        mockMvc.perform(get("/api/comments").param("mediaId", "media-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cmt-001"))
                .andExpect(jsonPath("$[0].mediaId").value("media-001"));
    }

    @Test
    void getByMedia_empty_shouldReturnEmptyList() throws Exception {
        when(getCommentsByMediaUseCase.execute("media-unknown")).thenReturn(List.of());

        mockMvc.perform(get("/api/comments").param("mediaId", "media-unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void getByExternalMedia_shouldReturn200() throws Exception {
        when(getCommentsByMediaUseCase.execute("tmdb-movie-550")).thenReturn(List.of(fakeComment));

        mockMvc.perform(get("/api/comments/external").param("externalKey", "tmdb-movie-550"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cmt-001"));
    }


    @Test
    void addExternalComment_shouldReturn200() throws Exception {
        when(addCommentUseCase.execute(any())).thenReturn(fakeComment);

        mockMvc.perform(post("/api/comments/external")
                        .header("X-User-Id", "user-72")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaId\":\"tmdb-movie-550\",\"content\":\"Super film !\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cmt-001"));
    }

    @Test
    void addExternalComment_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/comments/external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaId\":\"tmdb-movie-550\",\"content\":\"Super film !\"}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteComment_shouldReturn204() throws Exception {
        doNothing().when(deleteCommentUseCase).execute("cmt-001", "user-72");

        mockMvc.perform(delete("/api/comments/cmt-001")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/comments/cmt-001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_notOwner_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Not authorized"))
                .when(deleteCommentUseCase).execute("cmt-001", "user-other");

        mockMvc.perform(delete("/api/comments/cmt-001")
                        .header("X-User-Id", "user-other"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void deleteExternalComment_shouldReturn204() throws Exception {
        doNothing().when(deleteCommentUseCase).execute("cmt-001", "user-72");

        mockMvc.perform(delete("/api/comments/external/cmt-001")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExternalComment_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/comments/external/cmt-001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteExternalComment_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Comment not found"))
                .when(deleteCommentUseCase).execute("cmt-001", "user-72");

        mockMvc.perform(delete("/api/comments/external/cmt-001")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isInternalServerError());
    }
}