package com.collection.dto.response;

import java.time.LocalDateTime;

public class LikeResponse {

    private String id;
    private String userId;
    private String mediaId;
    private LocalDateTime likedAt;

    public LikeResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
    public LocalDateTime getLikedAt() { return likedAt; }
    public void setLikedAt(LocalDateTime likedAt) { this.likedAt = likedAt; }
}
