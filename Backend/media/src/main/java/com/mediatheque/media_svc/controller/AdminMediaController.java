package com.mediatheque.media_svc.controller;

import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.dto.UpdateMediaRequest;
import com.mediatheque.media_svc.service.MediaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Media — Admin")
public class AdminMediaController {

    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<List<MediaResponse>> getAll() {
        return ResponseEntity.ok(mediaService.getAllMedia());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MediaResponse>> getPending() {
        return ResponseEntity.ok(mediaService.getPendingMedia());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<MediaResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.approveMedia(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<MediaResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.rejectMedia(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<MediaResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateMediaRequest request) {
        return ResponseEntity.ok(mediaService.updateMedia(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
