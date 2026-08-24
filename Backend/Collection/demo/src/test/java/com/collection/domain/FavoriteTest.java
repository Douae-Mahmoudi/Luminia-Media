package com.collection.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FavoriteTest {

    @Test
    void shouldCreateFavoriteSuccessfully() {
        Favorite favorite = new Favorite("id1", "user1", "media1");
        assertEquals("user1", favorite.getUserId());
        assertEquals("media1", favorite.getMediaId());
        assertNotNull(favorite.getFavoritedAt());
    }

    @Test
    void shouldThrowWhenUserIdIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Favorite("id1", "", "media1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Favorite("id1", null, "media1"));
    }

    @Test
    void shouldThrowWhenMediaIdIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Favorite("id1", "user1", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Favorite("id1", "user1", null));
    }
}

