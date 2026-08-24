package com.collection.infrastructure.persistence;

import com.collection.infrastructure.persistence.entity.LikeEntity;
import com.collection.infrastructure.persistence.entity.LikeEntity.LikeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaLikeRepositoryTest {

    @Mock
    private JpaLikeRepository likeRepository;

    private static final String USER_ID = "user-123";
    private static final String MEDIA_ID = "media-456";
    private static final LikeType TYPE = LikeType.LIKE;

    private LikeEntity likeEntity;

    @BeforeEach
    void setUp() {
        likeEntity = new LikeEntity();
        likeEntity.setId("like-001");
        likeEntity.setUserId(USER_ID);
        likeEntity.setMediaId(MEDIA_ID);
        likeEntity.setType(TYPE);
    }

    @Test
    void findByUserIdAndMediaIdAndType_ShouldReturnOptional() {
        when(likeRepository.findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE))
                .thenReturn(Optional.of(likeEntity));

        Optional<LikeEntity> result = likeRepository.findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE);

        assertThat(result).isPresent().contains(likeEntity);
        verify(likeRepository).findByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE);
    }

    @Test
    void findByUserIdAndType_ShouldReturnList() {
        when(likeRepository.findByUserIdAndType(USER_ID, TYPE))
                .thenReturn(List.of(likeEntity));

        List<LikeEntity> results = likeRepository.findByUserIdAndType(USER_ID, TYPE);

        assertThat(results).hasSize(1).contains(likeEntity);
        verify(likeRepository).findByUserIdAndType(USER_ID, TYPE);
    }

    @Test
    void countByMediaIdAndType_ShouldReturnCount() {
        long expectedCount = 150L;
        when(likeRepository.countByMediaIdAndType(MEDIA_ID, TYPE)).thenReturn(expectedCount);

        long actualCount = likeRepository.countByMediaIdAndType(MEDIA_ID, TYPE);

        assertThat(actualCount).isEqualTo(expectedCount);
        verify(likeRepository).countByMediaIdAndType(MEDIA_ID, TYPE);
    }

    @Test
    void existsByUserIdAndMediaIdAndType_ShouldReturnBoolean() {
        when(likeRepository.existsByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE)).thenReturn(true);

        boolean exists = likeRepository.existsByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE);

        assertThat(exists).isTrue();
        verify(likeRepository).existsByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE);
    }

    @Test
    void deleteByUserIdAndMediaIdAndType_ShouldCallMethod() {
        likeRepository.deleteByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE);

        verify(likeRepository, times(1)).deleteByUserIdAndMediaIdAndType(USER_ID, MEDIA_ID, TYPE);
    }
}