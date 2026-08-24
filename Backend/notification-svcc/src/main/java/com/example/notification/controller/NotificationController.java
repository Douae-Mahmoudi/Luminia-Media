package com.example.notification.controller;

import com.example.notification.dto.request.PreferenceUpdateRequest;
import com.example.notification.dto.response.BadgeCountResponse;
import com.example.notification.dto.response.NotificationResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.dto.response.PreferenceResponse;
import com.example.notification.service.NotificationService;
import com.example.notification.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PreferenceService   preferenceService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(userId, pageable));
    }


    @GetMapping("/badge")
    public ResponseEntity<BadgeCountResponse> getBadgeCount(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                notificationService.getBadgeCount(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markOneAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal Long userId) {

        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<List<PreferenceResponse>> getPreferences(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                preferenceService.getMyPreferences(userId));
    }

    @PutMapping("/preferences")
    public ResponseEntity<PreferenceResponse> updatePreference(
            @AuthenticationPrincipal Long userId,
            @RequestBody PreferenceUpdateRequest request) {

        return ResponseEntity.ok(
                preferenceService.updatePreference(userId, request));
    }
}