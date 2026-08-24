package com.collection.usecase.like;

import com.collection.domain.Like;
import com.collection.event.MediaLikedEvent;
import com.collection.repository.LikeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class LikeMediaUseCase {

    private final LikeRepository likeRepository;
    private final KafkaTemplate<String, MediaLikedEvent> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Value("${services.media-svc.url:http://media-svc:8082}")
    private String mediaServiceUrl;

    @Value("${services.auth-svc.url:http://auth-svc:8081}")
    private String authServiceUrl;

    public LikeMediaUseCase(LikeRepository likeRepository,
                            KafkaTemplate<String, MediaLikedEvent> kafkaTemplate,
                            RestTemplate restTemplate) {
        this.likeRepository = likeRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
    }

    public record Input(String userId, String mediaId, String username,
                        String mediaTitle, String numericUserId) {}

    public Like execute(Input input) {
        log.info("[LikeMedia] Début - mediaId={}, userId={}", input.mediaId(), input.userId());

        if (likeRepository.existsByUserIdAndMediaId(input.userId(), input.mediaId())) {
            log.warn("[LikeMedia] Media déjà liké - mediaId={}, userId={}", input.mediaId(), input.userId());
            throw new RuntimeException("Media already liked");
        }

        String jwtToken = getCurrentJwtToken();

        Long ownerId = getMediaOwnerId(input.mediaId(), jwtToken);
        log.info("[LikeMedia] ownerId récupéré = {}", ownerId);

        Long numericUserId = getNumericUserId(input.numericUserId(), input.userId(), jwtToken);
        log.info("[LikeMedia] numericUserId récupéré = {}", numericUserId);

        Like like = new Like(
                UUID.randomUUID().toString(),
                input.userId(),
                input.mediaId()
        );
        Like saved = likeRepository.save(like);
        log.info("[LikeMedia] Like sauvegardé - id={}", saved.getId());

        if (ownerId != null && ownerId > 0 && numericUserId != null && numericUserId > 0) {
            try {
                MediaLikedEvent event = new MediaLikedEvent(
                        Long.parseLong(input.mediaId()),
                        ownerId,
                        numericUserId,
                        input.username(),
                        input.mediaTitle()
                );
                kafkaTemplate.send("media-liked", input.mediaId(), event);
                log.info("[Kafka] media-liked envoyé - mediaId={}, owner={}, userId={}",
                        input.mediaId(), ownerId, numericUserId);
            } catch (Exception e) {
                log.error("[Kafka] Échec envoi media-liked - {}", e.getMessage(), e);
            }
        } else {
            log.warn("[Kafka] Événement non envoyé - ownerId={}, numericUserId={}", ownerId, numericUserId);
        }

        return saved;
    }

    private String getCurrentJwtToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer le token JWT: {}", e.getMessage());
        }
        return null;
    }

    private Long getMediaOwnerId(String mediaId, String jwtToken) {
        String url = mediaServiceUrl + "/api/media/" + mediaId;
        log.debug("[MediaService] Appel REST: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null) {
                headers.setBearerAuth(jwtToken);
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            if (response != null && response.containsKey("ownerId")) {
                Object ownerIdObj = response.get("ownerId");
                if (ownerIdObj instanceof Number) {
                    return ((Number) ownerIdObj).longValue();
                } else if (ownerIdObj instanceof String) {
                    return Long.parseLong((String) ownerIdObj);
                }
            }
            log.warn("[MediaService] ownerId non trouvé pour mediaId={}", mediaId);
        } catch (RestClientException e) {
            log.error("[MediaService] Erreur appel REST pour mediaId={} - {}", mediaId, e.getMessage());
        } catch (Exception e) {
            log.error("[MediaService] Erreur inattendue pour mediaId={} - {}", mediaId, e.getMessage(), e);
        }
        return 0L;
    }

    private Long getNumericUserId(String numericUserId, String email, String jwtToken) {
        if (numericUserId != null && !numericUserId.isEmpty() && !"0".equals(numericUserId)) {
            try {
                return Long.parseLong(numericUserId);
            } catch (NumberFormatException e) {
            }
        }

        String url = authServiceUrl + "/api/users/by-email/" + email;
        log.debug("[AuthService] Appel REST: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null) {
                headers.setBearerAuth(jwtToken);
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            if (response != null && response.containsKey("id")) {
                Object idObj = response.get("id");
                if (idObj instanceof Number) {
                    return ((Number) idObj).longValue();
                } else if (idObj instanceof String) {
                    return Long.parseLong((String) idObj);
                }
            }
            log.warn("[AuthService] id non trouvé pour user={}", email);
        } catch (RestClientException e) {
            log.error("[AuthService] Erreur appel REST pour user={} - {}", email, e.getMessage());
        } catch (Exception e) {
            log.error("[AuthService] Erreur inattendue pour user={} - {}", email, e.getMessage(), e);
        }
        return 0L;
    }
}