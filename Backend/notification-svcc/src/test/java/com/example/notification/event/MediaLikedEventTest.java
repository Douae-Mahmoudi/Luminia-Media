package com.example.notification.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaLikedEventTest {

    @Test
    @DisplayName("NoArgsConstructor — creates instance with all fields null")
    void noArgsConstructor_fieldsAreNull() {
        MediaLikedEvent event = new MediaLikedEvent();

        assertThat(event.getMediaId()).isNull();
        assertThat(event.getMediaTitle()).isNull();
        assertThat(event.getLikedByUserId()).isNull();
        assertThat(event.getLikedByUsername()).isNull();
        assertThat(event.getOwnerId()).isNull();
    }

    @Test
    @DisplayName("Setters — update each field independently")
    void setters_updateFields() {
        MediaLikedEvent event = new MediaLikedEvent();

        event.setMediaId(10L);
        event.setMediaTitle("Cool Video");
        event.setLikedByUserId(5L);
        event.setLikedByUsername("charlie");
        event.setOwnerId(99L);

        assertThat(event.getMediaId()).isEqualTo(10L);
        assertThat(event.getMediaTitle()).isEqualTo("Cool Video");
        assertThat(event.getLikedByUserId()).isEqualTo(5L);
        assertThat(event.getLikedByUsername()).isEqualTo("charlie");
        assertThat(event.getOwnerId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("equals — two instances with same values are equal")
    void equals_sameValues_areEqual() {
        MediaLikedEvent a = new MediaLikedEvent();
        a.setMediaId(1L); a.setMediaTitle("t"); a.setLikedByUserId(2L);
        a.setLikedByUsername("u"); a.setOwnerId(3L);

        MediaLikedEvent b = new MediaLikedEvent();
        b.setMediaId(1L); b.setMediaTitle("t"); b.setLikedByUserId(2L);
        b.setLikedByUsername("u"); b.setOwnerId(3L);

        assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("equals — two instances with different values are not equal")
    void equals_differentValues_areNotEqual() {
        MediaLikedEvent a = new MediaLikedEvent();
        a.setMediaId(1L);

        MediaLikedEvent b = new MediaLikedEvent();
        b.setMediaId(2L);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("hashCode — equal objects have the same hashCode")
    void hashCode_equalObjects_sameHash() {
        MediaLikedEvent a = new MediaLikedEvent();
        a.setMediaId(1L); a.setOwnerId(2L);

        MediaLikedEvent b = new MediaLikedEvent();
        b.setMediaId(1L); b.setOwnerId(2L);

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("toString — contains all field values")
    void toString_containsAllFields() {
        MediaLikedEvent event = new MediaLikedEvent();
        event.setMediaId(7L);
        event.setMediaTitle("My Media");
        event.setLikedByUserId(8L);
        event.setLikedByUsername("dave");
        event.setOwnerId(9L);

        String str = event.toString();
        assertThat(str).contains("7", "My Media", "8", "dave", "9");
    }
}