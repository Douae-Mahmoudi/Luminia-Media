package com.collection.usecase.comment;

import com.collection.domain.Comment;
import com.collection.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCommentsByMediaUseCase {

    private final CommentRepository commentRepository;

    public GetCommentsByMediaUseCase(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> execute(String mediaId) {
        return commentRepository.findByMediaId(mediaId);
    }
}