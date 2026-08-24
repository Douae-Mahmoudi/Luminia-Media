package com.collection.usecase.comment;

import com.collection.domain.Comment;
import com.collection.event.CommentAddedEvent;
import com.collection.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AddCommentUseCase {

    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, CommentAddedEvent> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Value("${services.media-svc.url:http://media-svc:8082}")
    private String mediaServiceUrl;

    public AddCommentUseCase(CommentRepository commentRepository,
                             KafkaTemplate<String, CommentAddedEvent> kafkaTemplate,
                             RestTemplate restTemplate) {
        this.commentRepository = commentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
    }

    public record Input(String userId, String mediaId, String content,
                        String username, String ownerId, String mediaTitle,
                        String numericUserId) {}

    public Comment execute(Input input) {
        Comment comment = new Comment(
                UUID.randomUUID().toString(),
                input.userId(),
                input.mediaId(),
                input.content()
        );
        Comment saved = commentRepository.save(comment);

        Long realOwnerId = getRealOwnerId(input.mediaId());

        Long numericUserId;
        try {
            numericUserId = Long.parseLong(input.numericUserId());
        } catch (Exception e) {
            numericUserId = 0L;
        }

        try {
            CommentAddedEvent event = new CommentAddedEvent(
                    Long.parseLong(input.mediaId()),
                    realOwnerId,
                    numericUserId,
                    input.username(),
                    input.mediaTitle(),
                    input.content()
            );
            kafkaTemplate.send("comment-added", input.mediaId(), event);
            log.info("[Kafka] comment-added — mediaId={} owner={} userId={}",
                    input.mediaId(), realOwnerId, numericUserId);

        } catch (Exception e) {
            log.error("[Kafka] Échec comment-added — {}", e.getMessage());
        }

        return saved;
    }

    private Long getRealOwnerId(String mediaId) {
        try {
            String url = mediaServiceUrl + "/api/media/" + mediaId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("ownerId")) {
                Object ownerIdObj = response.get("ownerId");
                if (ownerIdObj instanceof Number) {
                    return ((Number) ownerIdObj).longValue();
                } else if (ownerIdObj instanceof String) {
                    return Long.parseLong((String) ownerIdObj);
                }
            }
        } catch (Exception e) {
            log.error("[MediaService] Erreur pour mediaId={} - {}", mediaId, e.getMessage());
        }
        return 0L;
    }
}