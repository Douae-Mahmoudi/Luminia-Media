package com.collection.domain;

import java.time.LocalDateTime;

public class Watchlist {

    public enum WatchlistStatus {
        TO_WATCH, WATCHING, WATCHED
    }

    private String id;
    private String userId;
    private String mediaId;
    private WatchlistStatus status;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    public Watchlist(String id, String userId, String mediaId) {
        this.id = id;
        this.userId = userId;
        this.mediaId = mediaId;
        this.status = WatchlistStatus.TO_WATCH;
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(WatchlistStatus newStatus) {
        if (newStatus == null)
            throw new IllegalArgumentException("Status cannot be null");
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMediaId() { return mediaId; }
    public WatchlistStatus getStatus() { return status; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}