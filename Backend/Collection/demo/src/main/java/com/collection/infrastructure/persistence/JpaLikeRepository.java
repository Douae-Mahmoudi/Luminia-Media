package com.collection.infrastructure.persistence;

import com.collection.infrastructure.persistence.entity.LikeEntity;
import com.collection.infrastructure.persistence.entity.LikeEntity.LikeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaLikeRepository extends JpaRepository<LikeEntity, String> {
    Optional<LikeEntity> findByUserIdAndMediaIdAndType(String userId, String mediaId, LikeType type);
    List<LikeEntity> findByUserIdAndType(String userId, LikeType type);
    long countByMediaIdAndType(String mediaId, LikeType type);
    boolean existsByUserIdAndMediaIdAndType(String userId, String mediaId, LikeType type);
    void deleteByUserIdAndMediaIdAndType(String userId, String mediaId, LikeType type);
}