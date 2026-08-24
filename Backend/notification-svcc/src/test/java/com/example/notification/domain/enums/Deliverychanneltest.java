package com.example.notification.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DeliveryChannel Enum Tests")
class DeliveryChannelTest {

    @Test
    @DisplayName("Contient exactement 2 valeurs")
    void shouldHaveExactlyTwoValues() {
        assertThat(DeliveryChannel.values()).hasSize(2);
    }

    @Test
    @DisplayName("Contient IN_APP et EMAIL")
    void shouldContainInAppAndEmail() {
        assertThat(DeliveryChannel.values())
                .contains(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL);
    }

    @Test
    @DisplayName("valueOf retourne la bonne constante")
    void valueOfShouldReturnCorrectEnum() {
        assertThat(DeliveryChannel.valueOf("IN_APP")).isEqualTo(DeliveryChannel.IN_APP);
        assertThat(DeliveryChannel.valueOf("EMAIL")).isEqualTo(DeliveryChannel.EMAIL);
    }

    @Test
    @DisplayName("valueOf lève IllegalArgumentException pour valeur inconnue")
    void valueOfShouldThrowForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> DeliveryChannel.valueOf("SMS"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryChannel.valueOf("PUSH"));
    }

    @Test
    @DisplayName("ordinal() — IN_APP=0 avant EMAIL=1")
    void ordinalShouldShowInAppBeforeEmail() {
        assertThat(DeliveryChannel.IN_APP.ordinal()).isEqualTo(0);
        assertThat(DeliveryChannel.EMAIL.ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("name() retourne le bon nom en String")
    void nameShouldReturnCorrectString() {
        assertThat(DeliveryChannel.IN_APP.name()).isEqualTo("IN_APP");
        assertThat(DeliveryChannel.EMAIL.name()).isEqualTo("EMAIL");
    }
}