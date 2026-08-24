package com.collection.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "media_id", "type"}))
public class LikeEntity {

    public enum LikeType {
        LIKE, FAVORITE
    }

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "media_id", nullable = false)
    private String mediaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LikeEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public LikeType getType() { return type; }
    public void setType(LikeType type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}