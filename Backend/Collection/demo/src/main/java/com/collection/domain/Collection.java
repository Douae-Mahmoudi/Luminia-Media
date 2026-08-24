package com.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Collection {
    private String id;
    private String userId;
    private String name;
    private String description;
    private boolean isPublic;
    private List<String> mediaIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Collection(String id, String userId, String name, String description, boolean isPublic) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.mediaIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addMedia(String mediaId) {
        if (!mediaIds.contains(mediaId)) {
            mediaIds.add(mediaId);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeMedia(String mediaId) {
        mediaIds.remove(mediaId);
        this.updatedAt = LocalDateTime.now();
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    public void toggleVisibility() {
        this.isPublic = !this.isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @JsonProperty("isPublic")
    public boolean isPublic() { return isPublic; }

    @JsonProperty("isPublic")
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public List<String> getMediaIds() { return Collections.unmodifiableList(mediaIds); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}