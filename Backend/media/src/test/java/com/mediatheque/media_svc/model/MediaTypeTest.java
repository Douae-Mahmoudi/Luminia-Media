package com.mediatheque.media_svc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

class MediaTypeTest {



    @Test
    @DisplayName("L'enum contient exactement 4 valeurs")
    void enum_shouldHaveExactlyFourValues() {
        assertThat(MediaType.values()).hasSize(4);
    }

    @Test
    @DisplayName("BOOK existe dans l'enum")
    void book_shouldExist() {
        assertThat(MediaType.valueOf("BOOK")).isEqualTo(MediaType.BOOK);
    }

    @Test
    @DisplayName("FILM existe dans l'enum")
    void film_shouldExist() {
        assertThat(MediaType.valueOf("FILM")).isEqualTo(MediaType.FILM);
    }

    @Test
    @DisplayName("GAME existe dans l'enum")
    void game_shouldExist() {
        assertThat(MediaType.valueOf("GAME")).isEqualTo(MediaType.GAME);
    }

    @Test
    @DisplayName("PODCAST existe dans l'enum")
    void podcast_shouldExist() {
        assertThat(MediaType.valueOf("PODCAST")).isEqualTo(MediaType.PODCAST);
    }



    @Test
    @DisplayName("Les valeurs sont dans le bon ordre")
    void enum_shouldBeInCorrectOrder() {
        MediaType[] values = MediaType.values();
        assertThat(values[0]).isEqualTo(MediaType.BOOK);
        assertThat(values[1]).isEqualTo(MediaType.FILM);
        assertThat(values[2]).isEqualTo(MediaType.GAME);
        assertThat(values[3]).isEqualTo(MediaType.PODCAST);
    }

    @Test
    @DisplayName("Les ordinaux sont corrects")
    void enum_shouldHaveCorrectOrdinals() {
        assertThat(MediaType.BOOK.ordinal()).isZero();
        assertThat(MediaType.FILM.ordinal()).isEqualTo(1);
        assertThat(MediaType.GAME.ordinal()).isEqualTo(2);
        assertThat(MediaType.PODCAST.ordinal()).isEqualTo(3);
    }



    @Test
    @DisplayName("name() retourne le nom exact de chaque valeur")
    void name_shouldReturnExactName() {
        assertThat(MediaType.BOOK.name()).isEqualTo("BOOK");
        assertThat(MediaType.FILM.name()).isEqualTo("FILM");
        assertThat(MediaType.GAME.name()).isEqualTo("GAME");
        assertThat(MediaType.PODCAST.name()).isEqualTo("PODCAST");
    }

    @ParameterizedTest
    @EnumSource(MediaType.class)
    @DisplayName("toString() retourne le même résultat que name()")
    void toString_shouldMatchName(MediaType type) {
        assertThat(type.toString()).isEqualTo(type.name());
    }



    @Test
    @DisplayName("valueOf() lève une exception pour une valeur inconnue")
    void valueOf_shouldThrowForUnknownValue() {
        assertThatThrownBy(() -> MediaType.valueOf("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valueOf() est sensible à la casse")
    void valueOf_shouldBeCaseSensitive() {
        assertThatThrownBy(() -> MediaType.valueOf("book"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> MediaType.valueOf("Film"))
                .isInstanceOf(IllegalArgumentException.class);
    }



    @Test
    @DisplayName("Une valeur est égale à elle-même")
    void enum_shouldEqualItself() {
        assertThat(MediaType.BOOK).isEqualTo(MediaType.BOOK);
    }

    @Test
    @DisplayName("Deux valeurs différentes ne sont pas égales")
    void enum_differentValues_shouldNotBeEqual() {
        assertThat(MediaType.BOOK).isNotEqualTo(MediaType.FILM);
        assertThat(MediaType.GAME).isNotEqualTo(MediaType.PODCAST);
    }

    @ParameterizedTest
    @EnumSource(MediaType.class)
    @DisplayName("Chaque valeur est non nulle")
    void eachValue_shouldNotBeNull(MediaType type) {
        assertThat(type).isNotNull();
    }
}
