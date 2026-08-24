package com.example.notification.Integration;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class NotificationTestHelper {

    private final NotificationRepository notificationRepository;

    public NotificationTestHelper(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification saveNotif(Long userId, NotificationStatus status) {
        Notification n = Notification.builder()
                .userId(userId)
                .type(NotificationType.MEDIA_LIKED)
                .message("Test message")
                .referenceId(1L)
                .referenceType(ReferenceType.MEDIA)
                .build();
        Notification saved = notificationRepository.save(n);
        if (status == NotificationStatus.READ) {
            notificationRepository.markAsRead(saved.getId(), userId, LocalDateTime.now());
            notificationRepository.flush();
            return notificationRepository.findById(saved.getId()).orElseThrow();
        }
        return saved;
    }
}
