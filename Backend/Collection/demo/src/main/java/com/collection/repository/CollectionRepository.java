package com.collection.repository;

import com.collection.domain.Collection;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository {
    Collection save(Collection collection);
    Optional<Collection> findById(String id);
    List<Collection> findByUserId(String userId);
    List<Collection> findPublicByUserId(String userId);
    List<Collection> findAllPublic(); // ← nouveau
    boolean existsById(String id);
    void deleteById(String id);
}