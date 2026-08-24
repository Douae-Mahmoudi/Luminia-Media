package com.collection.event;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MediaLikedEventTest {

    @Test
    void shouldCreateEventWithAllArgsConstructor() {
        Long mediaId = 101L;
        Long ownerId = 202L;
        Long userId = 303L;
        String username = "Karim";
        String title = "Mon Super Media";

        MediaLikedEvent event = new MediaLikedEvent(mediaId, ownerId, userId, username, title);

        assertThat(event.getMediaId()).isEqualTo(mediaId);
        assertThat(event.getOwnerId()).isEqualTo(ownerId);
        assertThat(event.getLikedByUserId()).isEqualTo(userId);
        assertThat(event.getLikedByUsername()).isEqualTo(username);
        assertThat(event.getMediaTitle()).isEqualTo(title);
    }

    @Test
    void shouldCreateEventWithNoArgsConstructorAndSetters() {
        MediaLikedEvent event = new MediaLikedEvent();

        event.setMediaId(500L);
        event.setLikedByUsername("TestUser");

        assertThat(event.getMediaId()).isEqualTo(500L);
        assertThat(event.getLikedByUsername()).isEqualTo("TestUser");
    }

    @Test
    void testEqualsAndHashCode() {
        MediaLikedEvent event1 = new MediaLikedEvent(1L, 2L, 3L, "User", "Title");
        MediaLikedEvent event2 = new MediaLikedEvent(1L, 2L, 3L, "User", "Title");
        MediaLikedEvent event3 = new MediaLikedEvent(9L, 2L, 3L, "Other", "Title");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1).isNotEqualTo(event3);
    }

    @Test
    void testToString() {
        MediaLikedEvent event = new MediaLikedEvent(1L, 2L, 3L, "User", "Title");

        assertThat(event.toString())
                .contains("mediaId=1")
                .contains("likedByUsername=User")
                .contains("mediaTitle=Title");
    }
}