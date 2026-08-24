package com.collection.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void shouldCreateCommentSuccessfully() {
        Comment comment = new Comment("id1", "user1", "media1", "Super film !");
        assertEquals("Super film !", comment.getContent());
        assertEquals("user1", comment.getUserId());
        assertEquals("media1", comment.getMediaId());
    }

    @Test
    void shouldThrowWhenContentIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Comment("id1", "user1", "media1", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Comment("id1", "user1", "media1", "   "));
        assertThrows(IllegalArgumentException.class,
                () -> new Comment("id1", "user1", "media1", null));
    }

    @Test
    void shouldThrowWhenContentExceeds1000Characters() {
        String longContent = "a".repeat(1001);
        assertThrows(IllegalArgumentException.class,
                () -> new Comment("id1", "user1", "media1", longContent));
    }

    @Test
    void shouldAcceptContentOfExactly1000Characters() {
        String content = "a".repeat(1000);
        assertDoesNotThrow(() -> new Comment("id1", "user1", "media1", content));
    }

    @Test
    void shouldEditContentSuccessfully() {
        Comment comment = new Comment("id1", "user1", "media1", "Ancien contenu");
        comment.edit("Nouveau contenu");
        assertEquals("Nouveau contenu", comment.getContent());
    }

    @Test
    void shouldThrowWhenEditWithBlankContent() {
        Comment comment = new Comment("id1", "user1", "media1", "Contenu");
        assertThrows(IllegalArgumentException.class, () -> comment.edit(""));
        assertThrows(IllegalArgumentException.class, () -> comment.edit(null));
    }

    @Test
    void shouldThrowWhenEditExceeds1000Characters() {
        Comment comment = new Comment("id1", "user1", "media1", "Contenu");
        assertThrows(IllegalArgumentException.class,
                () -> comment.edit("a".repeat(1001)));
    }

    @Test
    void shouldReturnTrueWhenOwnedByCorrectUser() {
        Comment comment = new Comment("id1", "user1", "media1", "Contenu");
        assertTrue(comment.isOwnedBy("user1"));
    }

    @Test
    void shouldReturnFalseWhenNotOwnedByUser() {
        Comment comment = new Comment("id1", "user1", "media1", "Contenu");
        assertFalse(comment.isOwnedBy("user2"));
    }

    @Test
    void shouldUpdateUpdatedAtOnEdit() throws InterruptedException {
        Comment comment = new Comment("id1", "user1", "media1", "Contenu");
        var before = comment.getUpdatedAt();
        Thread.sleep(10);
        comment.edit("Nouveau contenu");
        assertTrue(comment.getUpdatedAt().isAfter(before));
    }
}