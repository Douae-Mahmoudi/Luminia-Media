package com.collection.usecase.collection;

import com.collection.domain.Collection;
import com.collection.repository.CollectionRepository;
import org.springframework.stereotype.Service;

@Service
public class AddMediaToCollectionUseCase {

    private final CollectionRepository collectionRepository;

    public AddMediaToCollectionUseCase(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public record Input(String userId, String collectionId, String mediaId) {}

    public Collection execute(Input input) {
        Collection collection = collectionRepository.findById(input.collectionId())
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (!collection.getUserId().equals(input.userId()))
            throw new RuntimeException("Access denied");

        collection.addMedia(input.mediaId());
        return collectionRepository.save(collection);
    }
}