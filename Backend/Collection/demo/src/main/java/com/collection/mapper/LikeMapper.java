package com.collection.mapper;

import com.collection.domain.Like;
import com.collection.dto.request.LikeRequest;
import com.collection.dto.response.LikeResponse;
import com.collection.infrastructure.persistence.entity.LikeEntity;

import java.util.UUID;

public class LikeMapper {

    public static Like toDomain(LikeEntity entity) {
        return new Like(
                entity.getId(),
                entity.getUserId(),
                entity.getMediaId()
        );
    }

    public static LikeEntity toEntity(Like domain) {
        LikeEntity entity = new LikeEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setMediaId(domain.getMediaId());
        entity.setCreatedAt(domain.getLikedAt());
        entity.setType(LikeEntity.LikeType.LIKE);
        return entity;
    }

    public static Like toDomain(LikeRequest request, String userId) {
        return new Like(
                UUID.randomUUID().toString(),
                userId,
                request.getMediaId()
        );
    }

    public static LikeResponse toResponse(Like domain) {
        LikeResponse response = new LikeResponse();
        response.setId(domain.getId());
        response.setUserId(domain.getUserId());
        response.setMediaId(domain.getMediaId());
        response.setLikedAt(domain.getLikedAt());
        return response;
    }
}
