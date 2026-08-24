package com.mediatheque.media_svc.dto;

import com.mediatheque.media_svc.model.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateMediaRequestTest {



    @Test
    @DisplayName("Le constructeur no-args crée un objet avec tous les champs null")
    void noArgsConstructor_shouldCreateAllNullObject() {
        UpdateMediaRequest request = new UpdateMediaRequest();

        assertThat(request.getTitle()).isNull();
        assertThat(request.getAuthor()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getReleaseYear()).isNull();
        assertThat(request.getGenre()).isNull();
        assertThat(request.getImageUrl()).isNull();
    }


    @Test
    @DisplayName("Les setters modifient correctement tous les champs")
    void setters_shouldUpdateAllFields() {
        UpdateMediaRequest request = new UpdateMediaRequest();

        request.setTitle("Dune");
        request.setAuthor("Frank Herbert");
        request.setDescription("Un roman de SF épique.");
        request.setType(MediaType.BOOK);
        request.setReleaseYear(1965);
        request.setGenre("Science-Fiction");
        request.setImageUrl("https://example.com/dune.jpg");

        assertThat(request.getTitle()).isEqualTo("Dune");
        assertThat(request.getAuthor()).isEqualTo("Frank Herbert");
        assertThat(request.getDescription()).isEqualTo("Un roman de SF épique.");
        assertThat(request.getType()).isEqualTo(MediaType.BOOK);
        assertThat(request.getReleaseYear()).isEqualTo(1965);
        assertThat(request.getGenre()).isEqualTo("Science-Fiction");
        assertThat(request.getImageUrl()).isEqualTo("https://example.com/dune.jpg");
    }

    @Test
    @DisplayName("Les champs peuvent être mis à null via setter (update partiel)")
    void setters_shouldAcceptNullValues() {
        UpdateMediaRequest request = buildSample();

        request.setTitle(null);
        request.setAuthor(null);
        request.setType(null);

        assertThat(request.getTitle()).isNull();
        assertThat(request.getAuthor()).isNull();
        assertThat(request.getType()).isNull();
    }

    @Test
    @DisplayName("Chaque champ peut être mis à jour indépendamment")
    void setters_shouldUpdateFieldsIndependently() {
        UpdateMediaRequest request = buildSample();

        request.setTitle("Nouveau titre");

        assertThat(request.getTitle()).isEqualTo("Nouveau titre");
        assertThat(request.getAuthor()).isEqualTo("Frank Herbert");
        assertThat(request.getType()).isEqualTo(MediaType.BOOK);
    }


    @Test
    @DisplayName("Tous les MediaType peuvent être affectés")
    void allMediaTypes_shouldBeAssignable() {
        for (MediaType type : MediaType.values()) {
            UpdateMediaRequest request = new UpdateMediaRequest();
            request.setType(type);

            assertThat(request.getType()).isEqualTo(type);
        }
    }



    @Test
    @DisplayName("Deux objets identiques sont égaux")
    void equals_shouldReturnTrueForIdenticalObjects() {
        UpdateMediaRequest r1 = buildSample();
        UpdateMediaRequest r2 = buildSample();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux objets différents ne sont pas égaux")
    void equals_shouldReturnFalseForDifferentObjects() {
        UpdateMediaRequest r1 = buildSample();
        UpdateMediaRequest r2 = new UpdateMediaRequest();
        r2.setTitle("1984");
        r2.setAuthor("George Orwell");
        r2.setType(MediaType.FILM);

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    @DisplayName("Un objet est égal à lui-même")
    void equals_shouldReturnTrueForSameInstance() {
        UpdateMediaRequest request = buildSample();

        assertThat(request).isEqualTo(request);
    }

    @Test
    @DisplayName("Un objet n'est pas égal à null")
    void equals_shouldReturnFalseForNull() {
        assertThat(buildSample()).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Deux objets vides sont égaux")
    void equals_twoEmptyObjects_shouldBeEqual() {
        UpdateMediaRequest r1 = new UpdateMediaRequest();
        UpdateMediaRequest r2 = new UpdateMediaRequest();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }


    @Test
    @DisplayName("toString() contient les champs clés")
    void toString_shouldContainKeyFields() {
        UpdateMediaRequest request = buildSample();

        assertThat(request.toString())
                .contains("Dune")
                .contains("Frank Herbert")
                .contains("BOOK");
    }

    @Test
    @DisplayName("toString() d'un objet vide ne lève pas d'exception")
    void toString_emptyObject_shouldNotThrow() {
        assertThat(new UpdateMediaRequest().toString()).isNotNull();
    }


    @Test
    @DisplayName("releaseYear accepte toute valeur entière")
    void releaseYear_shouldAcceptAnyInteger() {
        UpdateMediaRequest request = new UpdateMediaRequest();

        request.setReleaseYear(0);
        assertThat(request.getReleaseYear()).isZero();

        request.setReleaseYear(Integer.MAX_VALUE);
        assertThat(request.getReleaseYear()).isEqualTo(Integer.MAX_VALUE);

        request.setReleaseYear(Integer.MIN_VALUE);
        assertThat(request.getReleaseYear()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Les champs acceptent des chaînes vides")
    void fields_shouldAcceptEmptyStrings() {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("");
        request.setAuthor("");
        request.setDescription("");
        request.setGenre("");
        request.setImageUrl("");

        assertThat(request.getTitle()).isEmpty();
        assertThat(request.getAuthor()).isEmpty();
        assertThat(request.getDescription()).isEmpty();
        assertThat(request.getGenre()).isEmpty();
        assertThat(request.getImageUrl()).isEmpty();
    }

    @Test
    @DisplayName("Un update partiel avec un seul champ est valide")
    void partialUpdate_withSingleField_shouldBeValid() {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Titre mis à jour");

        assertThat(request.getTitle()).isEqualTo("Titre mis à jour");
        assertThat(request.getAuthor()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getReleaseYear()).isNull();
        assertThat(request.getGenre()).isNull();
        assertThat(request.getImageUrl()).isNull();
    }



    private UpdateMediaRequest buildSample() {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Dune");
        request.setAuthor("Frank Herbert");
        request.setDescription("Un roman de SF épique.");
        request.setType(MediaType.BOOK);
        request.setReleaseYear(1965);
        request.setGenre("Science-Fiction");
        request.setImageUrl("https://example.com/dune.jpg");
        return request;
    }
}

