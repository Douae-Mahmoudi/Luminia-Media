package com.mediatheque.media_svc.service;

import com.mediatheque.media_svc.dto.MediaResponse;
import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import com.mediatheque.media_svc.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaSearchService {

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;

    public List<MediaResponse> searchByTitle(String title) {
        return mediaRepository.findByTitleContainingIgnoreCaseAndStatus(title, MediaStatus.AVAILABLE)
                .stream()
                .map(mediaService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaResponse> searchByType(MediaType type) {
        return mediaRepository.findByTypeAndStatus(type, MediaStatus.AVAILABLE)
                .stream()
                .map(mediaService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaResponse> searchByGenre(String genre) {
        return mediaRepository.findByGenreContainingIgnoreCaseAndStatus(genre, MediaStatus.AVAILABLE)
                .stream()
                .map(mediaService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaResponse> searchByReleaseYear(Integer year) {
        return mediaRepository.findByReleaseYearAndStatus(year, MediaStatus.AVAILABLE)
                .stream()
                .map(mediaService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaResponse> searchByFilters(String title, MediaType type, String genre, Integer releaseYear) {
        return mediaRepository.findAll()
                .stream()
                .filter(m -> m.getStatus() == MediaStatus.AVAILABLE)
                .filter(m -> title == null || m.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(m -> type == null || m.getType() == type)
                .filter(m -> genre == null || (m.getGenre() != null && m.getGenre().toLowerCase().contains(genre.toLowerCase())))
                .filter(m -> releaseYear == null || releaseYear.equals(m.getReleaseYear()))
                .map(mediaService::mapToResponse)
                .collect(Collectors.toList());
    }
}