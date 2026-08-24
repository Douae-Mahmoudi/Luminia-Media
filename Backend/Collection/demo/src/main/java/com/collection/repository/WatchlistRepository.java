package com.collection.repository;

import com.collection.domain.Watchlist;
import com.collection.domain.Watchlist.WatchlistStatus;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository {

    Watchlist saveWatchlist(Watchlist watchlist);

    Optional<Watchlist> findWatchlistByUserIdAndMediaId(String userId, String mediaId);

    List<Watchlist> findWatchlistByUserId(String userId);

    List<Watchlist> findWatchlistByUserIdAndStatus(String userId, WatchlistStatus status);

    boolean existsWatchlistByUserIdAndMediaId(String userId, String mediaId);

    void deleteWatchlistByUserIdAndMediaId(String userId, String mediaId);
}