package com.collection.infrastructure.persistence;

import com.collection.infrastructure.persistence.entity.CommentEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaCommentRepositoryTest {

    @Mock
    private JpaCommentRepository commentRepository;

    @Test
    void shouldFindCommentsByMediaId() {
        String mediaId = "movie-99";
        CommentEntity comment = new CommentEntity();

        List<CommentEntity> expectedComments = List.of(comment);

        when(commentRepository.findByMediaId(mediaId)).thenReturn(expectedComments);

        List<List<CommentEntity>> results = List.of(commentRepository.findByMediaId(mediaId));

        assertThat(results.get(0)).hasSize(1);
        verify(commentRepository, times(1)).findByMediaId(mediaId);
    }

    @Test
    void shouldFindCommentsByUserId() {
        String userId = "user-456";
        when(commentRepository.findByUserId(userId)).thenReturn(List.of(new CommentEntity()));

        List<CommentEntity> results = commentRepository.findByUserId(userId);


        assertThat(results).isNotEmpty();
        verify(commentRepository, times(1)).findByUserId(userId);
    }
}