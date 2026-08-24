package com.example.notification.sender;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;

public interface NotificationSender {
    void send(Notification notification, NotificationPreference preference);
    boolean supports(NotificationPreference preference);
}