package com.collection.Integration;

import com.collection.config.security.JwtUtil;
import com.collection.domain.Favorite;
import com.collection.usecase.favorite.AddFavoriteUseCase;
import com.collection.usecase.favorite.GetFavoritesUseCase;
import com.collection.usecase.favorite.RemoveFavoriteUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
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
        controllers = com.collection.controller.FavoriteController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.collection.config.SecurityConfig.class
        )
)
class FavoriteControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AddFavoriteUseCase    addFavoriteUseCase;
    @MockitoBean private RemoveFavoriteUseCase removeFavoriteUseCase;
    @MockitoBean private GetFavoritesUseCase   getFavoritesUseCase;
    @MockitoBean private JwtUtil               jwtUtil;

    private Favorite fakeInternalFavorite;
    private Favorite fakeExternalFavorite;

    private void setAuthentication(String userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        fakeInternalFavorite = new Favorite("fav-001", "user-72", "media001");   // ← sans tiret
        fakeExternalFavorite = new Favorite("fav-002", "user-72", "tmdb-movie-550");
        setAuthentication("user-72");
    }


    @Test
    void getFavorites_shouldReturn200WithList() throws Exception {
        when(getFavoritesUseCase.execute("user-72"))
                .thenReturn(List.of(fakeInternalFavorite, fakeExternalFavorite));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("fav-001"))
                .andExpect(jsonPath("$[1].id").value("fav-002"));
    }

    @Test
    void getFavorites_emptyList_shouldReturn200() throws Exception {
        when(getFavoritesUseCase.execute("user-72")).thenReturn(List.of());

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFavorites_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void addFavorite_shouldReturn200() throws Exception {
        when(addFavoriteUseCase.execute(any())).thenReturn(fakeInternalFavorite);

        mockMvc.perform(post("/api/favorites/media001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("fav-001"))
                .andExpect(jsonPath("$.userId").value("user-72"))
                .andExpect(jsonPath("$.mediaId").value("media001"));
    }

    @Test
    void addFavorite_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/favorites/media001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addFavorite_alreadyInFavorites_shouldReturn500() throws Exception {
        when(addFavoriteUseCase.execute(any()))
                .thenThrow(new RuntimeException("Already in favorites"));

        mockMvc.perform(post("/api/favorites/media001"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void removeFavorite_shouldReturn204() throws Exception {
        doNothing().when(removeFavoriteUseCase).execute("user-72", "media001");

        mockMvc.perform(delete("/api/favorites/media001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFavorite_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/favorites/media001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void removeFavorite_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Favorite not found"))
                .when(removeFavoriteUseCase).execute("user-72", "media999");

        mockMvc.perform(delete("/api/favorites/media999"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getExternalFavorites_shouldReturn200OnlyExternals() throws Exception {
        when(getFavoritesUseCase.execute("user-72"))
                .thenReturn(List.of(fakeInternalFavorite, fakeExternalFavorite));

        mockMvc.perform(get("/api/favorites/external"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mediaId").value("tmdb-movie-550"));
    }

    @Test
    void getExternalFavorites_noExternals_shouldReturnEmptyList() throws Exception {
        when(getFavoritesUseCase.execute("user-72"))
                .thenReturn(List.of(fakeInternalFavorite)); // "media001" sans tiret → filtré
        mockMvc.perform(get("/api/favorites/external"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getExternalFavorites_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/favorites/external"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void addExternalFavorite_shouldReturn200() throws Exception {
        when(addFavoriteUseCase.execute(any())).thenReturn(fakeExternalFavorite);

        mockMvc.perform(post("/api/favorites/external/tmdb-movie-550"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("fav-002"))
                .andExpect(jsonPath("$.userId").value("user-72"))
                .andExpect(jsonPath("$.mediaId").value("tmdb-movie-550"));
    }

    @Test
    void addExternalFavorite_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/favorites/external/tmdb-movie-550"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addExternalFavorite_alreadyInFavorites_shouldReturn500() throws Exception {
        when(addFavoriteUseCase.execute(any()))
                .thenThrow(new RuntimeException("Already in favorites"));

        mockMvc.perform(post("/api/favorites/external/tmdb-movie-550"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void removeExternalFavorite_shouldReturn204() throws Exception {
        doNothing().when(removeFavoriteUseCase).execute("user-72", "tmdb-movie-550");

        mockMvc.perform(delete("/api/favorites/external/tmdb-movie-550"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeExternalFavorite_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/favorites/external/tmdb-movie-550"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void removeExternalFavorite_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Favorite not found"))
                .when(removeFavoriteUseCase).execute("user-72", "tmdb-movie-999");

        mockMvc.perform(delete("/api/favorites/external/tmdb-movie-999"))
                .andExpect(status().isInternalServerError());
    }
}
