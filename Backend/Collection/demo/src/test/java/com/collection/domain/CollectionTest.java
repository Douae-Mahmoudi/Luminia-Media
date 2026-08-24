package com.collection.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollectionTest {

    private Collection createCollection() {
        return new Collection("id1", "user1", "Ma Collection", "Description", true);
    }

    @Test
    void shouldCreateCollectionWithEmptyMediaList() {
        Collection collection = createCollection();
        assertTrue(collection.getMediaIds().isEmpty());
    }

    @Test
    void shouldAddMediaSuccessfully() {
        Collection collection = createCollection();
        collection.addMedia("media1");
        assertTrue(collection.getMediaIds().contains("media1"));
    }

    @Test
    void shouldNotAddDuplicateMedia() {
        Collection collection = createCollection();
        collection.addMedia("media1");
        collection.addMedia("media1");
        assertEquals(1, collection.getMediaIds().size());
    }

    @Test
    void shouldRemoveMediaSuccessfully() {
        Collection collection = createCollection();
        collection.addMedia("media1");
        collection.removeMedia("media1");
        assertFalse(collection.getMediaIds().contains("media1"));
    }

    @Test
    void shouldRenameSuccessfully() {
        Collection collection = createCollection();
        collection.rename("Nouveau Nom");
        assertEquals("Nouveau Nom", collection.getName());
    }

    @Test
    void shouldThrowWhenRenameWithBlankName() {
        Collection collection = createCollection();
        assertThrows(IllegalArgumentException.class, () -> collection.rename(""));
        assertThrows(IllegalArgumentException.class, () -> collection.rename("   "));
        assertThrows(IllegalArgumentException.class, () -> collection.rename(null));
    }

    @Test
    void shouldToggleVisibility() {
        Collection collection = createCollection(); // isPublic = true
        collection.toggleVisibility();
        assertFalse(collection.isPublic());
        collection.toggleVisibility();
        assertTrue(collection.isPublic());
    }

    @Test
    void shouldReturnUnmodifiableMediaList() {
        Collection collection = createCollection();
        collection.addMedia("media1");
        assertThrows(UnsupportedOperationException.class,
                () -> collection.getMediaIds().add("media2"));
    }

    @Test
    void shouldUpdateUpdatedAtOnAddMedia() throws InterruptedException {
        Collection collection = createCollection();
        var before = collection.getUpdatedAt();
        Thread.sleep(10);
        collection.addMedia("media1");
        assertTrue(collection.getUpdatedAt().isAfter(before));
    }

    @Test
    void shouldUpdateUpdatedAtOnRename() throws InterruptedException {
        Collection collection = createCollection();
        var before = collection.getUpdatedAt();
        Thread.sleep(10);
        collection.rename("New Name");
        assertTrue(collection.getUpdatedAt().isAfter(before));
    }
}