package com.collection.usecase.like;

import com.collection.repository.LikeRepository;
import org.springframework.stereotype.Service;

@Service
public class GetLikesCountUseCase {

    private final LikeRepository likeRepository;

    public GetLikesCountUseCase(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public long execute(String mediaId) {
        return likeRepository.countByMediaId(mediaId);
    }
}
