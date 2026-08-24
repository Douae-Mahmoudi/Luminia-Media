package com.example.notification.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentAddedEventTest {

    @Test
    @DisplayName("NoArgsConstructor — creates instance with all fields null")
    void noArgsConstructor_fieldsAreNull() {
        CommentAddedEvent event = new CommentAddedEvent();

        assertThat(event.getMediaId()).isNull();
        assertThat(event.getOwnerId()).isNull();
        assertThat(event.getCommentedByUserId()).isNull();
        assertThat(event.getCommentedByUsername()).isNull();
        assertThat(event.getMediaTitle()).isNull();
        assertThat(event.getCommentContent()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor — sets all fields correctly")
    void allArgsConstructor_setsAllFields() {
        CommentAddedEvent event = new CommentAddedEvent(
                10L, 20L, 30L, "alice", "My Video", "Great content!");

        assertThat(event.getMediaId()).isEqualTo(10L);
        assertThat(event.getOwnerId()).isEqualTo(20L);
        assertThat(event.getCommentedByUserId()).isEqualTo(30L);
        assertThat(event.getCommentedByUsername()).isEqualTo("alice");
        assertThat(event.getMediaTitle()).isEqualTo("My Video");
        assertThat(event.getCommentContent()).isEqualTo("Great content!");
    }

    @Test
    @DisplayName("Setters — update each field independently")
    void setters_updateFields() {
        CommentAddedEvent event = new CommentAddedEvent();

        event.setMediaId(1L);
        event.setOwnerId(2L);
        event.setCommentedByUserId(3L);
        event.setCommentedByUsername("bob");
        event.setMediaTitle("Test Title");
        event.setCommentContent("Nice!");

        assertThat(event.getMediaId()).isEqualTo(1L);
        assertThat(event.getOwnerId()).isEqualTo(2L);
        assertThat(event.getCommentedByUserId()).isEqualTo(3L);
        assertThat(event.getCommentedByUsername()).isEqualTo("bob");
        assertThat(event.getMediaTitle()).isEqualTo("Test Title");
        assertThat(event.getCommentContent()).isEqualTo("Nice!");
    }

    @Test
    @DisplayName("equals — two instances with same values are equal")
    void equals_sameValues_areEqual() {
        CommentAddedEvent a = new CommentAddedEvent(1L, 2L, 3L, "alice", "title", "comment");
        CommentAddedEvent b = new CommentAddedEvent(1L, 2L, 3L, "alice", "title", "comment");

        assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("equals — two instances with different values are not equal")
    void equals_differentValues_areNotEqual() {
        CommentAddedEvent a = new CommentAddedEvent(1L, 2L, 3L, "alice", "title", "comment");
        CommentAddedEvent b = new CommentAddedEvent(9L, 2L, 3L, "alice", "title", "comment");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("hashCode — equal objects have the same hashCode")
    void hashCode_equalObjects_sameHash() {
        CommentAddedEvent a = new CommentAddedEvent(1L, 2L, 3L, "alice", "title", "comment");
        CommentAddedEvent b = new CommentAddedEvent(1L, 2L, 3L, "alice", "title", "comment");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("toString — contains all field values")
    void toString_containsAllFields() {
        CommentAddedEvent event = new CommentAddedEvent(1L, 2L, 3L, "alice", "My Video", "Nice!");
        String str = event.toString();

        assertThat(str).contains("1", "2", "3", "alice", "My Video", "Nice!");
    }
}