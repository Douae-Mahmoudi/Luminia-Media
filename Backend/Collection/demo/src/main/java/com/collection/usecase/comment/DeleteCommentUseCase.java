package com.collection.usecase.comment;

import com.collection.domain.Comment;
import com.collection.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteCommentUseCase {

    private final CommentRepository commentRepository;

    public DeleteCommentUseCase(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public void execute(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.isOwnedBy(userId))
            throw new RuntimeException("Access denied — not your comment");

        commentRepository.deleteById(commentId);
    }
}