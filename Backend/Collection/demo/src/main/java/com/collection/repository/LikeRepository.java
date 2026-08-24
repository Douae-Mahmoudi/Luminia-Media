package com.collection.repository;

import com.collection.domain.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {

    Like save(Like like);

    Optional<Like> findByUserIdAndMediaId(String userId, String mediaId);

    List<Like> findByUserId(String userId);

    long countByMediaId(String mediaId);

    boolean existsByUserIdAndMediaId(String userId, String mediaId);

    void deleteByUserIdAndMediaId(String userId, String mediaId);
}