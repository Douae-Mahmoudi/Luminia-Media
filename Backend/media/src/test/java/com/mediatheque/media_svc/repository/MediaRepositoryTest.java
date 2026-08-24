package com.mediatheque.media_svc.repository;

import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MediaRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;


    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();

        mediaRepository.save(Media.builder()
                .title("Dune")
                .author("Frank Herbert")
                .type(MediaType.BOOK)
                .status(MediaStatus.AVAILABLE)
                .genre("Science-Fiction")
                .releaseYear(1965)
                .build());

        mediaRepository.save(Media.builder()
                .title("Inception")
                .author("Christopher Nolan")
                .type(MediaType.FILM)
                .status(MediaStatus.AVAILABLE)
                .genre("Thriller")
                .releaseYear(2010)
                .build());

        mediaRepository.save(Media.builder()
                .title("1984")
                .author("George Orwell")
                .type(MediaType.BOOK)
                .status(MediaStatus.UNAVAILABLE)
                .genre("Dystopie")
                .releaseYear(1949)
                .build());

        mediaRepository.save(Media.builder()
                .title("The Witcher 3")
                .author("CD Projekt")
                .type(MediaType.GAME)
                .status(MediaStatus.AVAILABLE)
                .genre("Science-Fiction")
                .releaseYear(2015)
                .build());

        mediaRepository.save(Media.builder()
                .title("Darknet Diaries")
                .author("Jack Rhysider")
                .type(MediaType.PODCAST)
                .status(MediaStatus.PENDING)
                .genre("Tech")
                .releaseYear(2017)
                .build());
    }



    @Test
    @DisplayName("findByStatus(AVAILABLE) retourne tous les médias disponibles")
    void findByStatus_available_shouldReturnAvailableMedia() {
        List<Media> result = mediaRepository.findByStatus(MediaStatus.AVAILABLE);

        assertThat(result).hasSize(3)
                .extracting(Media::getTitle)
                .containsExactlyInAnyOrder("Dune", "Inception", "The Witcher 3");
    }

    @Test
    @DisplayName("findByStatus(PENDING) retourne uniquement les médias en attente")
    void findByStatus_pending_shouldReturnPendingMedia() {
        List<Media> result = mediaRepository.findByStatus(MediaStatus.PENDING);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("Darknet Diaries");
    }

    @Test
    @DisplayName("findByStatus(REJECTED) retourne une liste vide si aucun média rejeté")
    void findByStatus_rejected_shouldReturnEmptyList() {
        List<Media> result = mediaRepository.findByStatus(MediaStatus.REJECTED);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByTypeAndStatus(BOOK, AVAILABLE) retourne les livres disponibles")
    void findByTypeAndStatus_bookAvailable_shouldReturnCorrectMedia() {
        List<Media> result = mediaRepository.findByTypeAndStatus(MediaType.BOOK, MediaStatus.AVAILABLE);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("Dune");
    }

    @Test
    @DisplayName("findByTypeAndStatus(BOOK, UNAVAILABLE) retourne les livres indisponibles")
    void findByTypeAndStatus_bookUnavailable_shouldReturnCorrectMedia() {
        List<Media> result = mediaRepository.findByTypeAndStatus(MediaType.BOOK, MediaStatus.UNAVAILABLE);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("1984");
    }

    @Test
    @DisplayName("findByTypeAndStatus sans correspondance retourne une liste vide")
    void findByTypeAndStatus_noMatch_shouldReturnEmptyList() {
        List<Media> result = mediaRepository.findByTypeAndStatus(MediaType.PODCAST, MediaStatus.AVAILABLE);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByGenreContainingIgnoreCaseAndStatus retourne les médias dont le genre contient la chaîne")
    void findByGenreContainingIgnoreCaseAndStatus_shouldReturnMatching() {
        List<Media> result = mediaRepository.findByGenreContainingIgnoreCaseAndStatus(
                "science", MediaStatus.AVAILABLE);

        assertThat(result).hasSize(2)
                .extracting(Media::getTitle)
                .containsExactlyInAnyOrder("Dune", "The Witcher 3");
    }

    @Test
    @DisplayName("findByGenreContainingIgnoreCaseAndStatus est insensible à la casse")
    void findByGenreContainingIgnoreCaseAndStatus_shouldBeCaseInsensitive() {
        List<Media> lower = mediaRepository.findByGenreContainingIgnoreCaseAndStatus(
                "thriller", MediaStatus.AVAILABLE);
        List<Media> upper = mediaRepository.findByGenreContainingIgnoreCaseAndStatus(
                "THRILLER", MediaStatus.AVAILABLE);

        assertThat(lower).isEqualTo(upper);
        assertThat(lower).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("Inception");
    }

    @Test
    @DisplayName("findByGenreContainingIgnoreCaseAndStatus sans correspondance retourne liste vide")
    void findByGenreContainingIgnoreCaseAndStatus_noMatch_shouldReturnEmptyList() {
        List<Media> result = mediaRepository.findByGenreContainingIgnoreCaseAndStatus(
                "Horreur", MediaStatus.AVAILABLE);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByReleaseYearAndStatus retourne le média correspondant à l'année et au statut")
    void findByReleaseYearAndStatus_shouldReturnCorrectMedia() {
        List<Media> result = mediaRepository.findByReleaseYearAndStatus(2010, MediaStatus.AVAILABLE);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("Inception");
    }

    @Test
    @DisplayName("findByReleaseYearAndStatus avec mauvais statut retourne liste vide")
    void findByReleaseYearAndStatus_wrongStatus_shouldReturnEmptyList() {
        List<Media> result = mediaRepository.findByReleaseYearAndStatus(1965, MediaStatus.PENDING);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByReleaseYearAndStatus avec année inconnue retourne liste vide")
    void findByReleaseYearAndStatus_unknownYear_shouldReturnEmptyList() {
        List<Media> result = mediaRepository.findByReleaseYearAndStatus(1900, MediaStatus.AVAILABLE);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByTitleContainingIgnoreCaseAndStatus retourne le média dont le titre contient la chaîne")
    void findByTitleContainingIgnoreCaseAndStatus_shouldReturnMatching() {
        List<Media> result = mediaRepository.findByTitleContainingIgnoreCaseAndStatus(
                "dune", MediaStatus.AVAILABLE);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("Dune");
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCaseAndStatus est insensible à la casse")
    void findByTitleContainingIgnoreCaseAndStatus_shouldBeCaseInsensitive() {
        List<Media> lower = mediaRepository.findByTitleContainingIgnoreCaseAndStatus(
                "inception", MediaStatus.AVAILABLE);
        List<Media> upper = mediaRepository.findByTitleContainingIgnoreCaseAndStatus(
                "INCEPTION", MediaStatus.AVAILABLE);

        assertThat(lower).isEqualTo(upper);
        assertThat(lower).hasSize(1);
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCaseAndStatus avec titre partiel retourne les correspondances")
    void findByTitleContainingIgnoreCaseAndStatus_partialTitle_shouldReturnMatching() {
        List<Media> result = mediaRepository.findByTitleContainingIgnoreCaseAndStatus(
                "witcher", MediaStatus.AVAILABLE);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("The Witcher 3");
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCaseAndStatus sans correspondance retourne liste vide")
    void findByTitleContainingIgnoreCaseAndStatus_noMatch_shouldReturnEmptyList() {
        List<Media> result = mediaRepository.findByTitleContainingIgnoreCaseAndStatus(
                "Inexistant", MediaStatus.AVAILABLE);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByType(BOOK) retourne tous les livres peu importe le statut")
    void findByType_book_shouldReturnAllBooks() {
        List<Media> result = mediaRepository.findByType(MediaType.BOOK);

        assertThat(result).hasSize(2)
                .extracting(Media::getTitle)
                .containsExactlyInAnyOrder("Dune", "1984");
    }

    @Test
    @DisplayName("findByType(FILM) retourne uniquement les films")
    void findByType_film_shouldReturnFilms() {
        List<Media> result = mediaRepository.findByType(MediaType.FILM);

        assertThat(result).hasSize(1)
                .extracting(Media::getTitle)
                .containsExactly("Inception");
    }

    @Test
    @DisplayName("findByType sans correspondance retourne liste vide")
    void findByType_noMatch_shouldReturnEmptyList() {
        mediaRepository.deleteAll();

        List<Media> result = mediaRepository.findByType(MediaType.GAME);

        assertThat(result).isEmpty();
    }
}