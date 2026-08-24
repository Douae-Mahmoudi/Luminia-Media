package com.collection.usecase.collection.impl;

import com.collection.repository.CollectionRepository;
import com.collection.usecase.collection.DeleteCollectionUseCase;
import org.springframework.stereotype.Service;

@Service
public class DeleteCollectionUseCaseImpl implements DeleteCollectionUseCase {

    private final CollectionRepository collectionRepository;

    public DeleteCollectionUseCaseImpl(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Override
    public void execute(String id) {
        collectionRepository.deleteById(id);
    }
}