package com.collection.infrastructure.persistence.adapter;

import com.collection.domain.Comment;
import com.collection.infrastructure.persistence.JpaCommentRepository;
import com.collection.mapper.CommentMapper;
import com.collection.repository.CommentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpa;

    public CommentRepositoryAdapter(JpaCommentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Comment save(Comment comment) {
        return CommentMapper.toDomain(jpa.save(CommentMapper.toEntity(comment)));
    }

    @Override
    public Optional<Comment> findById(String id) {
        return jpa.findById(id).map(CommentMapper::toDomain);
    }

    @Override
    public List<Comment> findByMediaId(String mediaId) {
        return jpa.findByMediaId(mediaId)
                .stream()
                .map(CommentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> findByUserId(String userId) {
        return jpa.findByUserId(userId)
                .stream()
                .map(CommentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return jpa.existsById(id);
    }

    @Override
    public void deleteById(String id) {
        jpa.deleteById(id);
    }
}