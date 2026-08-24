package com.example.notification.consumer;

import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.event.CommentAddedEvent;
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
public class CollectionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "comment-added",
            groupId = "notif-group",
            containerFactory = "commentContainerFactory"
    )
    public void onCommentAdded(
            @Payload CommentAddedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("[Kafka] {} → mediaId={} commenter={} owner={}",
                topic, event.getMediaId(),
                event.getCommentedByUserId(), event.getOwnerId());

        if (event.getCommentedByUserId().equals(event.getOwnerId())) {
            log.debug("Auteur == propriétaire → notif ignorée");
            return;
        }

        String preview = event.getCommentContent() != null
                ? " : \"" + truncate(event.getCommentContent(), 60) + "\""
                : "";

        String message = String.format(
                "%s a commenté votre média \"%s\"%s",
                event.getCommentedByUsername(),
                event.getMediaTitle(),
                preview);

        notificationService.send(
                event.getOwnerId(),
                NotificationType.COMMENT_ADDED,
                message,
                event.getMediaId(),
                ReferenceType.MEDIA
        );
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}