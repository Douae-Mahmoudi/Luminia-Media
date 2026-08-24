package com.collection.usecase.comment;

import com.collection.domain.Comment;
import com.collection.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCommentUseCaseTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private DeleteCommentUseCase useCase;

    @Test
    void shouldDeleteCommentSuccessfully() {
        Comment comment = new Comment("com1", "user1", "media1", "Contenu");
        when(commentRepository.findById("com1")).thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> useCase.execute("com1", "user1"));

        verify(commentRepository).deleteById("com1");
    }

    @Test
    void shouldThrowWhenCommentNotFound() {
        when(commentRepository.findById("com1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.execute("com1", "user1"));

        assertEquals("Comment not found", ex.getMessage());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowWhenUserIsNotOwner() {
        Comment comment = new Comment("com1", "user1", "media1", "Contenu");
        when(commentRepository.findById("com1")).thenReturn(Optional.of(comment));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.execute("com1", "user2"));

        assertEquals("Access denied — not your comment", ex.getMessage());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void shouldCallDeleteByIdOnce() {
        Comment comment = new Comment("com1", "user1", "media1", "Contenu");
        when(commentRepository.findById("com1")).thenReturn(Optional.of(comment));

        useCase.execute("com1", "user1");

        verify(commentRepository, times(1)).deleteById("com1");
    }

    @Test
    void shouldNotCallDeleteWhenAccessDenied() {
        Comment comment = new Comment("com1", "user1", "media1", "Contenu");
        when(commentRepository.findById("com1")).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () -> useCase.execute("com1", "user2"));

        verify(commentRepository, never()).deleteById(any());
    }
}