package com.mediatheque.media_svc.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalMediaResponseTest {



    @Test
    @DisplayName("Le builder crée un objet avec tous les champs renseignés")
    void builder_shouldCreateWithAllFields() {
        ExternalMediaResponse response = ExternalMediaResponse.builder()
                .title("Dune")
                .author("Frank Herbert")
                .genre("Science-Fiction")
                .releaseYear(1965)
                .description("Un roman de SF épique.")
                .coverUrl("https://example.com/dune.jpg")
                .source("OpenLibrary")
                .externalId("OL123456M")
                .build();

        assertThat(response.getTitle()).isEqualTo("Dune");
        assertThat(response.getAuthor()).isEqualTo("Frank Herbert");
        assertThat(response.getGenre()).isEqualTo("Science-Fiction");
        assertThat(response.getReleaseYear()).isEqualTo(1965);
        assertThat(response.getDescription()).isEqualTo("Un roman de SF épique.");
        assertThat(response.getCoverUrl()).isEqualTo("https://example.com/dune.jpg");
        assertThat(response.getSource()).isEqualTo("OpenLibrary");
        assertThat(response.getExternalId()).isEqualTo("OL123456M");
    }

    @Test
    @DisplayName("Le builder crée un objet avec uniquement certains champs")
    void builder_shouldCreateWithPartialFields() {
        ExternalMediaResponse response = ExternalMediaResponse.builder()
                .title("1984")
                .author("George Orwell")
                .build();

        assertThat(response.getTitle()).isEqualTo("1984");
        assertThat(response.getAuthor()).isEqualTo("George Orwell");
        assertThat(response.getGenre()).isNull();
        assertThat(response.getReleaseYear()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getCoverUrl()).isNull();
        assertThat(response.getSource()).isNull();
        assertThat(response.getExternalId()).isNull();
    }

    @Test
    @DisplayName("Le builder sans aucun champ crée un objet avec tous les champs null")
    void builder_empty_shouldHaveAllNullFields() {
        ExternalMediaResponse response = ExternalMediaResponse.builder().build();

        assertThat(response.getTitle()).isNull();
        assertThat(response.getAuthor()).isNull();
        assertThat(response.getGenre()).isNull();
        assertThat(response.getReleaseYear()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getCoverUrl()).isNull();
        assertThat(response.getSource()).isNull();
        assertThat(response.getExternalId()).isNull();
    }



    @Test
    @DisplayName("Les setters modifient correctement les champs")
    void setters_shouldUpdateFields() {
        ExternalMediaResponse response = ExternalMediaResponse.builder().build();

        response.setTitle("Inception");
        response.setAuthor("Christopher Nolan");
        response.setGenre("Thriller");
        response.setReleaseYear(2010);
        response.setDescription("Un film sur les rêves.");
        response.setCoverUrl("https://example.com/inception.jpg");
        response.setSource("TMDB");
        response.setExternalId("tt1375666");

        assertThat(response.getTitle()).isEqualTo("Inception");
        assertThat(response.getAuthor()).isEqualTo("Christopher Nolan");
        assertThat(response.getGenre()).isEqualTo("Thriller");
        assertThat(response.getReleaseYear()).isEqualTo(2010);
        assertThat(response.getDescription()).isEqualTo("Un film sur les rêves.");
        assertThat(response.getCoverUrl()).isEqualTo("https://example.com/inception.jpg");
        assertThat(response.getSource()).isEqualTo("TMDB");
        assertThat(response.getExternalId()).isEqualTo("tt1375666");
    }


    @Test
    @DisplayName("Deux objets identiques sont égaux")
    void equals_shouldReturnTrueForIdenticalObjects() {
        ExternalMediaResponse r1 = buildSample();
        ExternalMediaResponse r2 = buildSample();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux objets différents ne sont pas égaux")
    void equals_shouldReturnFalseForDifferentObjects() {
        ExternalMediaResponse r1 = buildSample();
        ExternalMediaResponse r2 = ExternalMediaResponse.builder()
                .title("1984")
                .author("George Orwell")
                .source("GoogleBooks")
                .externalId("GB789")
                .build();

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    @DisplayName("Un objet est égal à lui-même")
    void equals_shouldReturnTrueForSameInstance() {
        ExternalMediaResponse response = buildSample();

        assertThat(response).isEqualTo(response);
    }

    @Test
    @DisplayName("Un objet n'est pas égal à null")
    void equals_shouldReturnFalseForNull() {
        ExternalMediaResponse response = buildSample();

        assertThat(response).isNotEqualTo(null);
    }



    @Test
    @DisplayName("toString() contient les champs clés")
    void toString_shouldContainKeyFields() {
        ExternalMediaResponse response = buildSample();

        String str = response.toString();

        assertThat(str)
                .contains("Dune")
                .contains("Frank Herbert")
                .contains("OpenLibrary")
                .contains("OL123456M");
    }

    @Test
    @DisplayName("toString() d'un objet vide ne lève pas d'exception")
    void toString_emptyObject_shouldNotThrow() {
        ExternalMediaResponse response = ExternalMediaResponse.builder().build();

        assertThat(response.toString()).isNotNull();
    }



    @Test
    @DisplayName("releaseYear accepte une valeur négative (pas de contrainte de validation)")
    void releaseYear_shouldAcceptNegativeValue() {
        ExternalMediaResponse response = ExternalMediaResponse.builder()
                .releaseYear(-500)
                .build();

        assertThat(response.getReleaseYear()).isEqualTo(-500);
    }

    @Test
    @DisplayName("Les champs acceptent des chaînes vides")
    void fields_shouldAcceptEmptyStrings() {
        ExternalMediaResponse response = ExternalMediaResponse.builder()
                .title("")
                .author("")
                .genre("")
                .description("")
                .coverUrl("")
                .source("")
                .externalId("")
                .build();

        assertThat(response.getTitle()).isEmpty();
        assertThat(response.getAuthor()).isEmpty();
        assertThat(response.getSource()).isEmpty();
        assertThat(response.getExternalId()).isEmpty();
    }


    private ExternalMediaResponse buildSample() {
        return ExternalMediaResponse.builder()
                .title("Dune")
                .author("Frank Herbert")
                .genre("Science-Fiction")
                .releaseYear(1965)
                .description("Un roman de SF épique.")
                .coverUrl("https://example.com/dune.jpg")
                .source("OpenLibrary")
                .externalId("OL123456M")
                .build();
    }
}
