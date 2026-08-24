package com.collection.usecase.comment;

import com.collection.domain.Comment;
import com.collection.event.CommentAddedEvent;
import com.collection.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCommentUseCaseTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private KafkaTemplate<String, CommentAddedEvent> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AddCommentUseCase addCommentUseCase;

    private AddCommentUseCase.Input validInput;

    @BeforeEach
    void setUp() {
        validInput = new AddCommentUseCase.Input(
                "1",
                "42",
                "Super film",
                "alice",
                "99",
                "Inception",
                "1"
        );
    }

    @Test
    void execute_shouldSaveCommentWithCorrectFields() {
        Comment saved = new Comment("uuid-123", "1", "42", "Super film");
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        addCommentUseCase.execute(validInput);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo("1");
        assertThat(captured.getContent()).isEqualTo("Super film");
        assertThat(captured.getId()).isNotBlank();
    }

    @Test
    void execute_shouldSendKafkaEventWithCorrectData() {
        when(commentRepository.save(any())).thenReturn(new Comment("id", "1", "42", "Super film"));

        addCommentUseCase.execute(validInput);

        ArgumentCaptor<CommentAddedEvent> eventCaptor = ArgumentCaptor.forClass(CommentAddedEvent.class);
        verify(kafkaTemplate).send(eq("comment-added"), eq("42"), eventCaptor.capture());

        CommentAddedEvent event = eventCaptor.getValue();
        assertThat(event.getMediaId()).isEqualTo(42L);
        assertThat(event.getCommentedByUserId()).isEqualTo(1L);
        assertThat(event.getCommentedByUsername()).isEqualTo("alice");
        assertThat(event.getMediaTitle()).isEqualTo("Inception");
        assertThat(event.getCommentContent()).isEqualTo("Super film");
    }

    @Test
    void execute_shouldStillReturnComment_whenKafkaThrows() {
        Comment saved = new Comment("uuid-123", "1", "42", "Super film");
        when(commentRepository.save(any())).thenReturn(saved);
        when(kafkaTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("Kafka indisponible"));

        Comment result = addCommentUseCase.execute(validInput);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void execute_shouldNotCrash_whenIdsAreNotNumeric() {
        AddCommentUseCase.Input badInput = new AddCommentUseCase.Input(
                "not-a-number",
                "also-bad",
                "Contenu",
                "bob",
                "bad-owner",
                "Film",
                "not-numeric"
        );
        Comment saved = new Comment("id", "not-a-number", "also-bad", "Contenu");
        when(commentRepository.save(any())).thenReturn(saved);

        Comment result = addCommentUseCase.execute(badInput);

        assertThat(result).isEqualTo(saved);
    }
}
