package com.mediatheque.media_svc.controller;

import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.service.MediaSearchService;
import com.mediatheque.media_svc.service.MediaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media — User")
public class MediaController {

    private final MediaService mediaService;
    private final MediaSearchService mediaSearchService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MediaResponse> submit(
            @RequestHeader(value = "X-User-Id", required = false) Long ownerId,
            @Valid @RequestBody CreateMediaRequest request) {
        if (request.getOwnerId() == null && ownerId != null) {
            request.setOwnerId(ownerId);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.submitMedia(request));
    }

    @GetMapping
    public ResponseEntity<List<MediaResponse>> getAvailable() {
        return ResponseEntity.ok(mediaService.getAvailableMedia());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<MediaResponse>> searchByTitle(
            @RequestParam String title) {
        return ResponseEntity.ok(mediaSearchService.searchByTitle(title));
    }

    @GetMapping("/search/type")
    public ResponseEntity<List<MediaResponse>> searchByType(
            @RequestParam MediaType type) {
        return ResponseEntity.ok(mediaSearchService.searchByType(type));
    }

    @GetMapping("/search/genre")
    public ResponseEntity<List<MediaResponse>> searchByGenre(
            @RequestParam String genre) {
        return ResponseEntity.ok(mediaSearchService.searchByGenre(genre));
    }

    @GetMapping("/search/year")
    public ResponseEntity<List<MediaResponse>> searchByYear(
            @RequestParam Integer year) {
        return ResponseEntity.ok(mediaSearchService.searchByReleaseYear(year));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MediaResponse>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) MediaType type,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer releaseYear) {
        return ResponseEntity.ok(
                mediaSearchService.searchByFilters(title, type, genre, releaseYear));
    }
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MediaResponse>> getMyMedia(
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(mediaService.getMediaByOwner(ownerId));
    }
}