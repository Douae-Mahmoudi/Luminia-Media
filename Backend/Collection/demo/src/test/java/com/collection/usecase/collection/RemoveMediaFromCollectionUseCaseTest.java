package com.collection.usecase.collection;

import com.collection.domain.Collection;
import com.collection.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveMediaFromCollectionUseCaseTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private RemoveMediaFromCollectionUseCase useCase;

    private Collection createCollectionWithMedia(String userId, String mediaId) {
        Collection collection = new Collection("col1", userId, "Ma Collection", "Description", true);
        collection.addMedia(mediaId);
        return collection;
    }

    @Test
    void shouldRemoveMediaSuccessfully() {
        Collection collection = createCollectionWithMedia("user1", "media1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        Collection result = useCase.execute(
                new RemoveMediaFromCollectionUseCase.Input("user1", "col1", "media1")
        );

        assertFalse(result.getMediaIds().contains("media1"));
        verify(collectionRepository).save(collection);
    }

    @Test
    void shouldThrowWhenCollectionNotFound() {
        when(collectionRepository.findById("col1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.execute(new RemoveMediaFromCollectionUseCase.Input("user1", "col1", "media1"))
        );

        assertEquals("Collection not found", ex.getMessage());
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserIsNotOwner() {
        Collection collection = createCollectionWithMedia("user1", "media1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.execute(new RemoveMediaFromCollectionUseCase.Input("user2", "col1", "media1"))
        );

        assertEquals("Access denied", ex.getMessage());
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void shouldCallSaveAfterRemovingMedia() {
        Collection collection = createCollectionWithMedia("user1", "media1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        useCase.execute(new RemoveMediaFromCollectionUseCase.Input("user1", "col1", "media1"));

        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void shouldNotThrowWhenRemovingNonExistentMedia() {
        Collection collection = createCollectionWithMedia("user1", "media1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        assertDoesNotThrow(() ->
                useCase.execute(new RemoveMediaFromCollectionUseCase.Input("user1", "col1", "mediaX"))
        );
    }
}
