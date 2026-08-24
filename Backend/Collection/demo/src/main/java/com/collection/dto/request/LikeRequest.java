package com.collection.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LikeRequest {

    @NotBlank(message = "MediaId is required")
    private String mediaId;

    public LikeRequest() {}

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
}
