package com.collection.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        CommentResponse response = new CommentResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setId("com1");
        response.setUserId("user1");
        response.setMediaId("media1");
        response.setContent("Super film !");
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        assertEquals("com1", response.getId());
        assertEquals("user1", response.getUserId());
        assertEquals("media1", response.getMediaId());
        assertEquals("Super film !", response.getContent());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void shouldDefaultAllFieldsToNull() {
        CommentResponse response = new CommentResponse();

        assertNull(response.getId());
        assertNull(response.getUserId());
        assertNull(response.getMediaId());
        assertNull(response.getContent());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void shouldAllowNullContent() {
        CommentResponse response = new CommentResponse();
        response.setContent(null);
        assertNull(response.getContent());
    }

    @Test
    void shouldAllowUpdatedAtDifferentFromCreatedAt() {
        CommentResponse response = new CommentResponse();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);

        assertTrue(response.getUpdatedAt().isAfter(response.getCreatedAt()));
    }
}





