package com.mediatheque.media_svc.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaEventPublisher {

    private static final String TOPIC = "media-decision";

    private final KafkaTemplate<String, MediaStatusEvent> kafkaTemplate;

    public void publishStatusEvent(MediaStatusEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.getMediaId()), event);
    }
}