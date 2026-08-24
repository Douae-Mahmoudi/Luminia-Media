package com.collection.usecase.like;

import com.collection.repository.LikeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnlikeMediaUseCaseTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private UnlikeMediaUseCase useCase;

    @Test
    void shouldUnlikeSuccessfully() {
        when(likeRepository.existsByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        assertDoesNotThrow(() -> useCase.execute("user1", "media1"));

        verify(likeRepository).deleteByUserIdAndMediaId("user1", "media1");
    }

    @Test
    void shouldThrowWhenLikeNotFound() {
        when(likeRepository.existsByUserIdAndMediaId("user1", "media1")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.execute("user1", "media1"));

        assertEquals("Like not found", ex.getMessage());
        verify(likeRepository, never()).deleteByUserIdAndMediaId(any(), any());
    }

    @Test
    void shouldCallDeleteOnce() {
        when(likeRepository.existsByUserIdAndMediaId("user1", "media1")).thenReturn(true);

        useCase.execute("user1", "media1");

        verify(likeRepository, times(1)).deleteByUserIdAndMediaId("user1", "media1");
    }

    @Test
    void shouldNotCallDeleteWhenLikeNotFound() {
        when(likeRepository.existsByUserIdAndMediaId("user1", "media1")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> useCase.execute("user1", "media1"));

        verify(likeRepository, never()).deleteByUserIdAndMediaId(any(), any());
    }
}