package com.collection.usecase.collection;

import com.collection.domain.Collection;
import com.collection.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCollectionUseCaseTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private GetCollectionUseCase useCase;

    private Collection createCollection(String id, String userId, boolean isPublic) {
        return new Collection(id, userId, "Ma Collection", "Description", isPublic);
    }


    @Test
    void shouldReturnCollectionWhenFound() {
        Collection collection = createCollection("col1", "user1", true);
        when(collectionRepository.findById("col1")).thenReturn(Optional.of(collection));

        Collection result = useCase.getById("col1");

        assertNotNull(result);
        assertEquals("col1", result.getId());
        assertEquals("user1", result.getUserId());
    }

    @Test
    void shouldThrowWhenCollectionNotFound() {
        when(collectionRepository.findById("col1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.getById("col1"));

        assertTrue(ex.getMessage().contains("Collection not found"));
        assertTrue(ex.getMessage().contains("col1"));
    }


    @Test
    void shouldReturnAllCollectionsForUser() {
        List<Collection> collections = List.of(
                createCollection("col1", "user1", true),
                createCollection("col2", "user1", false)
        );
        when(collectionRepository.findByUserId("user1")).thenReturn(collections);

        List<Collection> result = useCase.getByUserId("user1");

        assertEquals(2, result.size());
        verify(collectionRepository).findByUserId("user1");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCollections() {
        when(collectionRepository.findByUserId("user1")).thenReturn(List.of());

        List<Collection> result = useCase.getByUserId("user1");

        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnOnlyPublicCollections() {
        List<Collection> publicCollections = List.of(
                createCollection("col1", "user1", true)
        );
        when(collectionRepository.findPublicByUserId("user1")).thenReturn(publicCollections);

        List<Collection> result = useCase.getPublicByUserId("user1");

        assertEquals(1, result.size());
        assertTrue(result.get(0).isPublic());
        verify(collectionRepository).findPublicByUserId("user1");
    }

    @Test
    void shouldReturnEmptyListWhenNoPublicCollections() {
        when(collectionRepository.findPublicByUserId("user1")).thenReturn(List.of());

        List<Collection> result = useCase.getPublicByUserId("user1");

        assertTrue(result.isEmpty());
    }
}