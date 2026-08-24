package com.mediatheque.media_svc.integration;

import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import com.mediatheque.media_svc.service.MediaSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MediaSearchServiceIntegrationTest {

    @Autowired
    private MediaSearchService mediaSearchService;

    @Autowired
    private MediaRepository mediaRepository;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
        mediaRepository.saveAll(List.of(
                Media.builder()
                        .title("Harry Potter")
                        .author("Rowling")
                        .type(MediaType.BOOK)
                        .genre("Fantasy")
                        .releaseYear(1997)
                        .status(MediaStatus.AVAILABLE)
                        .build(),
                Media.builder()
                        .title("Inception")
                        .author("Nolan")
                        .type(MediaType.FILM)
                        .genre("SciFi")
                        .releaseYear(2010)
                        .status(MediaStatus.AVAILABLE)
                        .build(),
                Media.builder()
                        .title("Dune Pending")
                        .author("Villeneuve")
                        .type(MediaType.FILM)
                        .genre("SciFi")
                        .releaseYear(2021)
                        .status(MediaStatus.PENDING)
                        .build()
        ));
    }

    @Test
    void searchByTitle_shouldReturnMatchingAvailable() {
        List<MediaResponse> result = mediaSearchService.searchByTitle("harry");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void searchByTitle_shouldBeCaseInsensitive() {
        List<MediaResponse> result = mediaSearchService.searchByTitle("HARRY");
        assertThat(result).hasSize(1);
    }

    @Test
    void searchByTitle_shouldNotReturnPending() {
        List<MediaResponse> result = mediaSearchService.searchByTitle("Dune");
        assertThat(result).isEmpty();
    }

    @Test
    void searchByTitle_shouldReturnEmpty_whenNoMatch() {
        List<MediaResponse> result = mediaSearchService.searchByTitle("xyz");
        assertThat(result).isEmpty();
    }

    @Test
    void searchByType_shouldReturnOnlyBooksAvailable() {
        List<MediaResponse> result = mediaSearchService.searchByType(MediaType.BOOK);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(MediaType.BOOK);
    }

    @Test
    void searchByType_shouldNotReturnPending() {
        List<MediaResponse> result = mediaSearchService.searchByType(MediaType.FILM);
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .extracting(MediaResponse::getTitle)
                .containsExactly("Inception");
    }

    @Test
    void searchByGenre_shouldReturnMatchingAvailable() {
        List<MediaResponse> result = mediaSearchService.searchByGenre("Fantasy");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGenre()).isEqualTo("Fantasy");
    }

    @Test
    void searchByGenre_shouldBeCaseInsensitive() {
        List<MediaResponse> result = mediaSearchService.searchByGenre("fantasy");
        assertThat(result).hasSize(1);
    }

    @Test
    void searchByReleaseYear_shouldReturnCorrectMedia() {
        List<MediaResponse> result = mediaSearchService.searchByReleaseYear(1997);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void searchByReleaseYear_shouldReturnEmpty_whenNoMatch() {
        List<MediaResponse> result = mediaSearchService.searchByReleaseYear(1800);
        assertThat(result).isEmpty();
    }

    @Test
    void searchByFilters_titleOnly_shouldWork() {
        List<MediaResponse> result = mediaSearchService
                .searchByFilters("inception", null, null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    void searchByFilters_typeAndGenre_shouldWork() {
        List<MediaResponse> result = mediaSearchService
                .searchByFilters(null, MediaType.FILM, "SciFi", null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    void searchByFilters_noFilters_shouldReturnAllAvailable() {
        List<MediaResponse> result = mediaSearchService
                .searchByFilters(null, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void searchByFilters_shouldNotReturnPending() {
        List<MediaResponse> result = mediaSearchService
                .searchByFilters(null, null, null, null);
        assertThat(result)
                .isNotEmpty()
                .allMatch(m -> m.getStatus() == MediaStatus.AVAILABLE);
    }
}