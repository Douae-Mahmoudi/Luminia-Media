package com.collection.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        FavoriteResponse response = new FavoriteResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setId("fav1");
        response.setUserId("user1");
        response.setMediaId("media1");
        response.setFavoritedAt(now);

        assertEquals("fav1", response.getId());
        assertEquals("user1", response.getUserId());
        assertEquals("media1", response.getMediaId());
        assertEquals(now, response.getFavoritedAt());
    }

    @Test
    void shouldDefaultAllFieldsToNull() {
        FavoriteResponse response = new FavoriteResponse();

        assertNull(response.getId());
        assertNull(response.getUserId());
        assertNull(response.getMediaId());
        assertNull(response.getFavoritedAt());
    }

    @Test
    void shouldAllowNullFavoritedAt() {
        FavoriteResponse response = new FavoriteResponse();
        response.setFavoritedAt(null);
        assertNull(response.getFavoritedAt());
    }

    @Test
    void shouldStoreFavoritedAtCorrectly() {
        FavoriteResponse response = new FavoriteResponse();
        LocalDateTime favoritedAt = LocalDateTime.of(2024, 6, 15, 12, 0);
        response.setFavoritedAt(favoritedAt);
        assertEquals(2024, response.getFavoritedAt().getYear());
        assertEquals(6, response.getFavoritedAt().getMonthValue());
        assertEquals(15, response.getFavoritedAt().getDayOfMonth());
    }
}