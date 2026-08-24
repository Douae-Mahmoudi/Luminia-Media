package com.collection.controller;

import com.collection.domain.Watchlist;
import com.collection.usecase.watchlist.AddToWatchlistUseCase;
import com.collection.usecase.watchlist.GetWatchlistUseCase;
import com.collection.usecase.watchlist.RemoveFromWatchlistUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final AddToWatchlistUseCase    addToWatchlistUseCase;
    private final RemoveFromWatchlistUseCase removeFromWatchlistUseCase;
    private final GetWatchlistUseCase      getWatchlistUseCase;

    public WatchlistController(AddToWatchlistUseCase addToWatchlistUseCase,
                               RemoveFromWatchlistUseCase removeFromWatchlistUseCase,
                               GetWatchlistUseCase getWatchlistUseCase) {
        this.addToWatchlistUseCase    = addToWatchlistUseCase;
        this.removeFromWatchlistUseCase = removeFromWatchlistUseCase;
        this.getWatchlistUseCase      = getWatchlistUseCase;
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<Watchlist>> getWatchlist() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(getWatchlistUseCase.execute(userId));
    }

    @PostMapping("/{mediaId}")
    public ResponseEntity<Watchlist> addToWatchlist(@PathVariable String mediaId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(addToWatchlistUseCase.execute(
                new AddToWatchlistUseCase.Input(userId, mediaId)
        ));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String mediaId) {
        String userId = getCurrentUserId();
        removeFromWatchlistUseCase.execute(userId, mediaId);
        return ResponseEntity.noContent().build();
    }
}