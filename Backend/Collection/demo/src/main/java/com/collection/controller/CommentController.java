package com.collection.controller;

import com.collection.domain.Comment;
import com.collection.usecase.comment.AddCommentUseCase;
import com.collection.usecase.comment.DeleteCommentUseCase;
import com.collection.usecase.comment.GetCommentsByMediaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final AddCommentUseCase         addCommentUseCase;
    private final DeleteCommentUseCase      deleteCommentUseCase;
    private final GetCommentsByMediaUseCase getCommentsByMediaUseCase;

    public CommentController(AddCommentUseCase addCommentUseCase,
                             DeleteCommentUseCase deleteCommentUseCase,
                             GetCommentsByMediaUseCase getCommentsByMediaUseCase) {
        this.addCommentUseCase         = addCommentUseCase;
        this.deleteCommentUseCase      = deleteCommentUseCase;
        this.getCommentsByMediaUseCase = getCommentsByMediaUseCase;
    }


    @GetMapping
    public ResponseEntity<List<Comment>> getByMedia(
            @RequestParam String mediaId) {
        return ResponseEntity.ok(getCommentsByMediaUseCase.execute(mediaId));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Numeric-Id", defaultValue = "0") String numericUserId,
            @RequestHeader(value = "X-Username",    defaultValue = "unknown") String username,
            @RequestHeader(value = "X-Owner-Id",    defaultValue = "0")       String ownerId,
            @RequestHeader(value = "X-Media-Title", defaultValue = "Média")   String mediaTitle,
            @RequestBody AddCommentUseCase.Input input) {
        Comment comment = addCommentUseCase.execute(
                new AddCommentUseCase.Input(
                        userId, input.mediaId(), input.content(),
                        username, ownerId, mediaTitle, numericUserId
                )
        );
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String commentId) {
        deleteCommentUseCase.execute(commentId, userId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/external")
    public ResponseEntity<List<Comment>> getByExternalMedia(
            @RequestParam String externalKey) {
        return ResponseEntity.ok(getCommentsByMediaUseCase.execute(externalKey));
    }

    @PostMapping("/external")
    public ResponseEntity<Comment> addExternalComment(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Numeric-Id", defaultValue = "0") String numericUserId,
            @RequestHeader(value = "X-Username",    defaultValue = "unknown") String username,
            @RequestHeader(value = "X-Owner-Id",    defaultValue = "0")       String ownerId,
            @RequestHeader(value = "X-Media-Title", defaultValue = "Média")   String mediaTitle,
            @RequestBody AddCommentUseCase.Input input) {
        Comment comment = addCommentUseCase.execute(
                new AddCommentUseCase.Input(
                        userId, input.mediaId(), input.content(),
                        username, ownerId, mediaTitle, numericUserId
                )
        );
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/external/{commentId}")
    public ResponseEntity<Void> deleteExternalComment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String commentId) {
        deleteCommentUseCase.execute(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}