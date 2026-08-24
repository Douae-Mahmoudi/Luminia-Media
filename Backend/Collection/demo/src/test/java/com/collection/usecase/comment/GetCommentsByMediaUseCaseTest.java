package com.collection.usecase.comment;

import com.collection.domain.Comment;
import com.collection.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCommentsByMediaUseCaseTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private GetCommentsByMediaUseCase getCommentsByMediaUseCase;

    @Test
    void execute_shouldReturnComments_whenMediaIdExists() {
        List<Comment> comments = List.of(
                new Comment("id-1", "user-1", "42", "Super film !"),
                new Comment("id-2", "user-2", "42", "Excellent !")
        );
        when(commentRepository.findByMediaId("42")).thenReturn(comments);

        List<Comment> result = getCommentsByMediaUseCase.execute("42");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Super film !");
        assertThat(result.get(1).getContent()).isEqualTo("Excellent !");
        verify(commentRepository).findByMediaId("42");
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoCommentsExist() {
        when(commentRepository.findByMediaId("99")).thenReturn(List.of());

        List<Comment> result = getCommentsByMediaUseCase.execute("99");

        assertThat(result).isEmpty();
        verify(commentRepository).findByMediaId("99");
    }

    @Test
    void execute_shouldCallRepository_withCorrectMediaId() {
        when(commentRepository.findByMediaId("123")).thenReturn(List.of());

        getCommentsByMediaUseCase.execute("123");

        verify(commentRepository, times(1)).findByMediaId("123");
        verifyNoMoreInteractions(commentRepository);
    }
}
