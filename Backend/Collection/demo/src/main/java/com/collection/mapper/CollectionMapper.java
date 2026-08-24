package com.collection.mapper;

import com.collection.domain.Collection;
import com.collection.dto.request.CollectionRequest;
import com.collection.dto.response.CollectionResponse;
import com.collection.infrastructure.persistence.entity.CollectionEntity;

import java.util.UUID;

public class CollectionMapper {

    public static Collection toDomain(CollectionEntity entity) {
        Collection collection = new Collection(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getDescription(),
                entity.isPublic()
        );
        if (entity.getMediaIds() != null) {
            entity.getMediaIds().forEach(collection::addMedia);
        }
        return collection;
    }

    public static CollectionEntity toEntity(Collection domain) {
        CollectionEntity entity = new CollectionEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPublic(domain.isPublic());
        entity.setMediaIds(domain.getMediaIds());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static Collection toDomain(CollectionRequest request, String userId) {
        return new Collection(
                UUID.randomUUID().toString(),
                userId,
                request.getName(),
                request.getDescription(),
                request.isPublic()
        );
    }

    public static CollectionResponse toResponse(Collection domain) {
        CollectionResponse response = new CollectionResponse();
        response.setId(domain.getId());
        response.setUserId(domain.getUserId());
        response.setName(domain.getName());
        response.setDescription(domain.getDescription());
        response.setPublic(domain.isPublic());
        response.setMediaIds(domain.getMediaIds());
        response.setCreatedAt(domain.getCreatedAt());
        response.setUpdatedAt(domain.getUpdatedAt());
        return response;
    }
}
