package com.collection.usecase.favorite;

import com.collection.domain.Favorite;
import com.collection.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddFavoriteUseCase {

    private final FavoriteRepository favoriteRepository;

    public AddFavoriteUseCase(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public record Input(String userId, String mediaId) {}

    public Favorite execute(Input input) {
        if (favoriteRepository.existsFavoriteByUserIdAndMediaId(input.userId(), input.mediaId()))
            throw new RuntimeException("Already in favorites");

        Favorite favorite = new Favorite(
                UUID.randomUUID().toString(),
                input.userId(),
                input.mediaId()
        );
        return favoriteRepository.saveFavorite(favorite);
    }
}