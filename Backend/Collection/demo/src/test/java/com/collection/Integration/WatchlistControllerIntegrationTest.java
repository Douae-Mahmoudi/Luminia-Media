package com.collection.Integration;

import com.collection.config.security.JwtUtil;
import com.collection.domain.Watchlist;
import com.collection.usecase.watchlist.AddToWatchlistUseCase;
import com.collection.usecase.watchlist.GetWatchlistUseCase;
import com.collection.usecase.watchlist.RemoveFromWatchlistUseCase;
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
        controllers = com.collection.controller.WatchlistController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.collection.config.SecurityConfig.class
        )
)
class WatchlistControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AddToWatchlistUseCase    addToWatchlistUseCase;
    @MockitoBean private RemoveFromWatchlistUseCase removeFromWatchlistUseCase;
    @MockitoBean private GetWatchlistUseCase      getWatchlistUseCase;
    @MockitoBean private JwtUtil                  jwtUtil;

    private Watchlist fakeWatchlist;

    private void setAuthentication(String userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        fakeWatchlist = new Watchlist("wl-001", "user-72", "media-001");
        setAuthentication("user-72");
    }



    @Test
    void getWatchlist_shouldReturn200WithList() throws Exception {
        Watchlist second = new Watchlist("wl-002", "user-72", "media-002");
        when(getWatchlistUseCase.execute("user-72"))
                .thenReturn(List.of(fakeWatchlist, second));

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("wl-001"))
                .andExpect(jsonPath("$[1].id").value("wl-002"));
    }

    @Test
    void getWatchlist_emptyList_shouldReturn200() throws Exception {
        when(getWatchlistUseCase.execute("user-72")).thenReturn(List.of());

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getWatchlist_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isInternalServerError());
    }



    @Test
    void addToWatchlist_shouldReturn200() throws Exception {
        when(addToWatchlistUseCase.execute(any())).thenReturn(fakeWatchlist);

        mockMvc.perform(post("/api/watchlist/media-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wl-001"))
                .andExpect(jsonPath("$.userId").value("user-72"))
                .andExpect(jsonPath("$.mediaId").value("media-001"));
    }

    @Test
    void addToWatchlist_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/watchlist/media-001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addToWatchlist_alreadyInWatchlist_shouldReturn500() throws Exception {
        when(addToWatchlistUseCase.execute(any()))
                .thenThrow(new RuntimeException("Media already in watchlist"));

        mockMvc.perform(post("/api/watchlist/media-001"))
                .andExpect(status().isInternalServerError());
    }



    @Test
    void removeFromWatchlist_shouldReturn204() throws Exception {
        doNothing().when(removeFromWatchlistUseCase).execute("user-72", "media-001");

        mockMvc.perform(delete("/api/watchlist/media-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFromWatchlist_noAuthentication_shouldReturn500() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/watchlist/media-001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void removeFromWatchlist_notFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Media not found in watchlist"))
                .when(removeFromWatchlistUseCase).execute("user-72", "media-999");

        mockMvc.perform(delete("/api/watchlist/media-999"))
                .andExpect(status().isInternalServerError());
    }
}
