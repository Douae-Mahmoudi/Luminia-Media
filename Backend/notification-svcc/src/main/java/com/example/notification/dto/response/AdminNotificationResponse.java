package com.example.notification.dto.response;

import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private NotificationStatus status;
    private String message;
    private Long referenceId;
    private ReferenceType referenceType;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}