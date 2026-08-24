package com.collection.usecase.favorite;

import com.collection.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveFavoriteUseCaseTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private RemoveFavoriteUseCase useCase;

    @Test
    void shouldRemoveFavoriteSuccessfully() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        assertDoesNotThrow(() -> useCase.execute("user1", "media1"));

        verify(favoriteRepository).deleteFavoriteByUserIdAndMediaId("user1", "media1");
    }

    @Test
    void shouldThrowWhenFavoriteNotFound() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.execute("user1", "media1"));

        assertEquals("Favorite not found", ex.getMessage());
        verify(favoriteRepository, never()).deleteFavoriteByUserIdAndMediaId(any(), any());
    }

    @Test
    void shouldCallDeleteOnce() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        useCase.execute("user1", "media1");

        verify(favoriteRepository, times(1)).deleteFavoriteByUserIdAndMediaId("user1", "media1");
    }

    @Test
    void shouldNotCallDeleteWhenFavoriteNotFound() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> useCase.execute("user1", "media1"));

        verify(favoriteRepository, never()).deleteFavoriteByUserIdAndMediaId(any(), any());
    }
}