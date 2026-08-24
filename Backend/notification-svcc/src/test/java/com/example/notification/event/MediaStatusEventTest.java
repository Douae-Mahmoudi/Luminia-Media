package com.example.notification.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaStatusEventTest {

    @Test
    @DisplayName("NoArgsConstructor — creates instance with all fields null")
    void noArgsConstructor_fieldsAreNull() {
        MediaStatusEvent event = new MediaStatusEvent();

        assertThat(event.getMediaId()).isNull();
        assertThat(event.getMediaTitle()).isNull();
        assertThat(event.getOwnerId()).isNull();
        assertThat(event.getStatus()).isNull();
        assertThat(event.getReason()).isNull();
    }

    @Test
    @DisplayName("Setters — update each field independently")
    void setters_updateFields() {
        MediaStatusEvent event = new MediaStatusEvent();

        event.setMediaId(5L);
        event.setMediaTitle("Documentary");
        event.setOwnerId(10L);
        event.setStatus("ACCEPTED");
        event.setReason(null);

        assertThat(event.getMediaId()).isEqualTo(5L);
        assertThat(event.getMediaTitle()).isEqualTo("Documentary");
        assertThat(event.getOwnerId()).isEqualTo(10L);
        assertThat(event.getStatus()).isEqualTo("ACCEPTED");
        assertThat(event.getReason()).isNull();
    }

    @Test
    @DisplayName("status REFUSED — reason can be set")
    void status_refused_reasonIsSet() {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setStatus("REFUSED");
        event.setReason("Does not meet quality standards");

        assertThat(event.getStatus()).isEqualTo("REFUSED");
        assertThat(event.getReason()).isEqualTo("Does not meet quality standards");
    }

    @Test
    @DisplayName("status ACCEPTED — reason stays null")
    void status_accepted_reasonIsNull() {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setStatus("ACCEPTED");

        assertThat(event.getStatus()).isEqualTo("ACCEPTED");
        assertThat(event.getReason()).isNull();
    }

    @Test
    @DisplayName("equals — two instances with same values are equal")
    void equals_sameValues_areEqual() {
        MediaStatusEvent a = new MediaStatusEvent();
        a.setMediaId(1L); a.setMediaTitle("t"); a.setOwnerId(2L);
        a.setStatus("ACCEPTED"); a.setReason(null);

        MediaStatusEvent b = new MediaStatusEvent();
        b.setMediaId(1L); b.setMediaTitle("t"); b.setOwnerId(2L);
        b.setStatus("ACCEPTED"); b.setReason(null);

        assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("equals — different status makes instances not equal")
    void equals_differentStatus_areNotEqual() {
        MediaStatusEvent a = new MediaStatusEvent();
        a.setMediaId(1L); a.setStatus("ACCEPTED");

        MediaStatusEvent b = new MediaStatusEvent();
        b.setMediaId(1L); b.setStatus("REFUSED");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("hashCode — equal objects have the same hashCode")
    void hashCode_equalObjects_sameHash() {
        MediaStatusEvent a = new MediaStatusEvent();
        a.setMediaId(1L); a.setStatus("ACCEPTED");

        MediaStatusEvent b = new MediaStatusEvent();
        b.setMediaId(1L); b.setStatus("ACCEPTED");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("toString — contains all non-null field values")
    void toString_containsAllFields() {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(3L);
        event.setMediaTitle("Film");
        event.setOwnerId(4L);
        event.setStatus("REFUSED");
        event.setReason("Too short");

        String str = event.toString();
        assertThat(str).contains("3", "Film", "4", "REFUSED", "Too short");
    }
}