package com.example.notification.controller;

import com.example.notification.domain.enums.NotificationType;
import com.example.notification.dto.request.BroadcastRequest;
import com.example.notification.dto.response.AdminNotificationResponse;
import com.example.notification.dto.response.NotificationStatsResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminNotificationResponse>> getAllNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 30, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                adminNotificationService.getAllNotifications(userId, type, from, to, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsResponse> getStats() {
        return ResponseEntity.ok(adminNotificationService.getStats());
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(
            @RequestBody BroadcastRequest request) {

        adminNotificationService.broadcast(request);
        return ResponseEntity.accepted().build();
    }
}