package com.collection.infrastructure.persistence.adapter;

import com.collection.domain.Favorite;
import com.collection.domain.Like;
import com.collection.infrastructure.persistence.JpaLikeRepository;
import com.collection.infrastructure.persistence.entity.LikeEntity;
import com.collection.infrastructure.persistence.entity.LikeEntity.LikeType;
import com.collection.mapper.LikeMapper;
import com.collection.repository.FavoriteRepository;
import com.collection.repository.LikeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LikeRepositoryAdapter implements LikeRepository, FavoriteRepository {

    private final JpaLikeRepository jpa;

    public LikeRepositoryAdapter(JpaLikeRepository jpa) {
        this.jpa = jpa;
    }


    @Override
    public Like save(Like like) {
        LikeEntity entity = LikeMapper.toEntity(like);
        entity.setType(LikeType.LIKE);
        return LikeMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Like> findByUserIdAndMediaId(String userId, String mediaId) {
        return jpa.findByUserIdAndMediaIdAndType(userId, mediaId, LikeType.LIKE)
                .map(LikeMapper::toDomain);
    }

    @Override
    public List<Like> findByUserId(String userId) {
        return jpa.findByUserIdAndType(userId, LikeType.LIKE)
                .stream()
                .map(LikeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByMediaId(String mediaId) {
        return jpa.countByMediaIdAndType(mediaId, LikeType.LIKE);
    }

    @Override
    public boolean existsByUserIdAndMediaId(String userId, String mediaId) {
        return jpa.existsByUserIdAndMediaIdAndType(userId, mediaId, LikeType.LIKE);
    }

    @Override
    public void deleteByUserIdAndMediaId(String userId, String mediaId) {
        jpa.deleteByUserIdAndMediaIdAndType(userId, mediaId, LikeType.LIKE);
    }


    @Override
    public Favorite saveFavorite(Favorite favorite) {
        return toFavoriteDomain(jpa.save(toFavoriteEntity(favorite)));
    }

    @Override
    public Optional<Favorite> findFavoriteByUserIdAndMediaId(String userId, String mediaId) {
        return jpa.findByUserIdAndMediaIdAndType(userId, mediaId, LikeType.FAVORITE)
                .map(this::toFavoriteDomain);
    }

    @Override
    public List<Favorite> findFavoritesByUserId(String userId) {
        return jpa.findByUserIdAndType(userId, LikeType.FAVORITE)
                .stream()
                .map(this::toFavoriteDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsFavoriteByUserIdAndMediaId(String userId, String mediaId) {
        return jpa.existsByUserIdAndMediaIdAndType(userId, mediaId, LikeType.FAVORITE);
    }

    @Override
    public void deleteFavoriteByUserIdAndMediaId(String userId, String mediaId) {
        jpa.deleteByUserIdAndMediaIdAndType(userId, mediaId, LikeType.FAVORITE);
    }


    private LikeEntity toFavoriteEntity(Favorite favorite) {
        LikeEntity entity = new LikeEntity();
        entity.setId(favorite.getId() != null ? favorite.getId() : UUID.randomUUID().toString());
        entity.setUserId(favorite.getUserId());
        entity.setMediaId(favorite.getMediaId());
        entity.setType(LikeType.FAVORITE);
        entity.setCreatedAt(favorite.getFavoritedAt() != null ? favorite.getFavoritedAt() : LocalDateTime.now());
        return entity;
    }

    private Favorite toFavoriteDomain(LikeEntity entity) {
        return new Favorite(entity.getId(), entity.getUserId(), entity.getMediaId());
    }
}