package com.mediatheque.media_svc.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, MediaStatusEvent> kafkaTemplate;

    @InjectMocks
    private MediaEventPublisher mediaEventPublisher;



    private MediaStatusEvent buildEvent(String status) {
        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(1L);
        event.setMediaTitle("Mon média");
        event.setOwnerId(824036515L);
        event.setStatus(status);
        return event;
    }


    @Test
    void publishStatusEvent_shouldSendToCorrectTopic() {
        MediaStatusEvent event = buildEvent("ACCEPTED");

        mediaEventPublisher.publishStatusEvent(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), any(MediaStatusEvent.class));
        assertThat(topicCaptor.getValue()).isEqualTo("media-decision");
    }

    @Test
    void publishStatusEvent_shouldUseMediaIdAsKey() {
        MediaStatusEvent event = buildEvent("ACCEPTED");

        mediaEventPublisher.publishStatusEvent(event);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any(MediaStatusEvent.class));
        assertThat(keyCaptor.getValue()).isEqualTo("1");
    }

    @Test
    void publishStatusEvent_shouldSendCorrectEvent() {
        MediaStatusEvent event = buildEvent("ACCEPTED");

        mediaEventPublisher.publishStatusEvent(event);

        ArgumentCaptor<MediaStatusEvent> eventCaptor = ArgumentCaptor.forClass(MediaStatusEvent.class);
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

        MediaStatusEvent captured = eventCaptor.getValue();
        assertThat(captured.getMediaId()).isEqualTo(1L);
        assertThat(captured.getMediaTitle()).isEqualTo("Mon média");
        assertThat(captured.getOwnerId()).isEqualTo(824036515L);
        assertThat(captured.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void publishStatusEvent_shouldSendAcceptedEvent() {
        MediaStatusEvent event = buildEvent("ACCEPTED");

        mediaEventPublisher.publishStatusEvent(event);

        verify(kafkaTemplate).send("media-decision", "1", event);
    }

    @Test
    void publishStatusEvent_shouldSendRefusedEvent() {
        MediaStatusEvent event = buildEvent("REFUSED");
        event.setReason("Contenu non conforme");

        mediaEventPublisher.publishStatusEvent(event);

        ArgumentCaptor<MediaStatusEvent> eventCaptor = ArgumentCaptor.forClass(MediaStatusEvent.class);
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

        assertThat(eventCaptor.getValue().getStatus()).isEqualTo("REFUSED");
        assertThat(eventCaptor.getValue().getReason()).isEqualTo("Contenu non conforme");
    }

    @Test
    void publishStatusEvent_shouldCallKafkaTemplateExactlyOnce() {
        MediaStatusEvent event = buildEvent("ACCEPTED");

        mediaEventPublisher.publishStatusEvent(event);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(MediaStatusEvent.class));
    }

    @Test
    void publishStatusEvent_shouldUseStringKeyFromMediaId() {
        MediaStatusEvent event = buildEvent("ACCEPTED");
        event.setMediaId(999L);

        mediaEventPublisher.publishStatusEvent(event);

        verify(kafkaTemplate).send(eq("media-decision"), eq("999"), any(MediaStatusEvent.class));
    }
}