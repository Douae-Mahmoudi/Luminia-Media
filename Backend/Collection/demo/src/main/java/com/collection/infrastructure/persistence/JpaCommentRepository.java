package com.collection.infrastructure.persistence;

import com.collection.infrastructure.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCommentRepository extends JpaRepository<CommentEntity, String> {
    List<CommentEntity> findByMediaId(String mediaId);
    List<CommentEntity> findByUserId(String userId);

}