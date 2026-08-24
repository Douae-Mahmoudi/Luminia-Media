package com.collection.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LikeResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        LikeResponse response = new LikeResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setId("like1");
        response.setUserId("user1");
        response.setMediaId("media1");
        response.setLikedAt(now);

        assertEquals("like1", response.getId());
        assertEquals("user1", response.getUserId());
        assertEquals("media1", response.getMediaId());
        assertEquals(now, response.getLikedAt());
    }

    @Test
    void shouldDefaultAllFieldsToNull() {
        LikeResponse response = new LikeResponse();

        assertNull(response.getId());
        assertNull(response.getUserId());
        assertNull(response.getMediaId());
        assertNull(response.getLikedAt());
    }

    @Test
    void shouldAllowNullLikedAt() {
        LikeResponse response = new LikeResponse();
        response.setLikedAt(null);
        assertNull(response.getLikedAt());
    }

    @Test
    void shouldStoreLikedAtCorrectly() {
        LikeResponse response = new LikeResponse();
        LocalDateTime likedAt = LocalDateTime.of(2024, 6, 15, 12, 0);
        response.setLikedAt(likedAt);
        assertEquals(2024, response.getLikedAt().getYear());
        assertEquals(6, response.getLikedAt().getMonthValue());
        assertEquals(15, response.getLikedAt().getDayOfMonth());
    }
}
