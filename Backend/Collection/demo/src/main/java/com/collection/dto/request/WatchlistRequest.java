package com.collection.dto.request;

import com.collection.domain.Watchlist.WatchlistStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WatchlistRequest {

    @NotBlank(message = "MediaId is required")
    private String mediaId;

    @NotNull(message = "Status is required")
    private WatchlistStatus status;

    public WatchlistRequest() {}

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
    public WatchlistStatus getStatus() { return status; }
    public void setStatus(WatchlistStatus status) { this.status = status; }
}
