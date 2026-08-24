package com.collection.dto.response;

import com.collection.domain.Watchlist.WatchlistStatus;
import java.time.LocalDateTime;

public class WatchlistResponse {

    private String id;
    private String userId;
    private String mediaId;
    private WatchlistStatus status;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    public WatchlistResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
    public WatchlistStatus getStatus() { return status; }
    public void setStatus(WatchlistStatus status) { this.status = status; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
