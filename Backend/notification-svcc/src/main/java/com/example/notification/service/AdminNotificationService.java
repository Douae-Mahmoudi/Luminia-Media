package com.example.notification.service;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.dto.request.BroadcastRequest;
import com.example.notification.dto.response.AdminNotificationResponse;
import com.example.notification.dto.response.NotificationStatsResponse;
import com.example.notification.dto.response.PageResponse;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.sender.NotificationSender;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> senders;

    @Transactional
    public void broadcast(BroadcastRequest request) {
        List<Long> allUserIds = notificationRepository.findDistinctUserIds();

        if (allUserIds.isEmpty()) {
            log.warn("Broadcast requested but no users found");
            return;
        }

        allUserIds.forEach(userId -> {
            Notification notif = Notification.builder()
                    .userId(userId)
                    .type(NotificationType.BROADCAST)
                    .message(request.getMessage())
                    .referenceType(ReferenceType.SYSTEM)
                    .build();

            Notification saved = notificationRepository.save(notif);

            NotificationPreference defaultPref = NotificationPreference.builder()
                    .userId(userId)
                    .type(NotificationType.BROADCAST)
                    .inAppEnabled(true)
                    .emailEnabled(true)
                    .build();

            senders.forEach(sender -> {
                try {
                    sender.send(saved, defaultPref);
                } catch (Exception e) {
                    log.error("Broadcast sender {} failed for userId={} — {}",
                            sender.getClass().getSimpleName(), userId, e.getMessage());
                }
            });
        });

        log.info("Broadcast sent to {} users", allUserIds.size());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminNotificationResponse> getAllNotifications(
            Long userId,
            NotificationType type,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {

        Specification<Notification> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AdminNotificationResponse> page = notificationRepository
                .findAll(spec, pageable)
                .map(this::toAdminResponse);

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public NotificationStatsResponse getStats() {
        long total  = notificationRepository.count();
        long read   = notificationRepository.countRead();
        long unread = total - read;
        double openRate = total == 0 ? 0.0
                : Math.round((double) read / total * 1000.0) / 10.0;

        Map<String, Long> byType = notificationRepository.countByType()
                .stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));

        return NotificationStatsResponse.builder()
                .totalCount(total)
                .readCount(read)
                .unreadCount(unread)
                .openRatePercent(openRate)
                .countByType(byType)
                .build();
    }

    private AdminNotificationResponse toAdminResponse(Notification n) {
        return AdminNotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .status(n.getStatus())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}