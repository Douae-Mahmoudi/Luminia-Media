package com.example.notification.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ReferenceType Enum Tests")
class ReferenceTypeTest {

    @Test
    @DisplayName("Contient exactement 4 valeurs")
    void shouldHaveExactlyFourValues() {
        assertThat(ReferenceType.values()).hasSize(4);
    }

    @Test
    @DisplayName("Contient MEDIA, COMMENT, COLLECTION, SYSTEM")
    void shouldContainAllValues() {
        assertThat(ReferenceType.values())
                .contains(
                        ReferenceType.MEDIA,
                        ReferenceType.COMMENT,
                        ReferenceType.COLLECTION,
                        ReferenceType.SYSTEM
                );
    }

    @Test
    @DisplayName("valueOf retourne la bonne constante")
    void valueOfShouldReturnCorrectEnum() {
        assertThat(ReferenceType.valueOf("MEDIA")).isEqualTo(ReferenceType.MEDIA);
        assertThat(ReferenceType.valueOf("COMMENT")).isEqualTo(ReferenceType.COMMENT);
        assertThat(ReferenceType.valueOf("COLLECTION")).isEqualTo(ReferenceType.COLLECTION);
        assertThat(ReferenceType.valueOf("SYSTEM")).isEqualTo(ReferenceType.SYSTEM);
    }

    @Test
    @DisplayName("valueOf lève IllegalArgumentException pour valeur inconnue")
    void valueOfShouldThrowForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> ReferenceType.valueOf("ORDER"));
        assertThrows(IllegalArgumentException.class, () -> ReferenceType.valueOf("USER"));
    }

    @Test
    @DisplayName("ordinal() retourne le bon index")
    void ordinalShouldReturnCorrectIndex() {
        assertThat(ReferenceType.MEDIA.ordinal()).isEqualTo(0);
        assertThat(ReferenceType.COMMENT.ordinal()).isEqualTo(1);
        assertThat(ReferenceType.COLLECTION.ordinal()).isEqualTo(2);
        assertThat(ReferenceType.SYSTEM.ordinal()).isEqualTo(3);
    }

    @Test
    @DisplayName("name() retourne le bon nom en String")
    void nameShouldReturnCorrectString() {
        assertThat(ReferenceType.MEDIA.name()).isEqualTo("MEDIA");
        assertThat(ReferenceType.COMMENT.name()).isEqualTo("COMMENT");
        assertThat(ReferenceType.COLLECTION.name()).isEqualTo("COLLECTION");
        assertThat(ReferenceType.SYSTEM.name()).isEqualTo("SYSTEM");
    }
}
