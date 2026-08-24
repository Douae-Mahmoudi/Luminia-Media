package com.collection.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WatchlistTest {

    @Test
    void shouldCreateWatchlistWithDefaultStatusToWatch() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        assertEquals(Watchlist.WatchlistStatus.TO_WATCH, watchlist.getStatus());
    }

    @Test
    void shouldCreateWatchlistWithCorrectFields() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        assertEquals("id1", watchlist.getId());
        assertEquals("user1", watchlist.getUserId());
        assertEquals("media1", watchlist.getMediaId());
    }

    @Test
    void shouldSetAddedAtAndUpdatedAtOnCreation() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        assertNotNull(watchlist.getAddedAt());
        assertNotNull(watchlist.getUpdatedAt());
    }

    @Test
    void shouldUpdateStatusToWatching() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        watchlist.updateStatus(Watchlist.WatchlistStatus.WATCHING);
        assertEquals(Watchlist.WatchlistStatus.WATCHING, watchlist.getStatus());
    }

    @Test
    void shouldUpdateStatusToWatched() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        watchlist.updateStatus(Watchlist.WatchlistStatus.WATCHED);
        assertEquals(Watchlist.WatchlistStatus.WATCHED, watchlist.getStatus());
    }

    @Test
    void shouldUpdateStatusBackToToWatch() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        watchlist.updateStatus(Watchlist.WatchlistStatus.WATCHED);
        watchlist.updateStatus(Watchlist.WatchlistStatus.TO_WATCH);
        assertEquals(Watchlist.WatchlistStatus.TO_WATCH, watchlist.getStatus());
    }

    @Test
    void shouldThrowWhenStatusIsNull() {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        assertThrows(IllegalArgumentException.class,
                () -> watchlist.updateStatus(null));
    }

    @Test
    void shouldUpdateUpdatedAtAfterStatusChange() throws InterruptedException {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        var before = watchlist.getUpdatedAt();
        Thread.sleep(10);
        watchlist.updateStatus(Watchlist.WatchlistStatus.WATCHING);
        assertTrue(watchlist.getUpdatedAt().isAfter(before));
    }

    @Test
    void shouldNotChangeAddedAtAfterStatusChange() throws InterruptedException {
        Watchlist watchlist = new Watchlist("id1", "user1", "media1");
        var addedAt = watchlist.getAddedAt();
        Thread.sleep(10);
        watchlist.updateStatus(Watchlist.WatchlistStatus.WATCHING);
        assertEquals(addedAt, watchlist.getAddedAt());
    }
}
