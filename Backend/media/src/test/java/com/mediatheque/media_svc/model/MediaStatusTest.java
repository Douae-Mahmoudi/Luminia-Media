package com.mediatheque.media_svc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

class MediaStatusTest {



    @Test
    @DisplayName("L'enum contient exactement 4 valeurs")
    void enum_shouldHaveExactlyFourValues() {
        assertThat(MediaStatus.values()).hasSize(4);
    }

    @Test
    @DisplayName("PENDING existe dans l'enum")
    void pending_shouldExist() {
        assertThat(MediaStatus.valueOf("PENDING")).isEqualTo(MediaStatus.PENDING);
    }

    @Test
    @DisplayName("AVAILABLE existe dans l'enum")
    void available_shouldExist() {
        assertThat(MediaStatus.valueOf("AVAILABLE")).isEqualTo(MediaStatus.AVAILABLE);
    }

    @Test
    @DisplayName("REJECTED existe dans l'enum")
    void rejected_shouldExist() {
        assertThat(MediaStatus.valueOf("REJECTED")).isEqualTo(MediaStatus.REJECTED);
    }

    @Test
    @DisplayName("UNAVAILABLE existe dans l'enum")
    void unavailable_shouldExist() {
        assertThat(MediaStatus.valueOf("UNAVAILABLE")).isEqualTo(MediaStatus.UNAVAILABLE);
    }



    @Test
    @DisplayName("Les valeurs sont dans le bon ordre")
    void enum_shouldBeInCorrectOrder() {
        MediaStatus[] values = MediaStatus.values();
        assertThat(values[0]).isEqualTo(MediaStatus.PENDING);
        assertThat(values[1]).isEqualTo(MediaStatus.AVAILABLE);
        assertThat(values[2]).isEqualTo(MediaStatus.REJECTED);
        assertThat(values[3]).isEqualTo(MediaStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("Les ordinaux sont corrects")
    void enum_shouldHaveCorrectOrdinals() {
        assertThat(MediaStatus.PENDING.ordinal()).isZero();
        assertThat(MediaStatus.AVAILABLE.ordinal()).isEqualTo(1);
        assertThat(MediaStatus.REJECTED.ordinal()).isEqualTo(2);
        assertThat(MediaStatus.UNAVAILABLE.ordinal()).isEqualTo(3);
    }


    @Test
    @DisplayName("name() retourne le nom exact de chaque valeur")
    void name_shouldReturnExactName() {
        assertThat(MediaStatus.PENDING.name()).isEqualTo("PENDING");
        assertThat(MediaStatus.AVAILABLE.name()).isEqualTo("AVAILABLE");
        assertThat(MediaStatus.REJECTED.name()).isEqualTo("REJECTED");
        assertThat(MediaStatus.UNAVAILABLE.name()).isEqualTo("UNAVAILABLE");
    }

    @ParameterizedTest
    @EnumSource(MediaStatus.class)
    @DisplayName("toString() retourne le même résultat que name()")
    void toString_shouldMatchName(MediaStatus status) {
        assertThat(status.toString()).isEqualTo(status.name());
    }

    @Test
    @DisplayName("valueOf() lève une exception pour une valeur inconnue")
    void valueOf_shouldThrowForUnknownValue() {
        assertThatThrownBy(() -> MediaStatus.valueOf("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valueOf() est sensible à la casse")
    void valueOf_shouldBeCaseSensitive() {
        assertThatThrownBy(() -> MediaStatus.valueOf("available"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> MediaStatus.valueOf("Available"))
                .isInstanceOf(IllegalArgumentException.class);
    }



    @Test
    @DisplayName("Une valeur est égale à elle-même")
    void enum_shouldEqualItself() {
        assertThat(MediaStatus.AVAILABLE).isEqualTo(MediaStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Deux valeurs différentes ne sont pas égales")
    void enum_differentValues_shouldNotBeEqual() {
        assertThat(MediaStatus.PENDING).isNotEqualTo(MediaStatus.REJECTED);
    }

    @ParameterizedTest
    @EnumSource(MediaStatus.class)
    @DisplayName("Chaque valeur est non nulle")
    void eachValue_shouldNotBeNull(MediaStatus status) {
        assertThat(status).isNotNull();
    }
}