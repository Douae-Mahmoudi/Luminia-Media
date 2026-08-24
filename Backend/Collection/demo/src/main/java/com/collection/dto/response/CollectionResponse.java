package com.collection.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class CollectionResponse {

    private String id;
    private String userId;
    private String name;
    private String description;
    private boolean isPublic;
    private List<String> mediaIds;
    private int mediaCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CollectionResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public List<String> getMediaIds() { return mediaIds; }
    public void setMediaIds(List<String> mediaIds) {
        this.mediaIds = mediaIds;
        this.mediaCount = mediaIds != null ? mediaIds.size() : 0;
    }
    public int getMediaCount() { return mediaCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
