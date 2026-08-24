package com.collection.domain;

import java.time.LocalDateTime;

public class Favorite {
    private String id;
    private String userId;
    private String mediaId;
    private LocalDateTime favoritedAt;

    public Favorite(String id, String userId, String mediaId) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("UserId cannot be blank");
        if (mediaId == null || mediaId.isBlank())
            throw new IllegalArgumentException("MediaId cannot be blank");
        this.id = id;
        this.userId = userId;
        this.mediaId = mediaId;
        this.favoritedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMediaId() { return mediaId; }
    public LocalDateTime getFavoritedAt() { return favoritedAt; }
}