package com.collection.Integration;

import com.collection.domain.Favorite;
import com.collection.domain.Like;
import com.collection.infrastructure.persistence.JpaLikeRepository;
import com.collection.infrastructure.persistence.adapter.LikeRepositoryAdapter;
import com.collection.infrastructure.persistence.entity.LikeEntity.LikeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(LikeRepositoryAdapter.class)
class LikeRepositoryAdapterIntegrationTest {

    @Autowired private LikeRepositoryAdapter adapter;
    @Autowired private JpaLikeRepository jpa;

    private Like fakeLike;

    @BeforeEach
    void setUp() {
        jpa.deleteAll();
        fakeLike = new Like(UUID.randomUUID().toString(), "user-01", "media-001");
    }


    @Test
    void save_shouldPersistLike() {
        Like saved = adapter.save(fakeLike);

        assertNotNull(saved);
        assertEquals(fakeLike.getId(), saved.getId());
        assertEquals("user-01", saved.getUserId());
        assertEquals("media-001", saved.getMediaId());
    }

    @Test
    void save_shouldSetTypeToLike() {
        adapter.save(fakeLike);

        assertEquals(LikeType.LIKE, jpa.findById(fakeLike.getId()).orElseThrow().getType());
    }


    @Test
    void existsByUserIdAndMediaId_shouldReturnTrueAfterSave() {
        adapter.save(fakeLike);

        assertTrue(adapter.existsByUserIdAndMediaId("user-01", "media-001"));
    }

    @Test
    void existsByUserIdAndMediaId_shouldReturnFalseWhenNotExists() {
        assertFalse(adapter.existsByUserIdAndMediaId("user-01", "media-999"));
    }

    @Test
    void existsByUserIdAndMediaId_shouldNotConfuseWithFavorite() {
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveFavorite(fav);

        assertFalse(adapter.existsByUserIdAndMediaId("user-01", "media-001"));
    }


    @Test
    void findByUserIdAndMediaId_shouldReturnLike() {
        adapter.save(fakeLike);

        Optional<Like> result = adapter.findByUserIdAndMediaId("user-01", "media-001");

        assertTrue(result.isPresent());
        assertEquals("user-01", result.get().getUserId());
    }

    @Test
    void findByUserIdAndMediaId_notFound_shouldReturnEmpty() {
        Optional<Like> result = adapter.findByUserIdAndMediaId("user-01", "media-999");

        assertTrue(result.isEmpty());
    }


    @Test
    void findByUserId_shouldReturnAllLikesForUser() {
        adapter.save(fakeLike);
        Like second = new Like(UUID.randomUUID().toString(), "user-01", "media-002");
        adapter.save(second);

        List<Like> result = adapter.findByUserId("user-01");

        assertEquals(2, result.size());
    }

    @Test
    void findByUserId_shouldNotReturnFavorites() {
        adapter.save(fakeLike);
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-002");
        adapter.saveFavorite(fav);

        List<Like> result = adapter.findByUserId("user-01");

        assertEquals(1, result.size());
    }


    @Test
    void countByMediaId_shouldReturnCorrectCount() {
        adapter.save(fakeLike);
        Like second = new Like(UUID.randomUUID().toString(), "user-02", "media-001");
        adapter.save(second);

        assertEquals(2, adapter.countByMediaId("media-001"));
    }

    @Test
    void countByMediaId_shouldReturnZeroWhenNoLikes() {
        assertEquals(0, adapter.countByMediaId("media-999"));
    }


    @Test
    void deleteByUserIdAndMediaId_shouldRemoveLike() {
        adapter.save(fakeLike);

        adapter.deleteByUserIdAndMediaId("user-01", "media-001");

        assertFalse(adapter.existsByUserIdAndMediaId("user-01", "media-001"));
    }

    @Test
    void deleteByUserIdAndMediaId_shouldNotAffectOtherLikes() {
        adapter.save(fakeLike);
        Like other = new Like(UUID.randomUUID().toString(), "user-02", "media-001");
        adapter.save(other);

        adapter.deleteByUserIdAndMediaId("user-01", "media-001");

        assertTrue(adapter.existsByUserIdAndMediaId("user-02", "media-001"));
    }


    @Test
    void saveFavorite_shouldPersistFavorite() {
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-001");

        Favorite saved = adapter.saveFavorite(fav);

        assertNotNull(saved);
        assertEquals("user-01", saved.getUserId());
        assertEquals("media-001", saved.getMediaId());
    }

    @Test
    void saveFavorite_shouldSetTypeToFavorite() {
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveFavorite(fav);

        assertEquals(LikeType.FAVORITE, jpa.findById(fav.getId()).orElseThrow().getType());
    }


    @Test
    void existsFavoriteByUserIdAndMediaId_shouldReturnTrue() {
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveFavorite(fav);

        assertTrue(adapter.existsFavoriteByUserIdAndMediaId("user-01", "media-001"));
    }

    @Test
    void existsFavoriteByUserIdAndMediaId_shouldNotConfuseWithLike() {
        adapter.save(fakeLike);

        assertFalse(adapter.existsFavoriteByUserIdAndMediaId("user-01", "media-001"));
    }


    @Test
    void deleteFavoriteByUserIdAndMediaId_shouldRemoveFavorite() {
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveFavorite(fav);

        adapter.deleteFavoriteByUserIdAndMediaId("user-01", "media-001");

        assertFalse(adapter.existsFavoriteByUserIdAndMediaId("user-01", "media-001"));
    }

    @Test
    void deleteFavoriteByUserIdAndMediaId_shouldNotAffectLikes() {
        adapter.save(fakeLike);
        Favorite fav = new Favorite(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveFavorite(fav);

        adapter.deleteFavoriteByUserIdAndMediaId("user-01", "media-001");

        assertTrue(adapter.existsByUserIdAndMediaId("user-01", "media-001"));
    }
}
