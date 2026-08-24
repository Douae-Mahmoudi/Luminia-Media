package com.collection.controller;

import com.collection.domain.Like;
import com.collection.usecase.like.GetLikesCountUseCase;
import com.collection.usecase.like.GetUserLikeStatusUseCase;
import com.collection.usecase.like.LikeMediaUseCase;
import com.collection.usecase.like.UnlikeMediaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeMediaUseCase likeMediaUseCase;
    private final UnlikeMediaUseCase unlikeMediaUseCase;
    private final GetLikesCountUseCase getLikesCountUseCase;
    private final GetUserLikeStatusUseCase getUserLikeStatusUseCase;

    public LikeController(LikeMediaUseCase likeMediaUseCase,
                          UnlikeMediaUseCase unlikeMediaUseCase,
                          GetLikesCountUseCase getLikesCountUseCase,
                          GetUserLikeStatusUseCase getUserLikeStatusUseCase) {
        this.likeMediaUseCase = likeMediaUseCase;
        this.unlikeMediaUseCase = unlikeMediaUseCase;
        this.getLikesCountUseCase = getLikesCountUseCase;
        this.getUserLikeStatusUseCase = getUserLikeStatusUseCase;
    }


    @PostMapping("/{mediaId}")
    public ResponseEntity<Like> like(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Numeric-Id", defaultValue = "0") String numericUserId,
            @RequestHeader(value = "X-Username", defaultValue = "unknown") String username,
            @RequestHeader(value = "X-Media-Title", defaultValue = "Média") String mediaTitle,
            @PathVariable String mediaId) {

        Like like = likeMediaUseCase.execute(
                new LikeMediaUseCase.Input(userId, mediaId, username, mediaTitle, numericUserId)
        );
        return ResponseEntity.ok(like);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> unlike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String mediaId) {
        unlikeMediaUseCase.execute(userId, mediaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{mediaId}/count")
    public ResponseEntity<Map<String, Long>> getCount(@PathVariable String mediaId) {
        long count = getLikesCountUseCase.execute(mediaId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/{mediaId}/user")
    public ResponseEntity<Map<String, Boolean>> getUserStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String mediaId) {
        boolean liked = getUserLikeStatusUseCase.execute(userId, mediaId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }


    @PostMapping("/external/{externalKey}")
    public ResponseEntity<Like> likeExternal(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Numeric-Id", defaultValue = "0") String numericUserId,
            @RequestHeader(value = "X-Username", defaultValue = "unknown") String username,
            @RequestHeader(value = "X-Media-Title", defaultValue = "Média") String mediaTitle,
            @PathVariable String externalKey) {

        Like like = likeMediaUseCase.execute(
                new LikeMediaUseCase.Input(userId, externalKey, username, mediaTitle, numericUserId)
        );
        return ResponseEntity.ok(like);
    }

    @DeleteMapping("/external/{externalKey}")
    public ResponseEntity<Void> unlikeExternal(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String externalKey) {
        unlikeMediaUseCase.execute(userId, externalKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/external/{externalKey}/count")
    public ResponseEntity<Map<String, Long>> getExternalCount(
            @PathVariable String externalKey) {
        long count = getLikesCountUseCase.execute(externalKey);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/external/{externalKey}/user")
    public ResponseEntity<Map<String, Boolean>> getExternalUserStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String externalKey) {
        boolean liked = getUserLikeStatusUseCase.execute(userId, externalKey);
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}