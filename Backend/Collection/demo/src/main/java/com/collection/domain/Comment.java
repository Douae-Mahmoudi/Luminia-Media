package com.collection.domain;

import java.time.LocalDateTime;

public class Comment {
    private String id;
    private String userId;
    private String mediaId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Comment(String id, String userId, String mediaId, String content) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Content cannot be blank");
        if (content.length() > 1000)
            throw new IllegalArgumentException("Comment exceeds 1000 characters");
        this.id = id;
        this.userId = userId;
        this.mediaId = mediaId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void edit(String newContent) {
        if (newContent == null || newContent.isBlank())
            throw new IllegalArgumentException("Content cannot be blank");
        if (newContent.length() > 1000)
            throw new IllegalArgumentException("Comment exceeds 1000 characters");
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(String userId) {
        return this.userId.equals(userId);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMediaId() { return mediaId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}