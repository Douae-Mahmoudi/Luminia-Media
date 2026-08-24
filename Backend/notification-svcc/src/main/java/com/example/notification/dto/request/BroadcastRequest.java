package com.example.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastRequest {

    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 500, message = "Le message ne peut pas dépasser 500 caractères")
    private String message;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String title;
}