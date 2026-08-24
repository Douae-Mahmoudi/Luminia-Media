package com.collection.usecase.watchlist;

import com.collection.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveFromWatchlistUseCase {

    private final WatchlistRepository watchlistRepository;

    public RemoveFromWatchlistUseCase(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    @Transactional
    public void execute(String userId, String mediaId) {
        if (!watchlistRepository.existsWatchlistByUserIdAndMediaId(userId, mediaId))
            throw new RuntimeException("Media not found in watchlist");

        watchlistRepository.deleteWatchlistByUserIdAndMediaId(userId, mediaId);
    }
}