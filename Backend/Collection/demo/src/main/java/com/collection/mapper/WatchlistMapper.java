package com.collection.mapper;

import com.collection.domain.Watchlist;
import com.collection.dto.request.WatchlistRequest;
import com.collection.dto.response.WatchlistResponse;

import java.util.UUID;

public class WatchlistMapper {

    public static Watchlist toDomain(WatchlistRequest request, String userId) {
        Watchlist watchlist = new Watchlist(
                UUID.randomUUID().toString(),
                userId,
                request.getMediaId()
        );
        if (request.getStatus() != null) {
            watchlist.updateStatus(request.getStatus());
        }
        return watchlist;
    }

    public static WatchlistResponse toResponse(Watchlist domain) {
        WatchlistResponse response = new WatchlistResponse();
        response.setId(domain.getId());
        response.setUserId(domain.getUserId());
        response.setMediaId(domain.getMediaId());
        response.setStatus(domain.getStatus());
        response.setAddedAt(domain.getAddedAt());
        response.setUpdatedAt(domain.getUpdatedAt());
        return response;
    }
}
