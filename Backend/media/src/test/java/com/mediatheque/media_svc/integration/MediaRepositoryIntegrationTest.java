package com.mediatheque.media_svc.integration;

import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MediaRepositoryIntegrationTest {

    @Autowired
    private MediaRepository mediaRepository;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();

        mediaRepository.saveAll(List.of(
                Media.builder()
                        .title("Inception")
                        .author("Nolan")
                        .type(MediaType.FILM)
                        .genre("SciFi")
                        .releaseYear(2010)
                        .status(MediaStatus.AVAILABLE)
                        .build(),
                Media.builder()
                        .title("Harry Potter")
                        .author("Rowling")
                        .type(MediaType.BOOK)
                        .genre("Fantasy")
                        .releaseYear(1997)
                        .status(MediaStatus.AVAILABLE)
                        .build(),
                Media.builder()
                        .title("Dune")
                        .author("Villeneuve")
                        .type(MediaType.FILM)
                        .genre("SciFi")
                        .releaseYear(2021)
                        .status(MediaStatus.PENDING)
                        .build()
        ));
    }

    @Test
    void findByStatus_shouldReturnOnlyAvailable() {
        List<Media> result = mediaRepository.findByStatus(MediaStatus.AVAILABLE);
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(m -> m.getStatus() == MediaStatus.AVAILABLE);
    }

    @Test
    void findByStatus_shouldReturnOnlyPending() {
        List<Media> result = mediaRepository.findByStatus(MediaStatus.PENDING);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Dune");
    }

    @Test
    void findByTypeAndStatus_shouldReturnCorrectMedia() {
        List<Media> result = mediaRepository.findByTypeAndStatus(
                MediaType.BOOK, MediaStatus.AVAILABLE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void findByTitleContainingIgnoreCaseAndStatus_shouldBeCaseInsensitive() {
        List<Media> result = mediaRepository
                .findByTitleContainingIgnoreCaseAndStatus("harry", MediaStatus.AVAILABLE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void findByTitleContainingIgnoreCaseAndStatus_shouldReturnEmpty_whenNotFound() {
        List<Media> result = mediaRepository
                .findByTitleContainingIgnoreCaseAndStatus("xyz", MediaStatus.AVAILABLE);
        assertThat(result).isEmpty();
    }

    @Test
    void findByGenreContainingIgnoreCaseAndStatus_shouldWork() {
        List<Media> result = mediaRepository
                .findByGenreContainingIgnoreCaseAndStatus("sci", MediaStatus.AVAILABLE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    void findByReleaseYearAndStatus_shouldReturnCorrectMedia() {
        List<Media> result = mediaRepository
                .findByReleaseYearAndStatus(1997, MediaStatus.AVAILABLE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void prePersist_shouldSetCreatedAtAndUpdatedAt() {
        Media saved = mediaRepository.save(
                Media.builder()
                        .title("Test")
                        .author("Author")
                        .type(MediaType.BOOK)
                        .status(MediaStatus.AVAILABLE)
                        .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
