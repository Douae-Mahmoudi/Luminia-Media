package com.collection.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections")
public class CollectionEntity {

    public enum CollectionType {
        COLLECTION, WATCHLIST
    }

    public enum WatchlistStatus {
        TO_WATCH, WATCHING, WATCHED
    }

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name = "is_public")
    private boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "watchlist_status")
    private WatchlistStatus watchlistStatus;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collection_media", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "media_id")
    private List<String> mediaIds = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CollectionEntity() {}

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

    public CollectionType getType() { return type; }
    public void setType(CollectionType type) { this.type = type; }

    public WatchlistStatus getWatchlistStatus() { return watchlistStatus; }
    public void setWatchlistStatus(WatchlistStatus watchlistStatus) { this.watchlistStatus = watchlistStatus; }

    public List<String> getMediaIds() { return mediaIds; }
    public void setMediaIds(List<String> mediaIds) { this.mediaIds = mediaIds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}