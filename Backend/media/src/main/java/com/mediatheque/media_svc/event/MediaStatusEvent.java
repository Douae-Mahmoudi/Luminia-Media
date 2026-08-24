package com.mediatheque.media_svc.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MediaStatusEvent {
    private Long mediaId;
    private String mediaTitle;
    private Long ownerId;
    private String status;
    private String reason;
}