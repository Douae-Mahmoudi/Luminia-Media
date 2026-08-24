package com.collection.mapper;

import com.collection.domain.Favorite;
import com.collection.dto.request.FavoriteRequest;
import com.collection.dto.response.FavoriteResponse;

import java.util.UUID;

public class FavoriteMapper {

    public static Favorite toDomain(FavoriteRequest request, String userId) {
        return new Favorite(
                UUID.randomUUID().toString(),
                userId,
                request.getMediaId()
        );
    }

    public static FavoriteResponse toResponse(Favorite domain) {
        FavoriteResponse response = new FavoriteResponse();
        response.setId(domain.getId());
        response.setUserId(domain.getUserId());
        response.setMediaId(domain.getMediaId());
        response.setFavoritedAt(domain.getFavoritedAt());
        return response;
    }
}
