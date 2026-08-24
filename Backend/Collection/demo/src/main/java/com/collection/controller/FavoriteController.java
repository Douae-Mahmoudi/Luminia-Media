package com.collection.controller;

import com.collection.domain.Favorite;
import com.collection.usecase.favorite.AddFavoriteUseCase;
import com.collection.usecase.favorite.GetFavoritesUseCase;
import com.collection.usecase.favorite.RemoveFavoriteUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final AddFavoriteUseCase    addFavoriteUseCase;
    private final RemoveFavoriteUseCase removeFavoriteUseCase;
    private final GetFavoritesUseCase   getFavoritesUseCase;

    public FavoriteController(AddFavoriteUseCase addFavoriteUseCase,
                              RemoveFavoriteUseCase removeFavoriteUseCase,
                              GetFavoritesUseCase getFavoritesUseCase) {
        this.addFavoriteUseCase    = addFavoriteUseCase;
        this.removeFavoriteUseCase = removeFavoriteUseCase;
        this.getFavoritesUseCase   = getFavoritesUseCase;
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<Favorite>> getFavorites() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(getFavoritesUseCase.execute(userId));
    }

    @PostMapping("/{mediaId}")
    public ResponseEntity<Favorite> addFavorite(@PathVariable String mediaId) {
        String userId = getCurrentUserId();
        Favorite favorite = addFavoriteUseCase.execute(
                new AddFavoriteUseCase.Input(userId, mediaId)
        );
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable String mediaId) {
        String userId = getCurrentUserId();
        removeFavoriteUseCase.execute(userId, mediaId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/external")
    public ResponseEntity<List<Favorite>> getExternalFavorites() {
        String userId = getCurrentUserId();
        List<Favorite> externals = getFavoritesUseCase.execute(userId)
                .stream()
                .filter(f -> f.getMediaId().contains("-"))
                .toList();
        return ResponseEntity.ok(externals);
    }

    @PostMapping("/external/{externalKey}")
    public ResponseEntity<Favorite> addExternalFavorite(@PathVariable String externalKey) {
        String userId = getCurrentUserId();
        Favorite favorite = addFavoriteUseCase.execute(
                new AddFavoriteUseCase.Input(userId, externalKey)
        );
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/external/{externalKey}")
    public ResponseEntity<Void> removeExternalFavorite(@PathVariable String externalKey) {
        String userId = getCurrentUserId();
        removeFavoriteUseCase.execute(userId, externalKey);
        return ResponseEntity.noContent().build();
    }
}