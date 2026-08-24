package com.mediatheque.media_svc.service;

import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaSearchServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaSearchService mediaSearchService;



    private Media duneBook;
    private Media inceptionFilm;
    private Media witcherGame;
    private MediaResponse duneResponse;
    private MediaResponse inceptionResponse;
    private MediaResponse witcherResponse;

    @BeforeEach
    void setUp() {
        duneBook = Media.builder()
                .id(1L).title("Dune").author("Frank Herbert")
                .type(MediaType.BOOK).status(MediaStatus.AVAILABLE)
                .genre("Science-Fiction").releaseYear(1965).build();

        inceptionFilm = Media.builder()
                .id(2L).title("Inception").author("Christopher Nolan")
                .type(MediaType.FILM).status(MediaStatus.AVAILABLE)
                .genre("Thriller").releaseYear(2010).build();

        witcherGame = Media.builder()
                .id(3L).title("The Witcher 3").author("CD Projekt")
                .type(MediaType.GAME).status(MediaStatus.AVAILABLE)
                .genre("RPG").releaseYear(2015).build();

        duneResponse     = MediaResponse.builder().id(1L).title("Dune").type(MediaType.BOOK).build();
        inceptionResponse = MediaResponse.builder().id(2L).title("Inception").type(MediaType.FILM).build();
        witcherResponse  = MediaResponse.builder().id(3L).title("The Witcher 3").type(MediaType.GAME).build();
    }



    @Test
    @DisplayName("searchByTitle : retourne les médias correspondant au titre")
    void searchByTitle_shouldReturnMatchingMedia() {
        when(mediaRepository.findByTitleContainingIgnoreCaseAndStatus("Dune", MediaStatus.AVAILABLE))
                .thenReturn(List.of(duneBook));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);

        List<MediaResponse> results = mediaSearchService.searchByTitle("Dune");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Dune");
        verify(mediaRepository).findByTitleContainingIgnoreCaseAndStatus("Dune", MediaStatus.AVAILABLE);
        verify(mediaService).mapToResponse(duneBook);
    }

    @Test
    @DisplayName("searchByTitle : retourne une liste vide si aucun résultat")
    void searchByTitle_noMatch_shouldReturnEmptyList() {
        when(mediaRepository.findByTitleContainingIgnoreCaseAndStatus("Inexistant", MediaStatus.AVAILABLE))
                .thenReturn(List.of());

        List<MediaResponse> results = mediaSearchService.searchByTitle("Inexistant");

        assertThat(results).isEmpty();
        verify(mediaService, never()).mapToResponse(any());
    }

    @Test
    @DisplayName("searchByTitle : mappe chaque résultat via mapToResponse")
    void searchByTitle_shouldMapEachResult() {
        when(mediaRepository.findByTitleContainingIgnoreCaseAndStatus("i", MediaStatus.AVAILABLE))
                .thenReturn(List.of(duneBook, inceptionFilm));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);

        List<MediaResponse> results = mediaSearchService.searchByTitle("i");

        assertThat(results).hasSize(2);
        verify(mediaService, times(2)).mapToResponse(any());
    }

    @Test
    @DisplayName("searchByType : retourne les médias du type demandé")
    void searchByType_shouldReturnMatchingMedia() {
        when(mediaRepository.findByTypeAndStatus(MediaType.BOOK, MediaStatus.AVAILABLE))
                .thenReturn(List.of(duneBook));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);

        List<MediaResponse> results = mediaSearchService.searchByType(MediaType.BOOK);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(MediaType.BOOK);
        verify(mediaRepository).findByTypeAndStatus(MediaType.BOOK, MediaStatus.AVAILABLE);
    }

    @Test
    @DisplayName("searchByType : retourne une liste vide si aucun résultat")
    void searchByType_noMatch_shouldReturnEmptyList() {
        when(mediaRepository.findByTypeAndStatus(MediaType.PODCAST, MediaStatus.AVAILABLE))
                .thenReturn(List.of());

        List<MediaResponse> results = mediaSearchService.searchByType(MediaType.PODCAST);

        assertThat(results).isEmpty();
        verify(mediaService, never()).mapToResponse(any());
    }

    @Test
    @DisplayName("searchByType : mappe chaque résultat via mapToResponse")
    void searchByType_shouldMapEachResult() {
        when(mediaRepository.findByTypeAndStatus(MediaType.FILM, MediaStatus.AVAILABLE))
                .thenReturn(List.of(inceptionFilm));
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);

        List<MediaResponse> results = mediaSearchService.searchByType(MediaType.FILM);

        assertThat(results).hasSize(1);
        verify(mediaService).mapToResponse(inceptionFilm);
    }


    @Test
    @DisplayName("searchByGenre : retourne les médias du genre demandé")
    void searchByGenre_shouldReturnMatchingMedia() {
        when(mediaRepository.findByGenreContainingIgnoreCaseAndStatus("Science-Fiction", MediaStatus.AVAILABLE))
                .thenReturn(List.of(duneBook));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);

        List<MediaResponse> results = mediaSearchService.searchByGenre("Science-Fiction");

        assertThat(results).hasSize(1);
        verify(mediaRepository).findByGenreContainingIgnoreCaseAndStatus("Science-Fiction", MediaStatus.AVAILABLE);
    }

    @Test
    @DisplayName("searchByGenre : retourne une liste vide si aucun résultat")
    void searchByGenre_noMatch_shouldReturnEmptyList() {
        when(mediaRepository.findByGenreContainingIgnoreCaseAndStatus("Horreur", MediaStatus.AVAILABLE))
                .thenReturn(List.of());

        List<MediaResponse> results = mediaSearchService.searchByGenre("Horreur");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("searchByReleaseYear : retourne les médias de l'année demandée")
    void searchByReleaseYear_shouldReturnMatchingMedia() {
        when(mediaRepository.findByReleaseYearAndStatus(2010, MediaStatus.AVAILABLE))
                .thenReturn(List.of(inceptionFilm));
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);

        List<MediaResponse> results = mediaSearchService.searchByReleaseYear(2010);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Inception");
        verify(mediaRepository).findByReleaseYearAndStatus(2010, MediaStatus.AVAILABLE);
    }

    @Test
    @DisplayName("searchByReleaseYear : retourne une liste vide si aucun résultat")
    void searchByReleaseYear_noMatch_shouldReturnEmptyList() {
        when(mediaRepository.findByReleaseYearAndStatus(1900, MediaStatus.AVAILABLE))
                .thenReturn(List.of());

        List<MediaResponse> results = mediaSearchService.searchByReleaseYear(1900);

        assertThat(results).isEmpty();
    }


    @Test
    @DisplayName("searchByFilters : tous les filtres null retourne tous les médias AVAILABLE")
    void searchByFilters_allNullFilters_shouldReturnAllAvailable() {
        Media unavailable = Media.builder()
                .id(99L).title("Rejeté").type(MediaType.BOOK)
                .status(MediaStatus.UNAVAILABLE).build();

        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm, witcherGame, unavailable));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);
        when(mediaService.mapToResponse(witcherGame)).thenReturn(witcherResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, null, null);

        assertThat(results).hasSize(3);
        verify(mediaService, times(3)).mapToResponse(any());
    }

    @Test
    @DisplayName("searchByFilters : filtre par titre")
    void searchByFilters_withTitle_shouldFilterByTitle() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm, witcherGame));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters("dune", null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Dune");
    }

    @Test
    @DisplayName("searchByFilters : filtre par titre est insensible à la casse")
    void searchByFilters_titleFilter_shouldBeCaseInsensitive() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm));
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters("INCEPTION", null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("searchByFilters : filtre par type")
    void searchByFilters_withType_shouldFilterByType() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm, witcherGame));
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, MediaType.FILM, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(MediaType.FILM);
    }

    @Test
    @DisplayName("searchByFilters : filtre par genre")
    void searchByFilters_withGenre_shouldFilterByGenre() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm, witcherGame));
        when(mediaService.mapToResponse(witcherGame)).thenReturn(witcherResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, "RPG", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("The Witcher 3");
    }

    @Test
    @DisplayName("searchByFilters : filtre par genre est insensible à la casse")
    void searchByFilters_genreFilter_shouldBeCaseInsensitive() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, "science-fiction", null);

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("searchByFilters : filtre par année de sortie")
    void searchByFilters_withReleaseYear_shouldFilterByYear() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm, witcherGame));
        when(mediaService.mapToResponse(duneBook)).thenReturn(duneResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, null, 1965);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Dune");
    }

    @Test
    @DisplayName("searchByFilters : combinaison de filtres titre + type")
    void searchByFilters_combinedTitleAndType_shouldApplyBothFilters() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook, inceptionFilm, witcherGame));
        when(mediaService.mapToResponse(inceptionFilm)).thenReturn(inceptionResponse);

        List<MediaResponse> results = mediaSearchService.searchByFilters("inception", MediaType.FILM, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("searchByFilters : exclut les médias non AVAILABLE")
    void searchByFilters_shouldExcludeNonAvailableMedia() {
        Media pending = Media.builder()
                .id(10L).title("En attente").type(MediaType.BOOK)
                .status(MediaStatus.PENDING).genre("SF").releaseYear(2020).build();
        Media rejected = Media.builder()
                .id(11L).title("Rejeté").type(MediaType.BOOK)
                .status(MediaStatus.REJECTED).genre("SF").releaseYear(2020).build();

        when(mediaRepository.findAll()).thenReturn(List.of(pending, rejected));

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, null, null);

        assertThat(results).isEmpty();
        verify(mediaService, never()).mapToResponse(any());
    }

    @Test
    @DisplayName("searchByFilters : média avec genre null n'est pas retourné si filtre genre présent")
    void searchByFilters_mediaWithNullGenre_shouldBeExcludedWhenGenreFilterSet() {
        Media bookNoGenre = Media.builder()
                .id(5L).title("Sans Genre").type(MediaType.BOOK)
                .status(MediaStatus.AVAILABLE).genre(null).build();

        when(mediaRepository.findAll()).thenReturn(List.of(bookNoGenre));

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, "SF", null);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("searchByFilters : aucun résultat retourne liste vide")
    void searchByFilters_noMatch_shouldReturnEmptyList() {
        when(mediaRepository.findAll()).thenReturn(List.of(duneBook));

        List<MediaResponse> results = mediaSearchService.searchByFilters("Inexistant", null, null, null);

        assertThat(results).isEmpty();
        verify(mediaService, never()).mapToResponse(any());
    }

    @Test
    @DisplayName("searchByFilters : repository vide retourne liste vide")
    void searchByFilters_emptyRepository_shouldReturnEmptyList() {
        when(mediaRepository.findAll()).thenReturn(List.of());

        List<MediaResponse> results = mediaSearchService.searchByFilters(null, null, null, null);

        assertThat(results).isEmpty();
    }
}