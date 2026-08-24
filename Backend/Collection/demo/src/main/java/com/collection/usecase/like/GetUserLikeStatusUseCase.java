package com.collection.usecase.like;

import com.collection.repository.LikeRepository;
import org.springframework.stereotype.Service;

@Service
public class GetUserLikeStatusUseCase {

    private final LikeRepository likeRepository;

    public GetUserLikeStatusUseCase(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public boolean execute(String userId, String mediaId) {
        return likeRepository.existsByUserIdAndMediaId(userId, mediaId);
    }
}















