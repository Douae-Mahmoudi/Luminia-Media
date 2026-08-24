package com.mediatheque.media_svc.integration;

import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.dto.UpdateMediaRequest;
import com.mediatheque.media_svc.event.MediaEventPublisher;
import com.mediatheque.media_svc.exception.MediaNotFoundException;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import com.mediatheque.media_svc.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MediaServiceIntegrationTest {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MediaRepository mediaRepository;

    @MockitoBean
    private MediaEventPublisher mediaEventPublisher;  // ← Kafka mocké

    private Long availableId;
    private Long pendingId;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();

        Media available = mediaRepository.save(Media.builder()
                .title("Inception")
                .author("Nolan")
                .type(MediaType.FILM)
                .genre("SciFi")
                .releaseYear(2010)
                .status(MediaStatus.AVAILABLE)
                .build());

        Media pending = mediaRepository.save(Media.builder()
                .title("Dune")
                .author("Villeneuve")
                .type(MediaType.FILM)
                .status(MediaStatus.PENDING)
                .build());

        availableId = available.getId();
        pendingId   = pending.getId();
    }


    @Test
    void submitMedia_shouldPersistWithPendingStatus() {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setTitle("Interstellar");
        request.setAuthor("Nolan");
        request.setType(MediaType.FILM);
        request.setGenre("SciFi");
        request.setReleaseYear(2014);

        MediaResponse response = mediaService.submitMedia(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Interstellar");
        assertThat(response.getStatus()).isEqualTo(MediaStatus.PENDING);
        assertThat(mediaRepository.findById(response.getId())).isPresent();
    }


    @Test
    void getAvailableMedia_shouldReturnOnlyAvailable() {
        List<MediaResponse> result = mediaService.getAvailableMedia();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
        assertThat(result.get(0).getStatus()).isEqualTo(MediaStatus.AVAILABLE);
    }


    @Test
    void getMediaById_shouldReturnCorrectMedia() {
        MediaResponse response = mediaService.getMediaById(availableId);
        assertThat(response.getTitle()).isEqualTo("Inception");
    }

    @Test
    void getMediaById_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> mediaService.getMediaById(9999L))
                .isInstanceOf(MediaNotFoundException.class)
                .hasMessageContaining("9999");
    }


    @Test
    void getAllMedia_shouldReturnAllMedia() {
        List<MediaResponse> result = mediaService.getAllMedia();
        assertThat(result).hasSize(2);
    }


    @Test
    void getPendingMedia_shouldReturnOnlyPending() {
        List<MediaResponse> result = mediaService.getPendingMedia();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Dune");
    }


    @Test
    void approveMedia_shouldChangeStatusToAvailable() {
        MediaResponse response = mediaService.approveMedia(pendingId);
        assertThat(response.getStatus()).isEqualTo(MediaStatus.AVAILABLE);

        Media inDb = mediaRepository.findById(pendingId).orElseThrow();
        assertThat(inDb.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
    }

    @Test
    void approveMedia_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> mediaService.approveMedia(9999L))
                .isInstanceOf(MediaNotFoundException.class);
    }


    @Test
    void rejectMedia_shouldChangeStatusToRejected() {
        MediaResponse response = mediaService.rejectMedia(pendingId);
        assertThat(response.getStatus()).isEqualTo(MediaStatus.REJECTED);

        Media inDb = mediaRepository.findById(pendingId).orElseThrow();
        assertThat(inDb.getStatus()).isEqualTo(MediaStatus.REJECTED);
    }

    @Test
    void rejectMedia_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> mediaService.rejectMedia(9999L))
                .isInstanceOf(MediaNotFoundException.class);
    }


    @Test
    void updateMedia_shouldPersistChanges() {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Inception Directors Cut");
        request.setGenre("Thriller");

        MediaResponse response = mediaService.updateMedia(availableId, request);

        assertThat(response.getTitle()).isEqualTo("Inception Directors Cut");
        assertThat(response.getGenre()).isEqualTo("Thriller");
        assertThat(response.getAuthor()).isEqualTo("Nolan");

        Media inDb = mediaRepository.findById(availableId).orElseThrow();
        assertThat(inDb.getTitle()).isEqualTo("Inception Directors Cut");
    }

    @Test
    void updateMedia_shouldNotOverwriteNullFields() {
        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Nouveau Titre");

        mediaService.updateMedia(availableId, request);

        Media inDb = mediaRepository.findById(availableId).orElseThrow();
        assertThat(inDb.getAuthor()).isEqualTo("Nolan");
    }

    @Test
    void updateMedia_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> mediaService.updateMedia(9999L, new UpdateMediaRequest()))
                .isInstanceOf(MediaNotFoundException.class);
    }


    @Test
    void deleteMedia_shouldRemoveFromDatabase() {
        mediaService.deleteMedia(availableId);
        assertThat(mediaRepository.findById(availableId)).isEmpty();
    }

    @Test
    void deleteMedia_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> mediaService.deleteMedia(9999L))
                .isInstanceOf(MediaNotFoundException.class);
    }
}