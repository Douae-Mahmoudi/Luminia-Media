package com.example.notification.service;

import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationType;
import com.example.notification.dto.request.PreferenceUpdateRequest;
import com.example.notification.dto.response.PreferenceResponse;
import com.example.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Transactional(readOnly = true)
    public List<PreferenceResponse> getMyPreferences(Long userId) {
        List<NotificationPreference> existing = preferenceRepository.findByUserId(userId);

        if (existing.isEmpty()) {
            return Arrays.stream(NotificationType.values())
                    .map(type -> buildDefaultResponse(userId, type))
                    .toList();
        }

        return existing.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PreferenceResponse updatePreference(Long userId, PreferenceUpdateRequest request) {
        NotificationPreference pref = preferenceRepository
                .findByUserIdAndType(userId, request.getType())
                .orElseGet(() -> NotificationPreference.builder()
                        .userId(userId)
                        .type(request.getType())
                        .build());

        pref.setInAppEnabled(request.isInAppEnabled());
        pref.setEmailEnabled(request.isEmailEnabled());

        NotificationPreference saved = preferenceRepository.save(pref);
        log.info("Preference updated — userId={} type={}", userId, request.getType());
        return toResponse(saved);
    }


    private PreferenceResponse toResponse(NotificationPreference p) {
        return PreferenceResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .type(p.getType())
                .inAppEnabled(p.isInAppEnabled())
                .emailEnabled(p.isEmailEnabled())
                .build();
    }

    private PreferenceResponse buildDefaultResponse(Long userId, NotificationType type) {
        return PreferenceResponse.builder()
                .userId(userId)
                .type(type)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();
    }
}