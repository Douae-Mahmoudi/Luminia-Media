package com.collection.controller;

import com.collection.domain.Like;
import com.collection.usecase.like.LikeMediaUseCase;
import com.collection.usecase.like.UnlikeMediaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    private MockMvc mockMvc;

    @Mock private LikeMediaUseCase likeMediaUseCase;
    @Mock private UnlikeMediaUseCase unlikeMediaUseCase;

    @InjectMocks
    private LikeController controller;

    private final String USER_ID = "user-123";
    private final String MEDIA_ID = "movie-456";
    private final String LIKE_ID = "like-789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void like_ShouldReturnOkWithLike() throws Exception {
        Like expectedLike = new Like(LIKE_ID, USER_ID, MEDIA_ID);
        when(likeMediaUseCase.execute(any(LikeMediaUseCase.Input.class))).thenReturn(expectedLike);

        mockMvc.perform(post("/api/likes/{mediaId}", MEDIA_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LIKE_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.mediaId").value(MEDIA_ID));

        // On vérifie que l'Input a été créé avec les bonnes valeurs
        verify(likeMediaUseCase).execute(argThat(input ->
                input.userId().equals(USER_ID) && input.mediaId().equals(MEDIA_ID)
        ));
    }

    @Test
    void unlike_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/likes/{mediaId}", MEDIA_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNoContent());

        // Vérifie l'appel au Use Case de suppression
        verify(unlikeMediaUseCase, times(1)).execute(USER_ID, MEDIA_ID);
    }
}