package com.collection.Integration;

import com.collection.domain.Collection;
import com.collection.domain.Watchlist;
import com.collection.infrastructure.persistence.JpaCollectionRepository;
import com.collection.infrastructure.persistence.adapter.CollectionRepositoryAdapter;
import com.collection.infrastructure.persistence.entity.CollectionEntity;
import com.collection.infrastructure.persistence.entity.CollectionEntity.CollectionType;
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
@Import(CollectionRepositoryAdapter.class)
class CollectionRepositoryAdapterIntegrationTest {

    @Autowired private CollectionRepositoryAdapter adapter;
    @Autowired private JpaCollectionRepository jpa;

    private Collection fakeCollection;

    @BeforeEach
    void setUp() {
        jpa.deleteAll();
        fakeCollection = new Collection(
                UUID.randomUUID().toString(),
                "user-01",
                "Ma Collection",
                "Description",
                true
        );
    }


    @Test
    void save_shouldPersistCollection() {
        Collection saved = adapter.save(fakeCollection);

        assertNotNull(saved);
        assertEquals(fakeCollection.getId(), saved.getId());
        assertEquals("user-01", saved.getUserId());
        assertEquals("Ma Collection", saved.getName());
    }

    @Test
    void save_shouldSetTypeToCollection() {
        adapter.save(fakeCollection);

        CollectionEntity entity = jpa.findById(fakeCollection.getId()).orElseThrow();
        assertEquals(CollectionType.COLLECTION, entity.getType());
    }

    @Test
    void save_shouldPersistMediaIds() {
        fakeCollection.addMedia("media-001");
        fakeCollection.addMedia("media-002");

        Collection saved = adapter.save(fakeCollection);

        assertTrue(saved.getMediaIds().contains("media-001"));
        assertTrue(saved.getMediaIds().contains("media-002"));
    }



    @Test
    void findById_shouldReturnCollection() {
        adapter.save(fakeCollection);

        Optional<Collection> result = adapter.findById(fakeCollection.getId());

        assertTrue(result.isPresent());
        assertEquals(fakeCollection.getId(), result.get().getId());
    }

    @Test
    void findById_notFound_shouldReturnEmpty() {
        Optional<Collection> result = adapter.findById("not-exist");

        assertTrue(result.isEmpty());
    }


    @Test
    void findByUserId_shouldReturnOnlyUserCollections() {
        adapter.save(fakeCollection);
        Collection other = new Collection(UUID.randomUUID().toString(), "user-02", "Autre", null, false);
        adapter.save(other);

        List<Collection> result = adapter.findByUserId("user-01");

        assertEquals(1, result.size());
        assertEquals("user-01", result.get(0).getUserId());
    }

    @Test
    void findByUserId_shouldNotReturnWatchlists() {
        // Sauvegarder une vraie collection
        adapter.save(fakeCollection);

        Watchlist watchlist = new Watchlist(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveWatchlist(watchlist);

        List<Collection> result = adapter.findByUserId("user-01");

        assertEquals(1, result.size());
    }

    @Test
    void findByUserId_noCollections_shouldReturnEmpty() {
        List<Collection> result = adapter.findByUserId("user-inconnu");

        assertTrue(result.isEmpty());
    }


    @Test
    void findPublicByUserId_shouldReturnOnlyPublicCollections() {
        adapter.save(fakeCollection); // isPublic = true

        Collection privateOne = new Collection(UUID.randomUUID().toString(), "user-01", "Privée", null, false);
        adapter.save(privateOne);

        List<Collection> result = adapter.findPublicByUserId("user-01");

        assertEquals(1, result.size());
        assertTrue(result.get(0).isPublic());
    }

    @Test
    void findPublicByUserId_noPublic_shouldReturnEmpty() {
        Collection privateOne = new Collection(UUID.randomUUID().toString(), "user-01", "Privée", null, false);
        adapter.save(privateOne);

        List<Collection> result = adapter.findPublicByUserId("user-01");

        assertTrue(result.isEmpty());
    }


    @Test
    void existsById_shouldReturnTrueWhenExists() {
        adapter.save(fakeCollection);

        assertTrue(adapter.existsById(fakeCollection.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenNotExists() {
        assertFalse(adapter.existsById("not-exist"));
    }


    @Test
    void deleteById_shouldRemoveCollection() {
        adapter.save(fakeCollection);

        adapter.deleteById(fakeCollection.getId());

        assertTrue(adapter.findById(fakeCollection.getId()).isEmpty());
    }


    @Test
    void saveWatchlist_shouldPersistWatchlist() {
        Watchlist watchlist = new Watchlist(UUID.randomUUID().toString(), "user-01", "media-001");

        Watchlist saved = adapter.saveWatchlist(watchlist);

        assertNotNull(saved);
        assertEquals("user-01", saved.getUserId());
        assertEquals("media-001", saved.getMediaId());
    }

    @Test
    void existsWatchlistByUserIdAndMediaId_shouldReturnTrue() {
        Watchlist watchlist = new Watchlist(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveWatchlist(watchlist);

        assertTrue(adapter.existsWatchlistByUserIdAndMediaId("user-01", "media-001"));
    }

    @Test
    void existsWatchlistByUserIdAndMediaId_shouldReturnFalse() {
        assertFalse(adapter.existsWatchlistByUserIdAndMediaId("user-01", "media-999"));
    }

    @Test
    void deleteWatchlistByUserIdAndMediaId_shouldRemoveWatchlist() {
        Watchlist watchlist = new Watchlist(UUID.randomUUID().toString(), "user-01", "media-001");
        adapter.saveWatchlist(watchlist);

        adapter.deleteWatchlistByUserIdAndMediaId("user-01", "media-001");

        assertFalse(adapter.existsWatchlistByUserIdAndMediaId("user-01", "media-001"));
    }
}
