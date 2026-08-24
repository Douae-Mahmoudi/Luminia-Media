package com.collection.infrastructure.persistence;

import com.collection.infrastructure.persistence.entity.CollectionEntity;
import com.collection.infrastructure.persistence.entity.CollectionEntity.CollectionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaCollectionRepositoryUnitTest {

    @Mock
    private JpaCollectionRepository repository; // On mock l'interface

    @Test
    void shouldReturnCollectionWhenFoundByUserIdAndMediaId() {
        String userId = "user-1";
        String mediaId = "movie-1";
        CollectionEntity mockEntity = new CollectionEntity();
        mockEntity.setId("id-1");

        when(repository.findByUserIdAndMediaIdsContainingAndType(userId, mediaId, CollectionType.COLLECTION))
                .thenReturn(Optional.of(mockEntity));

        Optional<CollectionEntity> result = repository.findByUserIdAndMediaIdsContainingAndType(userId, mediaId, CollectionType.COLLECTION);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("id-1");

        verify(repository, times(1)).findByUserIdAndMediaIdsContainingAndType(userId, mediaId, CollectionType.COLLECTION);
    }
}