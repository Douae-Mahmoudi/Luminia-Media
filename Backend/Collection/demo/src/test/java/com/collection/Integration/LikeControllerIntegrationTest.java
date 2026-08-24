package com.collection.Integration;

import com.collection.config.security.JwtUtil;
import com.collection.domain.Like;
import com.collection.usecase.like.GetLikesCountUseCase;
import com.collection.usecase.like.GetUserLikeStatusUseCase;
import com.collection.usecase.like.LikeMediaUseCase;
import com.collection.usecase.like.UnlikeMediaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {
                com.collection.controller.LikeController.class,
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
class LikeControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private LikeMediaUseCase          likeMediaUseCase;
    @MockitoBean private UnlikeMediaUseCase        unlikeMediaUseCase;
    @MockitoBean private GetLikesCountUseCase      getLikesCountUseCase;
    @MockitoBean private GetUserLikeStatusUseCase  getUserLikeStatusUseCase;
    @MockitoBean private JwtUtil                   jwtUtil;

    private Like fakeLike;
    private Like fakeExternalLike;

    @BeforeEach
    void setUp() {
        fakeLike         = new Like("like-001", "user-72", "media-001");
        fakeExternalLike = new Like("like-002", "user-72", "tmdb-movie-550");
    }



    @Test
    void like_shouldReturn200() throws Exception {
        when(likeMediaUseCase.execute(any())).thenReturn(fakeLike);

        mockMvc.perform(post("/api/likes/media-001")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("like-001"))
                .andExpect(jsonPath("$.userId").value("user-72"))
                .andExpect(jsonPath("$.mediaId").value("media-001"));
    }

    @Test
    void like_withOptionalHeaders_shouldReturn200() throws Exception {
        when(likeMediaUseCase.execute(any())).thenReturn(fakeLike);

        mockMvc.perform(post("/api/likes/media-001")
                        .header("X-User-Id",          "user-72")
                        .header("X-User-Numeric-Id",  "72")
                        .header("X-Username",          "john")
                        .header("X-Media-Title",       "Inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("like-001"));
    }

    @Test
    void like_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/likes/media-001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void like_alreadyLiked_shouldReturn500() throws Exception {
        when(likeMediaUseCase.execute(any()))
                .thenThrow(new RuntimeException("Media already liked"));

        mockMvc.perform(post("/api/likes/media-001")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void unlike_shouldReturn204() throws Exception {
        doNothing().when(unlikeMediaUseCase).execute("user-72", "media-001");

        mockMvc.perform(delete("/api/likes/media-001")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unlike_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/likes/media-001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unlike_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Like not found"))
                .when(unlikeMediaUseCase).execute("user-72", "media-999");

        mockMvc.perform(delete("/api/likes/media-999")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getCount_shouldReturn200WithCount() throws Exception {
        when(getLikesCountUseCase.execute("media-001")).thenReturn(42L);

        mockMvc.perform(get("/api/likes/media-001/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(42));
    }

    @Test
    void getCount_noLikes_shouldReturn0() throws Exception {
        when(getLikesCountUseCase.execute("media-001")).thenReturn(0L);

        mockMvc.perform(get("/api/likes/media-001/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }


    @Test
    void getUserStatus_liked_shouldReturnTrue() throws Exception {
        when(getUserLikeStatusUseCase.execute("user-72", "media-001")).thenReturn(true);

        mockMvc.perform(get("/api/likes/media-001/user")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void getUserStatus_notLiked_shouldReturnFalse() throws Exception {
        when(getUserLikeStatusUseCase.execute("user-72", "media-001")).thenReturn(false);

        mockMvc.perform(get("/api/likes/media-001/user")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void getUserStatus_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/likes/media-001/user"))
                .andExpect(status().isBadRequest());
    }



    @Test
    void likeExternal_shouldReturn200() throws Exception {
        when(likeMediaUseCase.execute(any())).thenReturn(fakeExternalLike);

        mockMvc.perform(post("/api/likes/external/tmdb-movie-550")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("like-002"))
                .andExpect(jsonPath("$.userId").value("user-72"))
                .andExpect(jsonPath("$.mediaId").value("tmdb-movie-550"));
    }

    @Test
    void likeExternal_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/likes/external/tmdb-movie-550"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void likeExternal_alreadyLiked_shouldReturn500() throws Exception {
        when(likeMediaUseCase.execute(any()))
                .thenThrow(new RuntimeException("Media already liked"));

        mockMvc.perform(post("/api/likes/external/tmdb-movie-550")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void unlikeExternal_shouldReturn204() throws Exception {
        doNothing().when(unlikeMediaUseCase).execute("user-72", "tmdb-movie-550");

        mockMvc.perform(delete("/api/likes/external/tmdb-movie-550")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unlikeExternal_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/likes/external/tmdb-movie-550"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unlikeExternal_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Like not found"))
                .when(unlikeMediaUseCase).execute("user-72", "tmdb-movie-999");

        mockMvc.perform(delete("/api/likes/external/tmdb-movie-999")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getExternalCount_shouldReturn200WithCount() throws Exception {
        when(getLikesCountUseCase.execute("tmdb-movie-550")).thenReturn(128L);

        mockMvc.perform(get("/api/likes/external/tmdb-movie-550/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(128));
    }

    @Test
    void getExternalCount_noLikes_shouldReturn0() throws Exception {
        when(getLikesCountUseCase.execute("tmdb-movie-550")).thenReturn(0L);

        mockMvc.perform(get("/api/likes/external/tmdb-movie-550/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }


    @Test
    void getExternalUserStatus_liked_shouldReturnTrue() throws Exception {
        when(getUserLikeStatusUseCase.execute("user-72", "tmdb-movie-550")).thenReturn(true);

        mockMvc.perform(get("/api/likes/external/tmdb-movie-550/user")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void getExternalUserStatus_notLiked_shouldReturnFalse() throws Exception {
        when(getUserLikeStatusUseCase.execute("user-72", "tmdb-movie-550")).thenReturn(false);

        mockMvc.perform(get("/api/likes/external/tmdb-movie-550/user")
                        .header("X-User-Id", "user-72"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void getExternalUserStatus_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/likes/external/tmdb-movie-550/user"))
                .andExpect(status().isBadRequest());
    }
}
