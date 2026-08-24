package com.collection.usecase.like;

import com.collection.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnlikeMediaUseCase {

    private final LikeRepository likeRepository;

    public UnlikeMediaUseCase(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Transactional
    public void execute(String userId, String mediaId) {
        if (!likeRepository.existsByUserIdAndMediaId(userId, mediaId))
            throw new RuntimeException("Like not found");

        likeRepository.deleteByUserIdAndMediaId(userId, mediaId);
    }
}