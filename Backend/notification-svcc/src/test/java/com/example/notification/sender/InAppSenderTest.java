package com.example.notification.sender;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.entity.NotificationPreference;
import com.example.notification.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class InAppSenderTest {

    private InAppSender inAppSender;

    private Notification notification;
    private NotificationPreference preferenceEnabled;
    private NotificationPreference preferenceDisabled;

    @BeforeEach
    void setUp() {
        inAppSender = new InAppSender();

        notification = Notification.builder()
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .message("Someone liked your media")
                .build();

        preferenceEnabled = NotificationPreference.builder()
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .build();

        preferenceDisabled = NotificationPreference.builder()
                .userId(42L)
                .type(NotificationType.MEDIA_LIKED)
                .inAppEnabled(false)
                .emailEnabled(false)
                .build();
    }



    @Test
    @DisplayName("supports — returns true when inAppEnabled is true")
    void supports_returnsTrue_whenInAppEnabled() {
        assertThat(inAppSender.supports(preferenceEnabled)).isTrue();
    }

    @Test
    @DisplayName("supports — returns false when inAppEnabled is false")
    void supports_returnsFalse_whenInAppDisabled() {
        assertThat(inAppSender.supports(preferenceDisabled)).isFalse();
    }

    @Test
    @DisplayName("supports — ignores emailEnabled flag")
    void supports_ignoresEmailEnabled() {
        NotificationPreference emailOnlyPref = NotificationPreference.builder()
                .userId(42L)
                .type(NotificationType.BROADCAST)
                .inAppEnabled(false)
                .emailEnabled(true)
                .build();

        assertThat(inAppSender.supports(emailOnlyPref)).isFalse();
    }

    @Test
    @DisplayName("supports — returns true when both inApp and email are enabled")
    void supports_returnsTrue_whenBothEnabled() {
        NotificationPreference bothEnabled = NotificationPreference.builder()
                .userId(42L)
                .type(NotificationType.BROADCAST)
                .inAppEnabled(true)
                .emailEnabled(true)
                .build();

        assertThat(inAppSender.supports(bothEnabled)).isTrue();
    }


    @Test
    @DisplayName("send — completes without exception when inApp is enabled")
    void send_doesNotThrow_whenEnabled() {
        assertThatCode(() -> inAppSender.send(notification, preferenceEnabled))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("send — completes without exception when inApp is disabled")
    void send_doesNotThrow_whenDisabled() {
        assertThatCode(() -> inAppSender.send(notification, preferenceDisabled))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("send — when enabled, supports() returns true for the same preference")
    void send_enabled_supportsIsConsistent() {
        inAppSender.send(notification, preferenceEnabled);
        assertThat(inAppSender.supports(preferenceEnabled)).isTrue();
    }

    @Test
    @DisplayName("send — when disabled, supports() returns false for the same preference")
    void send_disabled_supportsIsConsistent() {
        inAppSender.send(notification, preferenceDisabled);
        assertThat(inAppSender.supports(preferenceDisabled)).isFalse();
    }

    @Test
    @DisplayName("send — disabled: calling send does not change the preference state")
    void send_disabled_doesNotMutatePreference() {
        inAppSender.send(notification, preferenceDisabled);
        assertThat(preferenceDisabled.isInAppEnabled()).isFalse();
    }

    @Test
    @DisplayName("send — enabled: calling send does not change the preference state")
    void send_enabled_doesNotMutatePreference() {
        inAppSender.send(notification, preferenceEnabled);
        assertThat(preferenceEnabled.isInAppEnabled()).isTrue();
    }
}

