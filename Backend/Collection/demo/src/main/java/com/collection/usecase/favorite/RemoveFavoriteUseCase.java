package com.collection.usecase.favorite;

import com.collection.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveFavoriteUseCase {

    private final FavoriteRepository favoriteRepository;

    public RemoveFavoriteUseCase(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional  // ← ajoute ça
    public void execute(String userId, String mediaId) {
        if (!favoriteRepository.existsFavoriteByUserIdAndMediaId(userId, mediaId))
            throw new RuntimeException("Favorite not found");

        favoriteRepository.deleteFavoriteByUserIdAndMediaId(userId, mediaId);
    }
}