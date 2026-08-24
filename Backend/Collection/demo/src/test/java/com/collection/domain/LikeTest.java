package com.collection.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LikeTest {

    @Test
    void shouldCreateLikeSuccessfully() {
        Like like = new Like("id1", "user1", "media1");
        assertEquals("id1", like.getId());
        assertEquals("user1", like.getUserId());
        assertEquals("media1", like.getMediaId());
        assertNotNull(like.getLikedAt());
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Like("id1", null, "media1"));
    }

    @Test
    void shouldThrowWhenUserIdIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Like("id1", "", "media1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Like("id1", "   ", "media1"));
    }

    @Test
    void shouldThrowWhenMediaIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Like("id1", "user1", null));
    }

    @Test
    void shouldThrowWhenMediaIdIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Like("id1", "user1", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Like("id1", "user1", "   "));
    }

    @Test
    void shouldSetLikedAtOnCreation() {
        var before = java.time.LocalDateTime.now();
        Like like = new Like("id1", "user1", "media1");
        var after = java.time.LocalDateTime.now();
        assertFalse(like.getLikedAt().isBefore(before));
        assertFalse(like.getLikedAt().isAfter(after));
    }

    @Test
    void shouldAllowNullId() {
        Like like = new Like(null, "user1", "media1");
        assertNull(like.getId());
    }
}





