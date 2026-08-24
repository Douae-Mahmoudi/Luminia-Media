package com.mediatheque.media_svc.service;

import com.mediatheque.media_svc.dto.CreateMediaRequest;
import com.mediatheque.media_svc.dto.UpdateMediaRequest;
import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.event.MediaEventPublisher;
import com.mediatheque.media_svc.event.MediaStatusEvent;
import com.mediatheque.media_svc.exception.MediaNotFoundException;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaEventPublisher eventPublisher;


    public MediaResponse submitMedia(CreateMediaRequest request) {
        Media media = Media.builder()
                .ownerId(request.getOwnerId())
                .ownerUsername(request.getOwnerUsername())
                .title(request.getTitle())
                .author(request.getAuthor())
                .description(request.getDescription())
                .type(request.getType())
                .releaseYear(request.getReleaseYear())
                .genre(request.getGenre())
                .imageUrl(request.getImageUrl())
                .contentUrl(request.getContentUrl())
                .status(MediaStatus.PENDING)
                .build();

        return mapToResponse(mediaRepository.save(media));
    }

    public List<MediaResponse> getMediaByOwner(Long ownerId) {
        return mediaRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaResponse> getAvailableMedia() {
        return mediaRepository.findByStatus(MediaStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MediaResponse getMediaById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id));
        return mapToResponse(media);
    }


    public List<MediaResponse> getAllMedia() {
        return mediaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MediaResponse approveMedia(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id));
        media.setStatus(MediaStatus.AVAILABLE);
        mediaRepository.save(media);

        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(media.getId());
        event.setMediaTitle(media.getTitle());
        event.setOwnerId(media.getOwnerId());
        event.setStatus("ACCEPTED");
        eventPublisher.publishStatusEvent(event);

        return mapToResponse(media);
    }

    public List<MediaResponse> getPendingMedia() {
        return mediaRepository.findByStatus(MediaStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MediaResponse rejectMedia(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id));
        media.setStatus(MediaStatus.REJECTED);
        mediaRepository.save(media);

        MediaStatusEvent event = new MediaStatusEvent();
        event.setMediaId(media.getId());
        event.setMediaTitle(media.getTitle());
        event.setOwnerId(media.getOwnerId());
        event.setStatus("REFUSED");
        eventPublisher.publishStatusEvent(event);

        return mapToResponse(media);
    }

    public MediaResponse updateMedia(Long id, UpdateMediaRequest request) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id));

        if (request.getTitle() != null)         media.setTitle(request.getTitle());
        if (request.getAuthor() != null)        media.setAuthor(request.getAuthor());
        if (request.getDescription() != null)   media.setDescription(request.getDescription());
        if (request.getType() != null)          media.setType(request.getType());
        if (request.getReleaseYear() != null)   media.setReleaseYear(request.getReleaseYear());
        if (request.getGenre() != null)         media.setGenre(request.getGenre());
        if (request.getImageUrl() != null)      media.setImageUrl(request.getImageUrl());
        if (request.getContentUrl() != null)    media.setContentUrl(request.getContentUrl());
        // ownerUsername intentionnellement non modifiable après création

        return mapToResponse(mediaRepository.save(media));
    }

    public void deleteMedia(Long id) {
        if (!mediaRepository.existsById(id)) {
            throw new MediaNotFoundException("Media not found with id: " + id);
        }
        mediaRepository.deleteById(id);
    }


    public MediaResponse mapToResponse(Media media) {
        return MediaResponse.builder()
                .id(media.getId())
                .ownerId(media.getOwnerId())
                .ownerUsername(media.getOwnerUsername())  // ← ajouté
                .title(media.getTitle())
                .author(media.getAuthor())
                .description(media.getDescription())
                .type(media.getType())
                .status(media.getStatus())
                .releaseYear(media.getReleaseYear())
                .genre(media.getGenre())
                .imageUrl(media.getImageUrl())
                .contentUrl(media.getContentUrl())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
}