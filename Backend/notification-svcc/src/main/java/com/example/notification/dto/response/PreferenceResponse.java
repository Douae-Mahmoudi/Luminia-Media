package com.example.notification.dto.response;

import com.example.notification.domain.enums.NotificationType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private boolean inAppEnabled;
    private boolean emailEnabled;
}