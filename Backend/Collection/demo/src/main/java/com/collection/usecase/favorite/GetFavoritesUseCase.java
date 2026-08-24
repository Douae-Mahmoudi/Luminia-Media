package com.collection.usecase.favorite;

import com.collection.domain.Favorite;
import com.collection.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class GetFavoritesUseCase {

    private final FavoriteRepository favoriteRepository;

    public GetFavoritesUseCase(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Favorite> execute(String userId) {
        return favoriteRepository.findFavoritesByUserId(userId);
    }
}
