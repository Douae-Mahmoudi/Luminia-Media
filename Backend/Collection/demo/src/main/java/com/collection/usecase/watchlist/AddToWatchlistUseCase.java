package com.collection.usecase.watchlist;

import com.collection.domain.Watchlist;
import com.collection.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddToWatchlistUseCase {

    private final WatchlistRepository watchlistRepository;

    public AddToWatchlistUseCase(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public record Input(String userId, String mediaId) {}

    public Watchlist execute(Input input) {
        if (watchlistRepository.existsWatchlistByUserIdAndMediaId(input.userId(), input.mediaId()))
            throw new RuntimeException("Media already in watchlist");

        Watchlist watchlist = new Watchlist(
                UUID.randomUUID().toString(),
                input.userId(),
                input.mediaId()
        );
        return watchlistRepository.saveWatchlist(watchlist);
    }
}