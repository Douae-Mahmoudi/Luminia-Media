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
class AddMediaToCollectionUseCaseTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private AddMediaToCollectionUseCase useCase;

    private Collection createCollection(String userId) {
        return new Collection("col1", userId, "Ma Collection", "Description", true);
    }

    @Test
    void shouldAddMediaSuccessfully() {
        Collection collection = createCollection("user1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        Collection result = useCase.execute(
                new AddMediaToCollectionUseCase.Input("user1", "col1", "media1")
        );

        assertTrue(result.getMediaIds().contains("media1"));
        verify(collectionRepository).save(collection);
    }

    @Test
    void shouldThrowWhenCollectionNotFound() {
        when(collectionRepository.findById("col1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.execute(new AddMediaToCollectionUseCase.Input("user1", "col1", "media1"))
        );

        assertEquals("Collection not found", ex.getMessage());
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserIsNotOwner() {
        Collection collection = createCollection("user1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.execute(new AddMediaToCollectionUseCase.Input("user2", "col1", "media1"))
        );

        assertEquals("Access denied", ex.getMessage());
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void shouldNotAddDuplicateMedia() {
        Collection collection = createCollection("user1");
        collection.addMedia("media1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        Collection result = useCase.execute(
                new AddMediaToCollectionUseCase.Input("user1", "col1", "media1")
        );

        assertEquals(1, result.getMediaIds().size());
    }

    @Test
    void shouldCallSaveAfterAddingMedia() {
        Collection collection = createCollection("user1");
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        useCase.execute(new AddMediaToCollectionUseCase.Input("user1", "col1", "media1"));

        verify(collectionRepository, times(1)).save(collection);
    }
}