package com.collection.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        CollectionResponse response = new CollectionResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setId("col1");
        response.setUserId("user1");
        response.setName("Ma Collection");
        response.setDescription("Description");
        response.setPublic(true);
        response.setMediaIds(List.of("media1", "media2"));
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        assertEquals("col1", response.getId());
        assertEquals("user1", response.getUserId());
        assertEquals("Ma Collection", response.getName());
        assertEquals("Description", response.getDescription());
        assertTrue(response.isPublic());
        assertEquals(List.of("media1", "media2"), response.getMediaIds());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void shouldCalculateMediaCountWhenSettingMediaIds() {
        CollectionResponse response = new CollectionResponse();
        response.setMediaIds(List.of("media1", "media2", "media3"));

        assertEquals(3, response.getMediaCount());
    }

    @Test
    void shouldSetMediaCountToZeroWhenMediaIdsIsEmpty() {
        CollectionResponse response = new CollectionResponse();
        response.setMediaIds(List.of());

        assertEquals(0, response.getMediaCount());
    }

    @Test
    void shouldSetMediaCountToZeroWhenMediaIdsIsNull() {
        CollectionResponse response = new CollectionResponse();
        response.setMediaIds(null);

        assertEquals(0, response.getMediaCount());
    }

    @Test
    void shouldDefaultIsPublicToFalse() {
        CollectionResponse response = new CollectionResponse();
        assertFalse(response.isPublic());
    }

    @Test
    void shouldDefaultMediaCountToZero() {
        CollectionResponse response = new CollectionResponse();
        assertEquals(0, response.getMediaCount());
    }

    @Test
    void shouldUpdateMediaCountWhenMediaIdsChanges() {
        CollectionResponse response = new CollectionResponse();
        response.setMediaIds(List.of("media1"));
        assertEquals(1, response.getMediaCount());

        response.setMediaIds(List.of("media1", "media2", "media3"));
        assertEquals(3, response.getMediaCount());
    }
}
