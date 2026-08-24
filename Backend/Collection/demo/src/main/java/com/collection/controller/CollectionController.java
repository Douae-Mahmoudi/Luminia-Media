package com.collection.controller;

import com.collection.domain.Collection;
import com.collection.usecase.collection.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CreateCollectionUseCase          createCollectionUseCase;
    private final GetCollectionUseCase             getCollectionUseCase;
    private final AddMediaToCollectionUseCase      addMediaToCollectionUseCase;
    private final RemoveMediaFromCollectionUseCase removeMediaFromCollectionUseCase;
    private final DeleteCollectionUseCase          deleteCollectionUseCase;

    public CollectionController(
            CreateCollectionUseCase          createCollectionUseCase,
            GetCollectionUseCase             getCollectionUseCase,
            AddMediaToCollectionUseCase      addMediaToCollectionUseCase,
            RemoveMediaFromCollectionUseCase removeMediaFromCollectionUseCase,
            DeleteCollectionUseCase          deleteCollectionUseCase) {
        this.createCollectionUseCase          = createCollectionUseCase;
        this.getCollectionUseCase             = getCollectionUseCase;
        this.addMediaToCollectionUseCase      = addMediaToCollectionUseCase;
        this.removeMediaFromCollectionUseCase = removeMediaFromCollectionUseCase;
        this.deleteCollectionUseCase          = deleteCollectionUseCase;
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<Collection> create(@RequestBody CreateCollectionUseCase.Input input) {
        String userId = getCurrentUserId();
        Collection created = createCollectionUseCase.execute(
                new CreateCollectionUseCase.Input(
                        userId,
                        input.name(),
                        input.description(),
                        input.isPublic()
                )
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Collection> getById(@PathVariable String id) {
        return ResponseEntity.ok(getCollectionUseCase.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Collection>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(getCollectionUseCase.getByUserId(userId));
    }
    @GetMapping("/public")
    public ResponseEntity<List<Collection>> getAllPublic() {
        return ResponseEntity.ok(getCollectionUseCase.getAllPublic());
    }

    @GetMapping("/user/{userId}/public")
    public ResponseEntity<List<Collection>> getPublicByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(getCollectionUseCase.getPublicByUserId(userId));
    }

    @PostMapping("/{collectionId}/media/{mediaId}")
    public ResponseEntity<Collection> addMedia(
            @PathVariable String collectionId,
            @PathVariable String mediaId) {
        String userId = getCurrentUserId();
        Collection updated = addMediaToCollectionUseCase.execute(
                new AddMediaToCollectionUseCase.Input(userId, collectionId, mediaId)
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable String id) {
        deleteCollectionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{collectionId}/media/{mediaId}")
    public ResponseEntity<Collection> removeMedia(
            @PathVariable String collectionId,
            @PathVariable String mediaId) {
        String userId = getCurrentUserId();
        Collection updated = removeMediaFromCollectionUseCase.execute(
                new RemoveMediaFromCollectionUseCase.Input(userId, collectionId, mediaId)
        );
        return ResponseEntity.ok(updated);
    }
}