package com.collection.usecase.watchlist;

import com.collection.repository.WatchlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveFromWatchlistUseCaseTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @InjectMocks
    private RemoveFromWatchlistUseCase useCase;

    @Test
    void shouldRemoveFromWatchlistSuccessfully() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        assertDoesNotThrow(() -> useCase.execute("user1", "media1"));

        verify(watchlistRepository).deleteWatchlistByUserIdAndMediaId("user1", "media1");
    }

    @Test
    void shouldThrowWhenMediaNotInWatchlist() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.execute("user1", "media1"));

        assertEquals("Media not found in watchlist", ex.getMessage());
        verify(watchlistRepository, never()).deleteWatchlistByUserIdAndMediaId(any(), any());
    }

    @Test
    void shouldCallDeleteOnce() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        useCase.execute("user1", "media1");

        verify(watchlistRepository, times(1)).deleteWatchlistByUserIdAndMediaId("user1", "media1");
    }

    @Test
    void shouldNotCallDeleteWhenMediaNotFound() {
        when(watchlistRepository.existsWatchlistByUserIdAndMediaId("user1", "media1")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> useCase.execute("user1", "media1"));

        verify(watchlistRepository, never()).deleteWatchlistByUserIdAndMediaId(any(), any());
    }
}