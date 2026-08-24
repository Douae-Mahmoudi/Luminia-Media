package com.mediatheque.media_svc.service;

import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.dto.UpdateMediaRequest;
import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.event.MediaEventPublisher;
import com.mediatheque.media_svc.exception.MediaNotFoundException;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaEventPublisher eventPublisher;

    @InjectMocks
    private MediaService mediaService;


    private Media buildMedia(Long id, MediaStatus status) {
        return Media.builder()
                .id(id)
                .ownerId(824036515L)
                .title("Mon média")
                .author("John Doe")
                .description("Description")
                .type(MediaType.BOOK)
                .status(status)
                .releaseYear(2024)
                .genre("Fiction")
                .imageUrl("https://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateMediaRequest buildCreateRequest() {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setOwnerId(824036515L);
        request.setTitle("Mon média");
        request.setAuthor("John Doe");
        request.setDescription("Description");
        request.setType(MediaType.BOOK);
        request.setReleaseYear(2024);
        request.setGenre("Fiction");
        request.setImageUrl("https://example.com/image.jpg");
        return request;
    }


    @Test
    void submitMedia_shouldSaveAndReturnResponse() {
        CreateMediaRequest request = buildCreateRequest();
        Media saved = buildMedia(1L, MediaStatus.PENDING);
        when(mediaRepository.save(any(Media.class))).thenReturn(saved);

        MediaResponse response = mediaService.submitMedia(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(MediaStatus.PENDING);
        assertThat(response.getOwnerId()).isEqualTo(824036515L);
        assertThat(response.getTitle()).isEqualTo("Mon média");

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MediaStatus.PENDING);
        assertThat(captor.getValue().getOwnerId()).isEqualTo(824036515L);
    }

    @Test
    void submitMedia_shouldMapAllFieldsCorrectly() {
        CreateMediaRequest request = buildCreateRequest();
        Media saved = buildMedia(1L, MediaStatus.PENDING);
        when(mediaRepository.save(any(Media.class))).thenReturn(saved);

        mediaService.submitMedia(request);

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        Media captured = captor.getValue();

        assertThat(captured.getTitle()).isEqualTo("Mon média");
        assertThat(captured.getAuthor()).isEqualTo("John Doe");
        assertThat(captured.getDescription()).isEqualTo("Description");
        assertThat(captured.getType()).isEqualTo(MediaType.BOOK);
        assertThat(captured.getReleaseYear()).isEqualTo(2024);
        assertThat(captured.getGenre()).isEqualTo("Fiction");
        assertThat(captured.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }


    @Test
    void getAvailableMedia_shouldReturnOnlyAvailableMedia() {
        List<Media> mediaList = List.of(
                buildMedia(1L, MediaStatus.AVAILABLE),
                buildMedia(2L, MediaStatus.AVAILABLE)
        );
        when(mediaRepository.findByStatus(MediaStatus.AVAILABLE)).thenReturn(mediaList);

        List<MediaResponse> result = mediaService.getAvailableMedia();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getStatus() == MediaStatus.AVAILABLE);
        verify(mediaRepository).findByStatus(MediaStatus.AVAILABLE);
    }

    @Test
    void getAvailableMedia_shouldReturnEmptyList_whenNoneAvailable() {
        when(mediaRepository.findByStatus(MediaStatus.AVAILABLE)).thenReturn(List.of());

        List<MediaResponse> result = mediaService.getAvailableMedia();

        assertThat(result).isEmpty();
    }


    @Test
    void getMediaById_shouldReturnMedia_whenFound() {
        Media media = buildMedia(1L, MediaStatus.AVAILABLE);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

        MediaResponse response = mediaService.getMediaById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Mon média");
    }

    @Test
    void getMediaById_shouldThrowMediaNotFoundException_whenNotFound() {
        when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.getMediaById(99L))
                .isInstanceOf(MediaNotFoundException.class)
                .hasMessageContaining("99");
    }


    @Test
    void getAllMedia_shouldReturnAllMedia() {
        List<Media> mediaList = List.of(
                buildMedia(1L, MediaStatus.AVAILABLE),
                buildMedia(2L, MediaStatus.PENDING),
                buildMedia(3L, MediaStatus.REJECTED)
        );
        when(mediaRepository.findAll()).thenReturn(mediaList);

        List<MediaResponse> result = mediaService.getAllMedia();

        assertThat(result).hasSize(3);
        verify(mediaRepository).findAll();
    }


    @Test
    void approveMedia_shouldSetStatusAvailableAndPublishEvent() {
        Media media = buildMedia(1L, MediaStatus.PENDING);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        when(mediaRepository.save(any(Media.class))).thenReturn(media);

        MediaResponse response = mediaService.approveMedia(1L);

        assertThat(response.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        verify(mediaRepository).save(media);
        verify(eventPublisher).publishStatusEvent(argThat(event ->
                event.getStatus().equals("ACCEPTED") &&
                        event.getMediaId().equals(1L) &&
                        event.getOwnerId().equals(824036515L)
        ));
    }

    @Test
    void approveMedia_shouldThrowMediaNotFoundException_whenNotFound() {
        when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.approveMedia(99L))
                .isInstanceOf(MediaNotFoundException.class)
                .hasMessageContaining("99");

        verify(eventPublisher, never()).publishStatusEvent(any());
    }


    @Test
    void getPendingMedia_shouldReturnOnlyPendingMedia() {
        List<Media> mediaList = List.of(
                buildMedia(1L, MediaStatus.PENDING),
                buildMedia(2L, MediaStatus.PENDING)
        );
        when(mediaRepository.findByStatus(MediaStatus.PENDING)).thenReturn(mediaList);

        List<MediaResponse> result = mediaService.getPendingMedia();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getStatus() == MediaStatus.PENDING);
        verify(mediaRepository).findByStatus(MediaStatus.PENDING);
    }


    @Test
    void rejectMedia_shouldSetStatusRejectedAndPublishEvent() {
        Media media = buildMedia(1L, MediaStatus.PENDING);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        when(mediaRepository.save(any(Media.class))).thenReturn(media);

        MediaResponse response = mediaService.rejectMedia(1L);

        assertThat(response.getStatus()).isEqualTo(MediaStatus.REJECTED);
        verify(mediaRepository).save(media);
        verify(eventPublisher).publishStatusEvent(argThat(event ->
                event.getStatus().equals("REFUSED") &&
                        event.getMediaId().equals(1L) &&
                        event.getOwnerId().equals(824036515L)
        ));
    }

    @Test
    void rejectMedia_shouldThrowMediaNotFoundException_whenNotFound() {
        when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.rejectMedia(99L))
                .isInstanceOf(MediaNotFoundException.class)
                .hasMessageContaining("99");

        verify(eventPublisher, never()).publishStatusEvent(any());
    }


    @Test
    void updateMedia_shouldUpdateOnlyNonNullFields() {
        Media media = buildMedia(1L, MediaStatus.AVAILABLE);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        when(mediaRepository.save(any(Media.class))).thenReturn(media);

        UpdateMediaRequest request = new UpdateMediaRequest();
        request.setTitle("Nouveau titre");
        request.setGenre("Thriller");

        mediaService.updateMedia(1L, request);

        assertThat(media.getTitle()).isEqualTo("Nouveau titre");
        assertThat(media.getGenre()).isEqualTo("Thriller");
        assertThat(media.getAuthor()).isEqualTo("John Doe"); // inchangé
        verify(mediaRepository).save(media);
    }

    @Test
    void updateMedia_shouldNotUpdateFields_whenAllNull() {
        Media media = buildMedia(1L, MediaStatus.AVAILABLE);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        when(mediaRepository.save(any(Media.class))).thenReturn(media);

        UpdateMediaRequest request = new UpdateMediaRequest();

        mediaService.updateMedia(1L, request);

        assertThat(media.getTitle()).isEqualTo("Mon média");
        assertThat(media.getAuthor()).isEqualTo("John Doe");
        verify(mediaRepository).save(media);
    }

    @Test
    void updateMedia_shouldThrowMediaNotFoundException_whenNotFound() {
        when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.updateMedia(99L, new UpdateMediaRequest()))
                .isInstanceOf(MediaNotFoundException.class)
                .hasMessageContaining("99");
    }


    @Test
    void deleteMedia_shouldDeleteSuccessfully_whenExists() {
        when(mediaRepository.existsById(1L)).thenReturn(true);

        mediaService.deleteMedia(1L);

        verify(mediaRepository).deleteById(1L);
    }

    @Test
    void deleteMedia_shouldThrowMediaNotFoundException_whenNotFound() {
        when(mediaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> mediaService.deleteMedia(99L))
                .isInstanceOf(MediaNotFoundException.class)
                .hasMessageContaining("99");

        verify(mediaRepository, never()).deleteById(any());
    }


    @Test
    void mapToResponse_shouldMapAllFieldsCorrectly() {
        Media media = buildMedia(1L, MediaStatus.AVAILABLE);

        MediaResponse response = mediaService.mapToResponse(media);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOwnerId()).isEqualTo(824036515L);
        assertThat(response.getTitle()).isEqualTo("Mon média");
        assertThat(response.getAuthor()).isEqualTo("John Doe");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getType()).isEqualTo(MediaType.BOOK);
        assertThat(response.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        assertThat(response.getReleaseYear()).isEqualTo(2024);
        assertThat(response.getGenre()).isEqualTo("Fiction");
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }
}