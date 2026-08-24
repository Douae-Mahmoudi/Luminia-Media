package com.collection.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaLikedEvent {
    private Long mediaId;
    private Long ownerId;
    private Long likedByUserId;
    private String likedByUsername;
    private String mediaTitle;
}