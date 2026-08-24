package com.collection.controller;

import com.collection.domain.Comment;
import com.collection.usecase.comment.AddCommentUseCase;
import com.collection.usecase.comment.DeleteCommentUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AddCommentUseCase addCommentUseCase;

    @Mock
    private DeleteCommentUseCase deleteCommentUseCase;

    @InjectMocks
    private CommentController commentController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void addComment_shouldReturn200() throws Exception {
        Comment saved = new Comment("uuid-1", "user-1", "media-42", "Contenu");
        when(addCommentUseCase.execute(any())).thenReturn(saved);

        String body = "{\"mediaId\":\"media-42\",\"content\":\"Contenu\"}";

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user-1")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("uuid-1"));
    }

    @Test
    void deleteComment_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/comments/comment-99")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isNoContent());

        verify(deleteCommentUseCase).execute("comment-99", "user-1");
    }
}