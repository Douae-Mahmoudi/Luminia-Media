package com.example.notification.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MediaLikedEvent {
    private Long mediaId;
    private String mediaTitle;
    private Long likedByUserId;
    private String likedByUsername;
    private Long ownerId;
}
