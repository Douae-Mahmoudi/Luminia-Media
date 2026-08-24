package com.example.notification.service;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.response.BadgeCountResponse;
import com.example.notification.dto.response.NotificationResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.repository.NotificationPreferenceRepository;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final List<NotificationSender> senders;

    @Transactional
    public void send(Long userId, NotificationType type,
                     String message, Long referenceId, ReferenceType referenceType) {

        NotificationPreference pref = preferenceRepository
                .findByUserIdAndType(userId, type)
                .orElseGet(() -> buildDefaultPreference(userId, type));

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification saved — userId={} type={}", userId, type);

        senders.forEach(sender -> {
            try {
                sender.send(saved, pref);
            } catch (Exception e) {
                log.error("Sender {} failed — {}",
                        sender.getClass().getSimpleName(), e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public BadgeCountResponse getBadgeCount(Long userId) {
        long count = notificationRepository
                .countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        return BadgeCountResponse.builder()
                .unreadCount(count)
                .build();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(
                notificationId, userId, LocalDateTime.now());
        if (updated > 0) {
            log.info("Notification marked as read — id={}", notificationId);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read — userId={}", updated, userId);
    }


    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .status(n.getStatus())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .read(n.getStatus() == NotificationStatus.READ)
                .build();
    }

    private NotificationPreference buildDefaultPreference(Long userId, NotificationType type) {
        return preferenceRepository.save(NotificationPreference.builder()
                .userId(userId)
                .type(type)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build());
    }
}