package com.collection.controller;

import com.collection.domain.Favorite;
import com.collection.usecase.favorite.AddFavoriteUseCase;
import com.collection.usecase.favorite.RemoveFavoriteUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AddFavoriteUseCase addFavoriteUseCase;

    @Mock
    private RemoveFavoriteUseCase removeFavoriteUseCase;

    @InjectMocks
    private FavoriteController favoriteController;

    private final String USER_ID = "user-123";
    private final String MEDIA_ID = "media-456";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(favoriteController).build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void addFavorite_ShouldReturnFavorite() throws Exception {
        Favorite favorite = new Favorite("fav-uuid", USER_ID, MEDIA_ID);
        when(addFavoriteUseCase.execute(any())).thenReturn(favorite);

        mockMvc.perform(post("/api/favorites/{mediaId}", MEDIA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.mediaId").value(MEDIA_ID));

        verify(addFavoriteUseCase).execute(any(AddFavoriteUseCase.Input.class));
    }

    @Test
    void removeFavorite_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/favorites/{mediaId}", MEDIA_ID))
                .andExpect(status().isNoContent());

        // On vérifie que le UseCase a été appelé avec les bons IDs extraits
        verify(removeFavoriteUseCase).execute(USER_ID, MEDIA_ID);
    }
}