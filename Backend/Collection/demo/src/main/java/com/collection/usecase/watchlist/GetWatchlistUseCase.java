package com.collection.usecase.watchlist;

import com.collection.domain.Watchlist;
import com.collection.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetWatchlistUseCase {

    private final WatchlistRepository watchlistRepository;

    public GetWatchlistUseCase(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public List<Watchlist> execute(String userId) {
        return watchlistRepository.findWatchlistByUserId(userId);
    }
}