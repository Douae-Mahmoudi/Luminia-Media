package com.collection.usecase.like;

import com.collection.domain.Like;
import com.collection.event.MediaLikedEvent;
import com.collection.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeMediaUseCaseTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private KafkaTemplate<String, MediaLikedEvent> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LikeMediaUseCase likeMediaUseCase;

    private LikeMediaUseCase.Input input;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(likeMediaUseCase, "mediaServiceUrl", "http://media-svc:8082");
        ReflectionTestUtils.setField(likeMediaUseCase, "authServiceUrl",  "http://auth-svc:8081");

        input = new LikeMediaUseCase.Input(
                "user@test.com",
                "456",
                "Mehdi",
                "Avatar",
                "123"
        );
    }

    @Test
    void execute_ShouldSaveLikeAndSendEvent_WhenNotAlreadyLiked() {
        when(likeRepository.existsByUserIdAndMediaId(input.userId(), input.mediaId())).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenAnswer(i -> i.getArgument(0));

        when(restTemplate.exchange(
                contains("/api/media/456"),
                any(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("ownerId", 789L)));

        Like result = likeMediaUseCase.execute(input);

        assertThat(result).isNotNull();
        verify(likeRepository).save(any(Like.class));

        ArgumentCaptor<MediaLikedEvent> eventCaptor = ArgumentCaptor.forClass(MediaLikedEvent.class);
        verify(kafkaTemplate).send(eq("media-liked"), eq("456"), eventCaptor.capture());

        MediaLikedEvent event = eventCaptor.getValue();
        assertThat(event.getMediaId()).isEqualTo(456L);
        assertThat(event.getOwnerId()).isEqualTo(789L);
        assertThat(event.getLikedByUserId()).isEqualTo(123L);
        assertThat(event.getLikedByUsername()).isEqualTo("Mehdi");
        assertThat(event.getMediaTitle()).isEqualTo("Avatar");
    }

    @Test
    void execute_ShouldThrowException_WhenAlreadyLiked() {
        when(likeRepository.existsByUserIdAndMediaId(input.userId(), input.mediaId())).thenReturn(true);

        assertThatThrownBy(() -> likeMediaUseCase.execute(input))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Media already liked");

        verify(likeRepository, never()).save(any());
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void execute_ShouldStillReturnLike_WhenKafkaFails() {
        when(likeRepository.existsByUserIdAndMediaId(input.userId(), input.mediaId())).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenAnswer(i -> i.getArgument(0));

        when(restTemplate.exchange(
                contains("/api/media/456"),
                any(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("ownerId", 789L)));

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka Error"));

        Like result = likeMediaUseCase.execute(input);

        assertThat(result).isNotNull();
        verify(likeRepository).save(any(Like.class));
    }
}
