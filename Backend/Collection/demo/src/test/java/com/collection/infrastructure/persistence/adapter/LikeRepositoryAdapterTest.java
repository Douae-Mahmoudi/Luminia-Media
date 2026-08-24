package com.collection.infrastructure.persistence.adapter;

import com.collection.domain.Favorite;
import com.collection.domain.Like;
import com.collection.infrastructure.persistence.JpaLikeRepository;
import com.collection.infrastructure.persistence.entity.LikeEntity;
import com.collection.infrastructure.persistence.entity.LikeEntity.LikeType;
import org.junit.jupiter.api.BeforeEach;
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
class LikeRepositoryAdapterTest {

    @Mock
    private JpaLikeRepository jpa;

    @InjectMocks
    private LikeRepositoryAdapter adapter;

    private static final String USER_ID = "user-123";
    private static final String MEDIA_ID = "movie-456";


    @Test
    void saveLike_ShouldSetTypeToLike() {
        // Given
        Like likeDomain = new Like("l1", USER_ID, MEDIA_ID);

        LikeEntity savedEntity = new LikeEntity();
        savedEntity.setId("l1");
        savedEntity.setUserId(USER_ID);
        savedEntity.setMediaId(MEDIA_ID);
        savedEntity.setType(LikeType.LIKE);

        when(jpa.save(any(LikeEntity.class))).thenReturn(savedEntity);

        Like result = adapter.save(likeDomain);

        ArgumentCaptor<LikeEntity> captor = ArgumentCaptor.forClass(LikeEntity.class);
        verify(jpa).save(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(LikeType.LIKE);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }
    @Test
    void findByUserIdAndMediaId_ShouldUseLikeType() {
        when(jpa.findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, LikeType.LIKE))
                .thenReturn(Optional.empty());

        adapter.findByUserIdAndMediaId(USER_ID, MEDIA_ID);

        verify(jpa).findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, LikeType.LIKE);
    }

    @Test
    void countByMediaId_ShouldReturnCountForLikesOnly() {
        when(jpa.countByMediaIdAndType(MEDIA_ID, LikeType.LIKE)).thenReturn(10L);

        long count = adapter.countByMediaId(MEDIA_ID);

        assertThat(count).isEqualTo(10L);
    }


    @Test
    void saveFavorite_ShouldSetTypeToFavoriteAndGenerateId() {
        Favorite favoriteDomain = new Favorite(null, USER_ID, MEDIA_ID);
        LikeEntity savedEntity = new LikeEntity();
        savedEntity.setId("fav-generated-uuid");
        savedEntity.setUserId(USER_ID);
        savedEntity.setMediaId(MEDIA_ID);
        savedEntity.setType(LikeType.FAVORITE);

        when(jpa.save(any(LikeEntity.class))).thenReturn(savedEntity);

        Favorite result = adapter.saveFavorite(favoriteDomain);

        ArgumentCaptor<LikeEntity> captor = ArgumentCaptor.forClass(LikeEntity.class);
        verify(jpa).save(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(LikeType.FAVORITE);
        assertThat(captor.getValue().getId()).isNotNull();
        assertThat(result.getId()).isEqualTo("fav-generated-uuid");
    }

    @Test
    void findFavoriteByUserIdAndMediaId_ShouldUseFavoriteType() {
        LikeEntity entity = new LikeEntity();
        entity.setId("f1");
        entity.setUserId(USER_ID);
        entity.setMediaId(MEDIA_ID);

        when(jpa.findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, LikeType.FAVORITE))
                .thenReturn(Optional.of(entity));

        Optional<Favorite> result = adapter.findFavoriteByUserIdAndMediaId(USER_ID, MEDIA_ID);

        assertThat(result).isPresent();
        verify(jpa).findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, LikeType.FAVORITE);
    }

    @Test
    void deleteFavoriteByUserIdAndMediaId_ShouldOnlyDeleteFavoriteType() {
        adapter.deleteFavoriteByUserIdAndMediaId(USER_ID, MEDIA_ID);

        verify(jpa).deleteByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, LikeType.FAVORITE);
    }

    @Test
    void existsFavoriteByUserIdAndMediaId_ShouldCheckFavoriteType() {
        when(jpa.existsByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, LikeType.FAVORITE)).thenReturn(true);

        boolean exists = adapter.existsFavoriteByUserIdAndMediaId(USER_ID, MEDIA_ID);

        assertThat(exists).isTrue();
    }
}