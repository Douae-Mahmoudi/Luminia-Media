package com.collection.usecase.collection;

import com.collection.domain.Collection;
import com.collection.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCollectionUseCaseTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private CreateCollectionUseCase useCase;

    @Test
    void shouldCreateCollectionSuccessfully() {
        CreateCollectionUseCase.Input input = new CreateCollectionUseCase.Input(
                "user1", "Ma Collection", "Description", true
        );

        Collection saved = new Collection("uuid", "user1", "Ma Collection", "Description", true);
        when(collectionRepository.save(any())).thenReturn(saved);

        Collection result = useCase.execute(input);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("Ma Collection", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.isPublic());
    }

    @Test
    void shouldCreatePrivateCollection() {
        CreateCollectionUseCase.Input input = new CreateCollectionUseCase.Input(
                "user1", "Privée", "desc", false
        );

        Collection saved = new Collection("uuid", "user1", "Privée", "desc", false);
        when(collectionRepository.save(any())).thenReturn(saved);

        Collection result = useCase.execute(input);

        assertFalse(result.isPublic());
    }

    @Test
    void shouldCallSaveOnce() {
        CreateCollectionUseCase.Input input = new CreateCollectionUseCase.Input(
                "user1", "Ma Collection", "Description", true
        );
        when(collectionRepository.save(any())).thenReturn(
                new Collection("uuid", "user1", "Ma Collection", "Description", true)
        );

        useCase.execute(input);

        verify(collectionRepository, times(1)).save(any());
    }

    @Test
    void shouldGenerateUniqueIdForEachCollection() {
        CreateCollectionUseCase.Input input = new CreateCollectionUseCase.Input(
                "user1", "Ma Collection", "Description", true
        );
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Collection result1 = useCase.execute(input);
        Collection result2 = useCase.execute(input);

        assertNotEquals(result1.getId(), result2.getId());
    }

    @Test
    void shouldPassCorrectDataToRepository() {
        CreateCollectionUseCase.Input input = new CreateCollectionUseCase.Input(
                "user1", "Ma Collection", "Description", true
        );
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Collection result = useCase.execute(input);

        assertEquals("user1", result.getUserId());
        assertEquals("Ma Collection", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.isPublic());
        assertTrue(result.getMediaIds().isEmpty());
    }
}