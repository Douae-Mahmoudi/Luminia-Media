package com.collection.usecase.collection;

import com.collection.domain.Collection;
import com.collection.repository.CollectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCollectionUseCase {

    private final CollectionRepository collectionRepository;

    public GetCollectionUseCase(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public Collection getById(String collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found: " + collectionId));
    }

    public List<Collection> getByUserId(String userId) {
        return collectionRepository.findByUserId(userId);
    }

    public List<Collection> getPublicByUserId(String userId) {
        return collectionRepository.findPublicByUserId(userId);
    }

    public List<Collection> getAllPublic() { // ← nouveau
        return collectionRepository.findAllPublic();
    }
}