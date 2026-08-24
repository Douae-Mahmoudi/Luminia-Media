package com.collection.controller;

import com.collection.domain.Watchlist;
import com.collection.usecase.watchlist.AddToWatchlistUseCase;
import com.collection.usecase.watchlist.RemoveFromWatchlistUseCase;
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
class WatchlistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AddToWatchlistUseCase addToWatchlistUseCase;

    @Mock
    private RemoveFromWatchlistUseCase removeFromWatchlistUseCase;

    @InjectMocks
    private WatchlistController watchlistController;

    private final String USER_ID = "user-123";
    private final String MEDIA_ID = "media-789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(watchlistController).build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void addToWatchlist_ShouldReturnWatchlist() throws Exception {
        Watchlist watchlist = new Watchlist("w-uuid", USER_ID, MEDIA_ID);
        when(addToWatchlistUseCase.execute(any())).thenReturn(watchlist);

        mockMvc.perform(post("/api/watchlist/{mediaId}", MEDIA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.mediaId").value(MEDIA_ID));

        verify(addToWatchlistUseCase).execute(any(AddToWatchlistUseCase.Input.class));
    }

    @Test
    void removeFromWatchlist_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/watchlist/{mediaId}", MEDIA_ID))
                .andExpect(status().isNoContent());

        verify(removeFromWatchlistUseCase).execute(USER_ID, MEDIA_ID);
    }
}