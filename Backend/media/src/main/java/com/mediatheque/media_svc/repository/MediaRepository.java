package com.mediatheque.media_svc.repository;

import com.mediatheque.media_svc.model.Media;
import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByStatus(MediaStatus status);
    List<Media> findByTypeAndStatus(MediaType type, MediaStatus status);
    List<Media> findByGenreContainingIgnoreCaseAndStatus(String genre, MediaStatus status);
    List<Media> findByReleaseYearAndStatus(Integer releaseYear, MediaStatus status);
    List<Media> findByTitleContainingIgnoreCaseAndStatus(String title, MediaStatus status);
    List<Media> findByType(MediaType type);
    List<Media> findByOwnerId(Long ownerId);
}