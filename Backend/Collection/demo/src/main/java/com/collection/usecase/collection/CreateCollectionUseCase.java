package com.collection.usecase.collection;

import com.collection.domain.Collection;
import com.collection.repository.CollectionRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCollectionUseCase {

    private final CollectionRepository collectionRepository;

    public CreateCollectionUseCase(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public record Input(
            String userId,
            String name,
            String description,
            @JsonProperty("isPublic") boolean isPublic
    ) {}

    public Collection execute(Input input) {
        Collection collection = new Collection(
                UUID.randomUUID().toString(),
                input.userId(),
                input.name(),
                input.description(),
                input.isPublic()
        );
        return collectionRepository.save(collection);
    }
}