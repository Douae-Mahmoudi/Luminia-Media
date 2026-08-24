package com.collection.dto.response;

import com.collection.domain.Watchlist.WatchlistStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        WatchlistResponse response = new WatchlistResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setId("watch1");
        response.setUserId("user1");
        response.setMediaId("media1");
        response.setStatus(WatchlistStatus.TO_WATCH);
        response.setAddedAt(now);
        response.setUpdatedAt(now);

        assertEquals("watch1", response.getId());
        assertEquals("user1", response.getUserId());
        assertEquals("media1", response.getMediaId());
        assertEquals(WatchlistStatus.TO_WATCH, response.getStatus());
        assertEquals(now, response.getAddedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void shouldDefaultAllFieldsToNull() {
        WatchlistResponse response = new WatchlistResponse();

        assertNull(response.getId());
        assertNull(response.getUserId());
        assertNull(response.getMediaId());
        assertNull(response.getStatus());
        assertNull(response.getAddedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void shouldSetStatusToWatching() {
        WatchlistResponse response = new WatchlistResponse();
        response.setStatus(WatchlistStatus.WATCHING);
        assertEquals(WatchlistStatus.WATCHING, response.getStatus());
    }

    @Test
    void shouldSetStatusToWatched() {
        WatchlistResponse response = new WatchlistResponse();
        response.setStatus(WatchlistStatus.WATCHED);
        assertEquals(WatchlistStatus.WATCHED, response.getStatus());
    }

    @Test
    void shouldAllowNullStatus() {
        WatchlistResponse response = new WatchlistResponse();
        response.setStatus(null);
        assertNull(response.getStatus());
    }

    @Test
    void shouldAllowUpdatedAtAfterAddedAt() {
        WatchlistResponse response = new WatchlistResponse();
        LocalDateTime addedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        response.setAddedAt(addedAt);
        response.setUpdatedAt(updatedAt);

        assertTrue(response.getUpdatedAt().isAfter(response.getAddedAt()));
    }
}