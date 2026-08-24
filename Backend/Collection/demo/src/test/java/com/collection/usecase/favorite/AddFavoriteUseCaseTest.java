package com.collection.usecase.favorite;

import com.collection.domain.Favorite;
import com.collection.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddFavoriteUseCaseTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private AddFavoriteUseCase useCase;

    @Test
    void shouldAddFavoriteSuccessfully() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(false);
        when(favoriteRepository.saveFavorite(any())).thenAnswer(inv -> inv.getArgument(0));

        Favorite result = useCase.execute(new AddFavoriteUseCase.Input("user1", "media1"));

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("media1", result.getMediaId());
    }

    @Test
    void shouldThrowWhenAlreadyInFavorites() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.execute(new AddFavoriteUseCase.Input("user1", "media1"))
        );

        assertEquals("Already in favorites", ex.getMessage());
        verify(favoriteRepository, never()).saveFavorite(any());
    }

    @Test
    void shouldCallSaveOnce() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(false);
        when(favoriteRepository.saveFavorite(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new AddFavoriteUseCase.Input("user1", "media1"));

        verify(favoriteRepository, times(1)).saveFavorite(any());
    }

    @Test
    void shouldGenerateUniqueIdForEachFavorite() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId(any(), any())).thenReturn(false);
        when(favoriteRepository.saveFavorite(any())).thenAnswer(inv -> inv.getArgument(0));

        Favorite result1 = useCase.execute(new AddFavoriteUseCase.Input("user1", "media1"));
        Favorite result2 = useCase.execute(new AddFavoriteUseCase.Input("user1", "media2"));

        assertNotEquals(result1.getId(), result2.getId());
    }

    @Test
    void shouldSetFavoritedAtOnCreation() {
        when(favoriteRepository.existsFavoriteByUserIdAndMediaId("user1", "media1")).thenReturn(false);
        when(favoriteRepository.saveFavorite(any())).thenAnswer(inv -> inv.getArgument(0));

        Favorite result = useCase.execute(new AddFavoriteUseCase.Input("user1", "media1"));

        assertNotNull(result.getFavoritedAt());
    }
}
