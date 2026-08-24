package com.collection.infrastructure.persistence.adapter;

import com.collection.domain.Collection;
import com.collection.domain.Watchlist;
import com.collection.infrastructure.persistence.JpaCollectionRepository;
import com.collection.infrastructure.persistence.entity.CollectionEntity;
import com.collection.infrastructure.persistence.entity.CollectionEntity.CollectionType;
import com.collection.infrastructure.persistence.entity.CollectionEntity.WatchlistStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionRepositoryAdapterTest {

    @Mock
    private JpaCollectionRepository jpa;

    @InjectMocks
    private CollectionRepositoryAdapter adapter;

    private static final String USER_ID = "user-123";
    private static final String MEDIA_ID = "movie-456";


    @Test
    void save_shouldSetTypeToCollectionAndCallJpa() {
        Collection collection = new Collection("id-1", USER_ID, "Ma Liste", "Desc", true);
        CollectionEntity entity = new CollectionEntity();
        entity.setId("id-1");

        when(jpa.save(any(CollectionEntity.class))).thenReturn(entity);

        adapter.save(collection);

        ArgumentCaptor<CollectionEntity> captor = ArgumentCaptor.forClass(CollectionEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(CollectionType.COLLECTION);
    }

    @Test
    void findByUserId_shouldFilterByCollectionType() {
        CollectionEntity entity = new CollectionEntity();
        entity.setId("id-1");
        entity.setUserId(USER_ID);
        when(jpa.findByUserIdAndType(USER_ID, CollectionType.COLLECTION)).thenReturn(List.of(entity));

        List<Collection> result = adapter.findByUserId(USER_ID);

        assertThat(result).hasSize(1);
        verify(jpa).findByUserIdAndType(USER_ID, CollectionType.COLLECTION);
    }


    @Test
    void saveWatchlist_shouldMapFieldsAndGenerateIdIfMissing() {

        Watchlist watchlist = new Watchlist(null, USER_ID, MEDIA_ID);
        watchlist.updateStatus(Watchlist.WatchlistStatus.TO_WATCH);

        CollectionEntity savedEntity = new CollectionEntity();
        savedEntity.setId("new-uuid");
        savedEntity.setMediaIds(List.of(MEDIA_ID));
        savedEntity.setWatchlistStatus(WatchlistStatus.TO_WATCH);

        when(jpa.save(any(CollectionEntity.class))).thenReturn(savedEntity);

        Watchlist result = adapter.saveWatchlist(watchlist);

        ArgumentCaptor<CollectionEntity> captor = ArgumentCaptor.forClass(CollectionEntity.class);
        verify(jpa).save(captor.capture());

        CollectionEntity captured = captor.getValue();
        assertThat(captured.getId()).isNotNull();
        assertThat(captured.getType()).isEqualTo(CollectionType.WATCHLIST);
        assertThat(result.getId()).isEqualTo("new-uuid");
    }

    @Test
    void findWatchlistByUserIdAndStatus_shouldConvertEnumCorrectly() {
        when(jpa.findByUserIdAndTypeAndWatchlistStatus(any(), any(), any()))
                .thenReturn(List.of());

        adapter.findWatchlistByUserIdAndStatus(USER_ID, Watchlist.WatchlistStatus.WATCHING);


        verify(jpa).findByUserIdAndTypeAndWatchlistStatus(USER_ID, CollectionType.WATCHLIST, WatchlistStatus.WATCHING);
    }

    @Test
    void deleteWatchlistByUserIdAndMediaId_shouldCallCorrectJpaMethod() {
        adapter.deleteWatchlistByUserIdAndMediaId(USER_ID, MEDIA_ID);

        verify(jpa).deleteByUserIdAndMediaIdsContainingAndType(USER_ID, MEDIA_ID, CollectionType.WATCHLIST);
    }

    @Test
    void existsWatchlistByUserIdAndMediaId_shouldReturnJpaResponse() {
        when(jpa.existsByUserIdAndMediaIdsContainingAndType(USER_ID, MEDIA_ID, CollectionType.WATCHLIST))
                .thenReturn(true);

        boolean exists = adapter.existsWatchlistByUserIdAndMediaId(USER_ID, MEDIA_ID);

        assertThat(exists).isTrue();
    }
}