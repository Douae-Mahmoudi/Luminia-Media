package com.collection.infrastructure.persistence.adapter;

import com.collection.domain.Collection;
import com.collection.domain.Watchlist;
import com.collection.infrastructure.persistence.JpaCollectionRepository;
import com.collection.infrastructure.persistence.entity.CollectionEntity;
import com.collection.infrastructure.persistence.entity.CollectionEntity.CollectionType;
import com.collection.infrastructure.persistence.entity.CollectionEntity.WatchlistStatus;
import com.collection.mapper.CollectionMapper;
import com.collection.repository.CollectionRepository;
import com.collection.repository.WatchlistRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CollectionRepositoryAdapter implements CollectionRepository, WatchlistRepository {

    private final JpaCollectionRepository jpa;

    public CollectionRepositoryAdapter(JpaCollectionRepository jpa) {
        this.jpa = jpa;
    }


    @Override
    public Collection save(Collection collection) {
        CollectionEntity entity = CollectionMapper.toEntity(collection);
        entity.setType(CollectionType.COLLECTION);
        return CollectionMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Collection> findById(String id) {
        return jpa.findById(id).map(CollectionMapper::toDomain);
    }

    @Override
    public List<Collection> findByUserId(String userId) {
        return jpa.findByUserIdAndType(userId, CollectionType.COLLECTION)
                .stream()
                .map(CollectionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Collection> findPublicByUserId(String userId) {
        return jpa.findByUserIdAndIsPublicTrueAndType(userId, CollectionType.COLLECTION)
                .stream()
                .map(CollectionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return jpa.existsById(id);
    }

    @Override
    public void deleteById(String id) {
        jpa.deleteById(id);
    }


    @Override
    public Watchlist saveWatchlist(Watchlist watchlist) {
        return toWatchlistDomain(jpa.save(toWatchlistEntity(watchlist)));
    }

    @Override
    public Optional<Watchlist> findWatchlistByUserIdAndMediaId(String userId, String mediaId) {
        return jpa.findByUserIdAndMediaIdsContainingAndType(userId, mediaId, CollectionType.WATCHLIST)
                .map(this::toWatchlistDomain);
    }

    @Override
    public List<Watchlist> findWatchlistByUserId(String userId) {
        return jpa.findByUserIdAndType(userId, CollectionType.WATCHLIST)
                .stream()
                .map(this::toWatchlistDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Watchlist> findWatchlistByUserIdAndStatus(String userId, Watchlist.WatchlistStatus status) {
        WatchlistStatus entityStatus = WatchlistStatus.valueOf(status.name());
        return jpa.findByUserIdAndTypeAndWatchlistStatus(userId, CollectionType.WATCHLIST, entityStatus)
                .stream()
                .map(this::toWatchlistDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsWatchlistByUserIdAndMediaId(String userId, String mediaId) {
        return jpa.existsByUserIdAndMediaIdsContainingAndType(userId, mediaId, CollectionType.WATCHLIST);
    }

    @Override
    public void deleteWatchlistByUserIdAndMediaId(String userId, String mediaId) {
        jpa.deleteByUserIdAndMediaIdsContainingAndType(userId, mediaId, CollectionType.WATCHLIST);
    }


    private CollectionEntity toWatchlistEntity(Watchlist watchlist) {
        CollectionEntity entity = new CollectionEntity();
        entity.setId(watchlist.getId() != null ? watchlist.getId() : UUID.randomUUID().toString());
        entity.setUserId(watchlist.getUserId());
        entity.setType(CollectionType.WATCHLIST);
        entity.setWatchlistStatus(WatchlistStatus.valueOf(watchlist.getStatus().name()));
        entity.setMediaIds(List.of(watchlist.getMediaId()));
        entity.setCreatedAt(watchlist.getAddedAt() != null ? watchlist.getAddedAt() : LocalDateTime.now());
        entity.setUpdatedAt(watchlist.getUpdatedAt() != null ? watchlist.getUpdatedAt() : LocalDateTime.now());
        return entity;
    }
    @Override
    public List<Collection> findAllPublic() {
        return jpa.findByIsPublicTrueAndType(CollectionType.COLLECTION)
                .stream()
                .map(CollectionMapper::toDomain)
                .collect(Collectors.toList());
    }
    private Watchlist toWatchlistDomain(CollectionEntity entity) {
        String mediaId = entity.getMediaIds() != null && !entity.getMediaIds().isEmpty()
                ? entity.getMediaIds().get(0) : null;
        Watchlist watchlist = new Watchlist(entity.getId(), entity.getUserId(), mediaId);
        if (entity.getWatchlistStatus() != null) {
            watchlist.updateStatus(Watchlist.WatchlistStatus.valueOf(entity.getWatchlistStatus().name()));
        }
        return watchlist;
    }
}