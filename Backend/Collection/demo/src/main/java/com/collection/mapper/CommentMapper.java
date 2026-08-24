package com.collection.mapper;

import com.collection.domain.Comment;
import com.collection.dto.request.CommentRequest;
import com.collection.dto.response.CommentResponse;
import com.collection.infrastructure.persistence.entity.CommentEntity;

import java.util.UUID;

public class CommentMapper {

    public static Comment toDomain(CommentEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getUserId(),
                entity.getMediaId(),
                entity.getContent()
        );
    }

    public static CommentEntity toEntity(Comment domain) {
        CommentEntity entity = new CommentEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setMediaId(domain.getMediaId());
        entity.setContent(domain.getContent());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static Comment toDomain(CommentRequest request, String userId) {
        return new Comment(
                UUID.randomUUID().toString(),
                userId,
                request.getMediaId(),
                request.getContent()
        );
    }

    public static CommentResponse toResponse(Comment domain) {
        CommentResponse response = new CommentResponse();
        response.setId(domain.getId());
        response.setUserId(domain.getUserId());
        response.setMediaId(domain.getMediaId());
        response.setContent(domain.getContent());
        response.setCreatedAt(domain.getCreatedAt());
        response.setUpdatedAt(domain.getUpdatedAt());
        return response;
    }
}
