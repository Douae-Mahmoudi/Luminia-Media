package com.collection.repository;

import com.collection.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(String id);

    List<Comment> findByMediaId(String mediaId);

    List<Comment> findByUserId(String userId);

    boolean existsById(String id);

    void deleteById(String id);
}