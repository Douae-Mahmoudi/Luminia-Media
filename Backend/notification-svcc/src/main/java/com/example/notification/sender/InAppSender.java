package com.example.notification.sender;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppSender implements NotificationSender {

    @Override
    public void send(Notification notification, NotificationPreference preference) {
        if (!supports(preference)) {
            log.debug("InApp disabled for userId={} type={}",
                    notification.getUserId(), notification.getType());
            return;
        }

        log.info("InApp notification delivered — userId={} type={} message={}",
                notification.getUserId(),
                notification.getType(),
                notification.getMessage());
    }

    @Override
    public boolean supports(NotificationPreference preference) {
        return preference.isInAppEnabled();
    }
}