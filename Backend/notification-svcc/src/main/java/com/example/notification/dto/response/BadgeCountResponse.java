package com.example.notification.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeCountResponse {

    private long unreadCount;
}