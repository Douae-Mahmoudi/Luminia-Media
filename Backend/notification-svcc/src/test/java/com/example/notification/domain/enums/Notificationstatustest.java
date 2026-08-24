package com.example.notification.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("NotificationStatus Enum Tests")
class NotificationStatusTest {

    @Test
    @DisplayName("Contient exactement 2 valeurs")
    void shouldHaveExactlyTwoValues() {
        assertThat(NotificationStatus.values()).hasSize(2);
    }

    @Test
    @DisplayName("Contient UNREAD et READ")
    void shouldContainUnreadAndRead() {
        assertThat(NotificationStatus.values())
                .contains(NotificationStatus.UNREAD, NotificationStatus.READ);
    }

    @Test
    @DisplayName("valueOf retourne la bonne constante")
    void valueOfShouldReturnCorrectEnum() {
        assertThat(NotificationStatus.valueOf("UNREAD")).isEqualTo(NotificationStatus.UNREAD);
        assertThat(NotificationStatus.valueOf("READ")).isEqualTo(NotificationStatus.READ);
    }

    @Test
    @DisplayName("valueOf lève IllegalArgumentException pour valeur inconnue")
    void valueOfShouldThrowForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> NotificationStatus.valueOf("PENDING"));
        assertThrows(IllegalArgumentException.class, () -> NotificationStatus.valueOf("DELETED"));
    }

    @Test
    @DisplayName("ordinal() — UNREAD=0 avant READ=1")
    void ordinalShouldShowUnreadBeforeRead() {
        assertThat(NotificationStatus.UNREAD.ordinal()).isEqualTo(0);
        assertThat(NotificationStatus.READ.ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("name() retourne le bon nom en String")
    void nameShouldReturnCorrectString() {
        assertThat(NotificationStatus.UNREAD.name()).isEqualTo("UNREAD");
        assertThat(NotificationStatus.READ.name()).isEqualTo("READ");
    }
}