package com.mediatheque.media_svc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatheque.media_svc.dto.ExternalMediaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExternalApiService externalApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(externalApiService, "tvmazeBaseUrl",         "https://api.tvmaze.com");
        ReflectionTestUtils.setField(externalApiService, "youtubeBaseUrl",         "https://www.googleapis.com/youtube/v3");
        ReflectionTestUtils.setField(externalApiService, "youtubeApiKey",          "youtube-key");
        ReflectionTestUtils.setField(externalApiService, "archiveBaseUrl",         "https://archive.org");
        ReflectionTestUtils.setField(externalApiService, "podcastIndexBaseUrl",    "https://api.podcastindex.org/api/1.0");
        ReflectionTestUtils.setField(externalApiService, "podcastIndexApiKey",     "podcast-key");
        ReflectionTestUtils.setField(externalApiService, "podcastIndexApiSecret",  "podcast-secret");
        ReflectionTestUtils.setField(externalApiService, "itunesBaseUrl",          "https://itunes.apple.com");
        ReflectionTestUtils.setField(externalApiService, "freeToGameBaseUrl",      "https://www.freetogame.com/api");
        ReflectionTestUtils.setField(externalApiService, "rawgBaseUrl",            "https://api.rawg.io/api");
        ReflectionTestUtils.setField(externalApiService, "rawgApiKey",             "rawg-key");
        ReflectionTestUtils.setField(externalApiService, "internetArchiveBaseUrl", "https://archive.org");
    }


    @Test
    @DisplayName("searchBooks : retourne une liste mappée correctement")
    void searchBooks_shouldReturnMappedResults() {
        Map<String, Object> response = Map.of(
                "response", Map.of(
                        "docs", List.of(
                                Map.of(
                                        "identifier",   "dune_frank_herbert",
                                        "title",        "Dune",
                                        "creator",      "Frank Herbert",
                                        "subject",      "Science-Fiction",
                                        "description",  "Un roman de SF.",
                                        "year",         "1965"
                                )
                        )
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchBooks("Dune");

        assertThat(results).hasSize(1);
        ExternalMediaResponse result = results.get(0);
        assertThat(result.getTitle()).isEqualTo("Dune");
        assertThat(result.getAuthor()).isEqualTo("Frank Herbert");
        assertThat(result.getGenre()).isEqualTo("Science-Fiction");
        assertThat(result.getReleaseYear()).isEqualTo(1965);
        assertThat(result.getDescription()).isEqualTo("Un roman de SF.");
        assertThat(result.getCoverUrl()).isEqualTo("https://archive.org/services/img/dune_frank_herbert");
        assertThat(result.getReadUrl()).isEqualTo("https://archive.org/details/dune_frank_herbert");
        assertThat(result.getSource()).isEqualTo("InternetArchive");
        assertThat(result.getExternalId()).isEqualTo("dune_frank_herbert");
    }

    @Test
    @DisplayName("searchBooks : creator en liste → premier élément extrait")
    void searchBooks_creatorAsList_shouldReturnFirstElement() {
        Map<String, Object> response = Map.of(
                "response", Map.of(
                        "docs", List.of(
                                Map.of(
                                        "identifier", "ol001",
                                        "title",      "Test",
                                        "creator",    List.of("Auteur A", "Auteur B")
                                )
                        )
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchBooks("Test");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo("Auteur A");
    }

    @Test
    @DisplayName("searchBooks : year entier → releaseYear correct")
    void searchBooks_integerYear_shouldReturnCorrectYear() {
        Map<String, Object> response = Map.of(
                "response", Map.of(
                        "docs", List.of(
                                Map.of("identifier", "ol002", "title", "Test", "year", 2001)
                        )
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchBooks("Test");

        assertThat(results.get(0).getReleaseYear()).isEqualTo(2001);
    }

    @Test
    @DisplayName("searchBooks : réponse null → liste vide")
    void searchBooks_nullResponse_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThat(externalApiService.searchBooks("query")).isEmpty();
    }

    @Test
    @DisplayName("searchBooks : réponse sans 'response' → liste vide")
    void searchBooks_noResponseKey_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of());

        assertThat(externalApiService.searchBooks("query")).isEmpty();
    }

    @Test
    @DisplayName("searchBooks : docs vide → liste vide")
    void searchBooks_emptyDocs_shouldReturnEmptyList() {
        Map<String, Object> response = Map.of("response", Map.of("docs", List.of()));
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThat(externalApiService.searchBooks("query")).isEmpty();
    }

    @Test
    @DisplayName("searchBooks : titre null → item ignoré")
    void searchBooks_nullTitle_shouldSkipItem() {
        Map<String, Object> response = Map.of(
                "response", Map.of(
                        "docs", List.of(Map.of("identifier", "ol000"))
                )
        );
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThat(externalApiService.searchBooks("query")).isEmpty();
    }


    @Test
    @DisplayName("searchFilms : retourne une liste mappée correctement")
    void searchFilms_shouldReturnMappedResults() {
        List<Map> response = List.of(
                Map.of(
                        "show", Map.of(
                                "id",        27205,
                                "name",      "Inception",
                                "summary",   "<p>Un film sur les rêves.</p>",
                                "premiered", "2010-07-16",
                                "type",      "Scripted",
                                "image",     Map.of("medium", "https://img.tvmaze.com/inception.jpg")
                        )
                )
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchFilms("Inception");

        assertThat(results).hasSize(1);
        ExternalMediaResponse result = results.get(0);
        assertThat(result.getTitle()).isEqualTo("Inception");
        assertThat(result.getDescription()).isEqualTo("Un film sur les rêves.");
        assertThat(result.getReleaseYear()).isEqualTo(2010);
        assertThat(result.getGenre()).isEqualTo("Scripted");
        assertThat(result.getCoverUrl()).isEqualTo("https://img.tvmaze.com/inception.jpg");
        assertThat(result.getReadUrl()).isEqualTo("https://www.tvmaze.com/shows/27205");
        assertThat(result.getSource()).isEqualTo("TVMaze");
        assertThat(result.getExternalId()).isEqualTo("27205");
    }

    @Test
    @DisplayName("searchFilms : image null → coverUrl null")
    void searchFilms_nullImage_shouldReturnNullCoverUrl() {
        List<Map> response = List.of(
                Map.of("show", Map.of("id", 1, "name", "Film sans image"))
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchFilms("Film sans image");

        assertThat(results.get(0).getCoverUrl()).isNull();
    }

    @Test
    @DisplayName("searchFilms : premiered invalide → releaseYear null")
    void searchFilms_invalidPremiered_shouldReturnNullYear() {
        List<Map> response = List.of(
                Map.of("show", Map.of("id", 2, "name", "Film", "premiered", "N/A"))
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchFilms("Film");

        assertThat(results.get(0).getReleaseYear()).isNull();
    }

    @Test
    @DisplayName("searchFilms : réponse null → liste vide")
    void searchFilms_nullResponse_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(null);

        assertThat(externalApiService.searchFilms("query")).isEmpty();
    }

    @Test
    @DisplayName("searchFilms : item sans 'show' → ignoré")
    void searchFilms_missingShow_shouldSkipItem() {
        List<Map> response = List.of(Map.of());
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(response);

        assertThat(externalApiService.searchFilms("query")).isEmpty();
    }

    @Test
    @DisplayName("searchGames : retourne une liste mappée correctement")
    void searchGames_shouldReturnMappedResults() {
        Map<String, Object> response = Map.of(
                "results", List.of(
                        Map.of(
                                "id",               3498,
                                "name",             "The Witcher 3",
                                "slug",             "the-witcher-3",
                                "released",         "2015-05-18",
                                "background_image", "https://img.rawg.io/witcher.jpg",
                                "genres",           List.of(Map.of("name", "RPG"))
                        )
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchGames("Witcher");

        assertThat(results).hasSize(1);
        ExternalMediaResponse result = results.get(0);
        assertThat(result.getTitle()).isEqualTo("The Witcher 3");
        assertThat(result.getReleaseYear()).isEqualTo(2015);
        assertThat(result.getGenre()).isEqualTo("RPG");
        assertThat(result.getCoverUrl()).isEqualTo("https://img.rawg.io/witcher.jpg");
        assertThat(result.getReadUrl()).isEqualTo("https://rawg.io/games/the-witcher-3");
        assertThat(result.getSource()).isEqualTo("RAWG");
        assertThat(result.getExternalId()).isEqualTo("3498");
    }

    @Test
    @DisplayName("searchGames : genres vide → genre null")
    void searchGames_emptyGenres_shouldReturnNullGenre() {
        Map<String, Object> response = Map.of(
                "results", List.of(
                        Map.of("id", 1, "name", "Game", "genres", List.of())
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchGames("Game");

        assertThat(results.get(0).getGenre()).isNull();
    }

    @Test
    @DisplayName("searchGames : released invalide → releaseYear null")
    void searchGames_invalidReleasedDate_shouldReturnNullYear() {
        Map<String, Object> response = Map.of(
                "results", List.of(
                        Map.of("id", 1, "name", "Game", "released", "TBD")
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<ExternalMediaResponse> results = externalApiService.searchGames("Game");

        assertThat(results.get(0).getReleaseYear()).isNull();
    }

    @Test
    @DisplayName("searchGames : réponse null → liste vide")
    void searchGames_nullResponse_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThat(externalApiService.searchGames("query")).isEmpty();
    }

    @Test
    @DisplayName("searchGames : réponse sans 'results' → liste vide")
    void searchGames_noResultsKey_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of());

        assertThat(externalApiService.searchGames("query")).isEmpty();
    }


    @Test
    @DisplayName("searchPodcasts : fallback iTunes retourne une liste mappée")
    void searchPodcasts_itunesFallback_shouldReturnMappedResults() throws Exception {
        // PodcastIndex échoue → exchange lève une exception → fallback iTunes
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("PodcastIndex unavailable"));

        Map<String, Object> itunesResponse = Map.of(
                "results", List.of(
                        Map.of(
                                "collectionId",      123456,
                                "collectionName",    "Darknet Diaries",
                                "artistName",        "Jack Rhysider",
                                "primaryGenreName",  "Tech",
                                "artworkUrl600",     "https://img.url/darknet.jpg",
                                "trackViewUrl",      "https://podcasts.apple.com/darknet"
                        )
                )
        );

        String rawJson = new ObjectMapper().writeValueAsString(itunesResponse);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(rawJson);

        List<ExternalMediaResponse> results = externalApiService.searchPodcasts("Darknet");

        assertThat(results).hasSize(1);
        ExternalMediaResponse result = results.get(0);
        assertThat(result.getTitle()).isEqualTo("Darknet Diaries");
        assertThat(result.getAuthor()).isEqualTo("Jack Rhysider");
        assertThat(result.getGenre()).isEqualTo("Tech");
        assertThat(result.getCoverUrl()).isEqualTo("https://img.url/darknet.jpg");
        assertThat(result.getReadUrl()).isEqualTo("https://podcasts.apple.com/darknet");
        assertThat(result.getSource()).isEqualTo("iTunes");
        assertThat(result.getExternalId()).isEqualTo("123456");
        assertThat(result.getReleaseYear()).isNull();
        assertThat(result.getDescription()).isNull();
    }

    @Test
    @DisplayName("searchPodcastsItunes : réponse null → liste vide")
    void searchPodcastsItunes_nullResponse_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);

        assertThat(externalApiService.searchPodcastsItunes("query")).isEmpty();
    }

    @Test
    @DisplayName("searchPodcastsItunes : JSON sans 'results' → liste vide")
    void searchPodcastsItunes_noResultsKey_shouldReturnEmptyList() throws Exception {
        String rawJson = new ObjectMapper().writeValueAsString(Map.of());
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(rawJson);

        assertThat(externalApiService.searchPodcastsItunes("query")).isEmpty();
    }

    @Test
    @DisplayName("searchPodcastsItunes : JSON invalide → liste vide sans exception")
    void searchPodcastsItunes_invalidJson_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{invalid json}");

        assertThat(externalApiService.searchPodcastsItunes("query")).isEmpty();
    }

    @Test
    @DisplayName("searchPodcastsItunes : RestTemplate lève une exception → liste vide")
    void searchPodcastsItunes_restTemplateThrows_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThat(externalApiService.searchPodcastsItunes("query")).isEmpty();
    }


    @Test
    @DisplayName("searchFreeGames : retourne les jeux filtrés par query")
    void searchFreeGames_shouldReturnFilteredResults() {
        List<Map> allGames = List.of(
                Map.of(
                        "id",                1,
                        "title",             "Fortnite",
                        "genre",             "Battle Royale",
                        "short_description", "Un BR gratuit.",
                        "thumbnail",         "https://img.freetogame.com/fortnite.jpg",
                        "game_url",          "https://www.freetogame.com/fortnite"
                ),
                Map.of(
                        "id",    2,
                        "title", "World of Warcraft",
                        "genre", "MMORPG"
                )
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(allGames);

        List<ExternalMediaResponse> results = externalApiService.searchFreeGames("Fortnite");

        assertThat(results).hasSize(1);
        ExternalMediaResponse result = results.get(0);
        assertThat(result.getTitle()).isEqualTo("Fortnite");
        assertThat(result.getGenre()).isEqualTo("Battle Royale");
        assertThat(result.getDescription()).isEqualTo("Un BR gratuit.");
        assertThat(result.getCoverUrl()).isEqualTo("https://img.freetogame.com/fortnite.jpg");
        assertThat(result.getReadUrl()).isEqualTo("https://www.freetogame.com/fortnite");
        assertThat(result.getSource()).isEqualTo("FreeToGame");
        assertThat(result.getExternalId()).isEqualTo("1");
    }

    @Test
    @DisplayName("searchFreeGames : query vide → tous les jeux retournés")
    void searchFreeGames_blankQuery_shouldReturnAllGames() {
        List<Map> allGames = List.of(
                Map.of("id", 1, "title", "Fortnite"),
                Map.of("id", 2, "title", "Warframe")
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(allGames);

        List<ExternalMediaResponse> results = externalApiService.searchFreeGames("");

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("searchFreeGames : réponse null → liste vide")
    void searchFreeGames_nullResponse_shouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(null);

        assertThat(externalApiService.searchFreeGames("query")).isEmpty();
    }


    @Test
    @DisplayName("searchBooks : l'URL Internet Archive est correctement construite")
    void searchBooks_shouldCallCorrectUrl() {
        Map<String, Object> response = Map.of("response", Map.of("docs", List.of()));
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        externalApiService.searchBooks("Dune");

        verify(restTemplate).getForObject(
                contains("archive.org/advancedsearch.php"),
                eq(Map.class)
        );
    }

    @Test
    @DisplayName("searchFilms : l'URL TVMaze est correctement construite")
    void searchFilms_shouldCallCorrectUrl() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(List.of());

        externalApiService.searchFilms("Inception");

        verify(restTemplate).getForObject(
                eq("https://api.tvmaze.com/search/shows?q=Inception"),
                eq(List.class)
        );
    }

    @Test
    @DisplayName("searchGames : l'URL RAWG est correctement construite")
    void searchGames_shouldCallCorrectUrl() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of());

        externalApiService.searchGames("Witcher");

        verify(restTemplate).getForObject(
                eq("https://api.rawg.io/api/games?key=rawg-key&search=Witcher&page_size=10"),
                eq(Map.class)
        );
    }

    @Test
    @DisplayName("searchPodcastsItunes : l'URL iTunes est correctement construite")
    void searchPodcastsItunes_shouldCallCorrectUrl() throws Exception {
        String rawJson = new ObjectMapper().writeValueAsString(Map.of("results", List.of()));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(rawJson);

        externalApiService.searchPodcastsItunes("Tech");

        verify(restTemplate).getForObject(
                eq("https://itunes.apple.com/search?term=Tech&media=podcast&limit=10"),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("searchFreeGames : l'URL FreeToGame est correctement construite")
    void searchFreeGames_shouldCallCorrectUrl() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(List.of());

        externalApiService.searchFreeGames("Fortnite");

        verify(restTemplate).getForObject(
                eq("https://www.freetogame.com/api/games"),
                eq(List.class)
        );
    }
}
