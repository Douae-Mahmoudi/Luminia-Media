package com.collection.infrastructure.persistence;

import com.collection.infrastructure.persistence.entity.CollectionEntity;
import com.collection.infrastructure.persistence.entity.CollectionEntity.CollectionType;
import com.collection.infrastructure.persistence.entity.CollectionEntity.WatchlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCollectionRepository extends JpaRepository<CollectionEntity, String> {
    List<CollectionEntity> findByUserIdAndType(String userId, CollectionType type);
    List<CollectionEntity> findByUserIdAndIsPublicTrueAndType(String userId, CollectionType type);
    List<CollectionEntity> findByIsPublicTrueAndType(CollectionType type);
    Optional<CollectionEntity> findByUserIdAndMediaIdsContainingAndType(String userId, String mediaId, CollectionType type);
    List<CollectionEntity> findByUserIdAndTypeAndWatchlistStatus(String userId, CollectionType type, WatchlistStatus status);
    boolean existsByUserIdAndMediaIdsContainingAndType(String userId, String mediaId, CollectionType type);
    void deleteByUserIdAndMediaIdsContainingAndType(String userId, String mediaId, CollectionType type);
}