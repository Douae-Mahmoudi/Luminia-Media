package com.mediatheque.media_svc.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaStatusEventTest {


    private MediaStatusEvent buildFull() {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(1L);
        event.setMediaTitle("Mon média");
        event.setOwnerId(824036515L);
        event.setStatus("ACCEPTED");
        event.setReason(null);
        return event;
    }


    @Test
    void noArgsConstructor_shouldCreateEmptyEvent() {
        MediaStatusEvent event = new MediaStatusEvent();

        assertThat(event.getMediaId()).isNull();
        assertThat(event.getMediaTitle()).isNull();
        assertThat(event.getOwnerId()).isNull();
        assertThat(event.getStatus()).isNull();
        assertThat(event.getReason()).isNull();
    }


    @Test
    void setters_shouldSetAllFields() {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(1L);
        event.setMediaTitle("Mon média");
        event.setOwnerId(824036515L);
        event.setStatus("ACCEPTED");
        event.setReason("Raison test");

        assertThat(event.getMediaId()).isEqualTo(1L);
        assertThat(event.getMediaTitle()).isEqualTo("Mon média");
        assertThat(event.getOwnerId()).isEqualTo(824036515L);
        assertThat(event.getStatus()).isEqualTo("ACCEPTED");
        assertThat(event.getReason()).isEqualTo("Raison test");
    }


    @Test
    void status_shouldAcceptAccepted() {
        MediaStatusEvent event = buildFull();
        event.setStatus("ACCEPTED");
        assertThat(event.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void status_shouldAcceptRefused() {
        MediaStatusEvent event = buildFull();
        event.setStatus("REFUSED");
        assertThat(event.getStatus()).isEqualTo("REFUSED");
    }

    @Test
    void reason_shouldBeNull_whenAccepted() {
        MediaStatusEvent event = buildFull();
        event.setStatus("ACCEPTED");
        event.setReason(null);
        assertThat(event.getReason()).isNull();
    }

    @Test
    void reason_shouldBeSet_whenRefused() {
        MediaStatusEvent event = buildFull();
        event.setStatus("REFUSED");
        event.setReason("Contenu non conforme");
        assertThat(event.getReason()).isEqualTo("Contenu non conforme");
    }


    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        MediaStatusEvent e1 = buildFull();
        MediaStatusEvent e2 = buildFull();
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentMediaId() {
        MediaStatusEvent e1 = buildFull();
        MediaStatusEvent e2 = buildFull();
        e2.setMediaId(99L);
        assertThat(e1).isNotEqualTo(e2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentStatus() {
        MediaStatusEvent e1 = buildFull();
        MediaStatusEvent e2 = buildFull();
        e2.setStatus("REFUSED");
        assertThat(e1).isNotEqualTo(e2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentOwnerId() {
        MediaStatusEvent e1 = buildFull();
        MediaStatusEvent e2 = buildFull();
        e2.setOwnerId(999L);
        assertThat(e1).isNotEqualTo(e2);
    }


    @Test
    void toString_shouldContainAllFields() {
        MediaStatusEvent event = buildFull();
        String str = event.toString();

        assertThat(str).contains("1");
        assertThat(str).contains("Mon média");
        assertThat(str).contains("824036515");
        assertThat(str).contains("ACCEPTED");
    }


    @Test
    void event_shouldAllowNullFields() {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(null);
        event.setMediaTitle(null);
        event.setOwnerId(null);
        event.setStatus(null);
        event.setReason(null);

        assertThat(event.getMediaId()).isNull();
        assertThat(event.getMediaTitle()).isNull();
        assertThat(event.getOwnerId()).isNull();
        assertThat(event.getStatus()).isNull();
        assertThat(event.getReason()).isNull();
    }
}