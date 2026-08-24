package com.collection.usecase.watchlist;

import com.collection.domain.Watchlist;
import com.collection.repository.WatchlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddToWatchlistUseCaseTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @InjectMocks
    private AddToWatchlistUseCase useCase;

    @Test
    void shouldAddToWatchlistSuccessfully() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(false);
        when(watchlistRepository.saveWatchlist(any())).thenAnswer(inv -> inv.getArgument(0));

        Watchlist result = useCase.execute(new AddToWatchlistUseCase.Input("user1", "media1"));

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("media1", result.getMediaId());
    }

    @Test
    void shouldThrowWhenAlreadyInWatchlist() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.execute(new AddToWatchlistUseCase.Input("user1", "media1"))
        );

        assertEquals("Media already in watchlist", ex.getMessage());
        verify(watchlistRepository, never()).saveWatchlist(any());
    }

    @Test
    void shouldCallSaveOnce() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(false);
        when(watchlistRepository.saveWatchlist(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new AddToWatchlistUseCase.Input("user1", "media1"));

        verify(watchlistRepository, times(1)).saveWatchlist(any());
    }

    @Test
    void shouldSetDefaultStatusToWatch() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(false);
        when(watchlistRepository.saveWatchlist(any())).thenAnswer(inv -> inv.getArgument(0));

        Watchlist result = useCase.execute(new AddToWatchlistUseCase.Input("user1", "media1"));

        assertEquals(Watchlist.WatchlistStatus.TO_WATCH, result.getStatus());
    }

    @Test
    void shouldGenerateUniqueIdForEachWatchlist() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId(any(), any())).thenReturn(false);
        when(watchlistRepository.saveWatchlist(any())).thenAnswer(inv -> inv.getArgument(0));

        Watchlist result1 = useCase.execute(new AddToWatchlistUseCase.Input("user1", "media1"));
        Watchlist result2 = useCase.execute(new AddToWatchlistUseCase.Input("user1", "media2"));

        assertNotEquals(result1.getId(), result2.getId());
    }
}
