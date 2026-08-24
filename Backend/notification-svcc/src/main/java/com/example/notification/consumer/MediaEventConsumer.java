package com.example.notification.consumer;

import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.event.MediaLikedEvent;
import com.example.notification.event.MediaStatusEvent;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaEventConsumer {

    private final NotificationService notificationService;


    @KafkaListener(
            topics = "media-liked",
            groupId = "notif-group",
            containerFactory = "mediaLikedContainerFactory"
    )
    public void onMediaLiked(
            @Payload MediaLikedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("[Kafka] {} → mediaId={} owner={}",
                topic, event.getMediaId(), event.getOwnerId());

        String message = String.format(
                "%s a aimé votre média \"%s\"",
                event.getLikedByUsername(),
                event.getMediaTitle());

        notificationService.send(
                event.getOwnerId(),
                NotificationType.MEDIA_LIKED,
                message,
                event.getMediaId(),
                ReferenceType.MEDIA
        );
    }


    @KafkaListener(
            topics = "media-decision",
            groupId = "notif-group",
            containerFactory = "mediaStatusContainerFactory"
    )
    public void onMediaDecision(
            @Payload MediaStatusEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("[Kafka] {} → mediaId={} status={} owner={}",
                topic, event.getMediaId(), event.getStatus(), event.getOwnerId());

        switch (event.getStatus()) {

            case "ACCEPTED" -> {
                String message = String.format(
                        "Votre média \"%s\" a été accepté et publié !",
                        event.getMediaTitle());

                notificationService.send(
                        event.getOwnerId(),
                        NotificationType.MEDIA_ACCEPTED,
                        message,
                        event.getMediaId(),
                        ReferenceType.MEDIA
                );
            }

            case "REFUSED" -> {
                String reason = event.getReason() != null
                        ? " — Raison : " + event.getReason()
                        : "";

                String message = String.format(
                        "Votre média \"%s\" a été refusé.%s",
                        event.getMediaTitle(), reason);

                notificationService.send(
                        event.getOwnerId(),
                        NotificationType.MEDIA_REFUSED,
                        message,
                        event.getMediaId(),
                        ReferenceType.MEDIA
                );
            }

            default -> log.warn("[Kafka] Status inconnu : {}", event.getStatus());
        }
    }
}