package com.mediatheque.media_svc.controller;

import com.mediatheque.media_svc.dto.ExternalMediaResponse;
import com.mediatheque.media_svc.service.ExternalApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
@Tag(name = "External APIs")
public class ExternalApiController {

    private final ExternalApiService externalApiService;



    @GetMapping("/films")
    @Operation(summary = "Rechercher films/séries via TVMaze")
    public ResponseEntity<List<ExternalMediaResponse>> searchFilms(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchFilms(query));
    }

    @GetMapping("/films/youtube")
    @Operation(summary = "Rechercher films complets gratuits via YouTube")
    public ResponseEntity<List<ExternalMediaResponse>> searchYoutubeMovies(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchYoutubeMovies(query));
    }

    @GetMapping("/films/archive")
    @Operation(summary = "Rechercher films domaine public via Archive.org")
    public ResponseEntity<List<ExternalMediaResponse>> searchArchiveMovies(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchArchiveMovies(query));
    }


    @GetMapping("/podcasts")
    @Operation(summary = "Rechercher podcasts via PodcastIndex (fallback iTunes automatique)")
    public ResponseEntity<List<ExternalMediaResponse>> searchPodcasts(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchPodcasts(query));
    }

    @GetMapping("/podcasts/itunes")
    @Operation(summary = "Rechercher podcasts directement via iTunes")
    public ResponseEntity<List<ExternalMediaResponse>> searchPodcastsItunes(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchPodcastsItunes(query));
    }



    @GetMapping("/games")
    @Operation(summary = "Rechercher jeux via RAWG")
    public ResponseEntity<List<ExternalMediaResponse>> searchGames(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchGames(query));
    }

    @GetMapping("/games/free")
    @Operation(summary = "Rechercher jeux F2P via FreeToGame")
    public ResponseEntity<List<ExternalMediaResponse>> searchFreeGames(
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(externalApiService.searchFreeGames(query));
    }



    @GetMapping("/books")
    @Operation(summary = "Rechercher livres via Internet Archive")
    public ResponseEntity<List<ExternalMediaResponse>> searchBooks(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApiService.searchBooks(query));
    }
}
