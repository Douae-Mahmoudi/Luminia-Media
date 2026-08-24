package com.collection.repository;

import com.collection.domain.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository {
    Favorite saveFavorite(Favorite favorite);
    Optional<Favorite> findFavoriteByUserIdAndMediaId(String userId, String mediaId);
    List<Favorite> findFavoritesByUserId(String userId);
    boolean existsFavoriteByUserIdAndMediaId(String userId, String mediaId);
    void deleteFavoriteByUserIdAndMediaId(String userId, String mediaId);
}

