package com.collection.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAddedEvent {
    private Long mediaId;
    private Long ownerId;
    private Long commentedByUserId;
    private String commentedByUsername;
    private String mediaTitle;
    private String commentContent;
}