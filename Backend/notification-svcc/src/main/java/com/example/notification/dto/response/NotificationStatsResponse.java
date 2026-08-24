package com.example.notification.dto.response;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsResponse {

    private long totalCount;
    private long readCount;
    private long unreadCount;
    private double openRatePercent;
    private Map<String, Long> countByType;
}