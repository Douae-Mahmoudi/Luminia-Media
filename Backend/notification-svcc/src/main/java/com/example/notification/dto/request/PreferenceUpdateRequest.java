package com.example.notification.dto.request;

import com.example.notification.domain.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceUpdateRequest {

    @NotNull(message = "Le type de notification est obligatoire")
    private NotificationType type;

    private boolean inAppEnabled;
    private boolean emailEnabled;
}