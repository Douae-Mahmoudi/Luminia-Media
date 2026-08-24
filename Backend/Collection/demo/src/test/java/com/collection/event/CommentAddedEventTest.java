package com.collection.event;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CommentAddedEventTest {

    @Test
    void shouldCreateEventWithAllArgsConstructor() {
        Long mediaId = 1L;
        Long ownerId = 2L;
        Long userId = 3L;
        String username = "Alice";
        String title = "Inception";
        String content = "Super film !";

        CommentAddedEvent event = new CommentAddedEvent(mediaId, ownerId, userId, username, title, content);

        assertThat(event.getMediaId()).isEqualTo(mediaId);
        assertThat(event.getOwnerId()).isEqualTo(ownerId);
        assertThat(event.getCommentedByUserId()).isEqualTo(userId);
        assertThat(event.getCommentedByUsername()).isEqualTo(username);
        assertThat(event.getMediaTitle()).isEqualTo(title);
        assertThat(event.getCommentContent()).isEqualTo(content);
    }

    @Test
    void shouldCreateEventWithNoArgsConstructorAndSetters() {
        CommentAddedEvent event = new CommentAddedEvent();

        event.setMediaId(10L);
        event.setCommentContent("Nouveau commentaire");

        assertThat(event.getMediaId()).isEqualTo(10L);
        assertThat(event.getCommentContent()).isEqualTo("Nouveau commentaire");
    }

    @Test
    void testEqualsAndHashCode() {
        CommentAddedEvent event1 = new CommentAddedEvent(1L, 2L, 3L, "User", "Title", "Text");
        CommentAddedEvent event2 = new CommentAddedEvent(1L, 2L, 3L, "User", "Title", "Text");
        CommentAddedEvent event3 = new CommentAddedEvent(9L, 9L, 9L, "Diff", "Diff", "Diff");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1).isNotEqualTo(event3);
    }

    @Test
    void testToString() {
        CommentAddedEvent event = new CommentAddedEvent(1L, 2L, 3L, "User", "Title", "Text");

        assertThat(event.toString()).contains("mediaId=1", "commentedByUsername=User", "commentContent=Text");
    }
}