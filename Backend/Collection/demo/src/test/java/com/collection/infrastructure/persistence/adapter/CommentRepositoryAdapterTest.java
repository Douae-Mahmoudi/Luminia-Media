package com.collection.infrastructure.persistence.adapter;

import com.collection.domain.Comment;
import com.collection.infrastructure.persistence.JpaCommentRepository;
import com.collection.infrastructure.persistence.entity.CommentEntity;
import com.collection.mapper.CommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentRepositoryAdapterTest {

    @Mock
    private JpaCommentRepository jpa;

    @InjectMocks
    private CommentRepositoryAdapter adapter;

    private Comment commentDomain;
    private CommentEntity commentEntity;

    private final String COMMENT_ID = "comm-123";
    private final String USER_ID = "user-456";
    private final String MEDIA_ID = "media-789";

    @BeforeEach
    void setUp() {
        commentDomain = new Comment(COMMENT_ID, USER_ID, MEDIA_ID, "Super contenu !");

        commentEntity = new CommentEntity();
        commentEntity.setId(COMMENT_ID);
        commentEntity.setUserId(USER_ID);
        commentEntity.setMediaId(MEDIA_ID);
        commentEntity.setContent("Super contenu !");
    }

    @Test
    void save_ShouldReturnMappedDomainComment() {
        when(jpa.save(any(CommentEntity.class))).thenReturn(commentEntity);

        Comment savedComment = adapter.save(commentDomain);
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isEqualTo(COMMENT_ID);
        verify(jpa, times(1)).save(any(CommentEntity.class));
    }

    @Test
    void findById_ShouldReturnOptionalComment() {
        when(jpa.findById(COMMENT_ID)).thenReturn(Optional.of(commentEntity));

        Optional<Comment> result = adapter.findById(COMMENT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("Super contenu !");
    }

    @Test
    void findByMediaId_ShouldReturnListOfComments() {
        when(jpa.findByMediaId(MEDIA_ID)).thenReturn(List.of(commentEntity));

        List<Comment> results = adapter.findByMediaId(MEDIA_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMediaId()).isEqualTo(MEDIA_ID);
        verify(jpa).findByMediaId(MEDIA_ID);
    }

    @Test
    void findByUserId_ShouldReturnListOfComments() {
        when(jpa.findByUserId(USER_ID)).thenReturn(List.of(commentEntity));

        List<Comment> results = adapter.findByUserId(USER_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(USER_ID);
        verify(jpa).findByUserId(USER_ID);
    }

    @Test
    void existsById_ShouldReturnTrueIfPresent() {
        when(jpa.existsById(COMMENT_ID)).thenReturn(true);

        boolean exists = adapter.existsById(COMMENT_ID);

        assertThat(exists).isTrue();
    }

    @Test
    void deleteById_ShouldInvokeJpaDelete() {
        adapter.deleteById(COMMENT_ID);

        verify(jpa, times(1)).deleteById(COMMENT_ID);
    }
}