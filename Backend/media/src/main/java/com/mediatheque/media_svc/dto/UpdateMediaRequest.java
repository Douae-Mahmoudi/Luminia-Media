package com.mediatheque.media_svc.dto;

import com.mediatheque.media_svc.model.MediaType;
import lombok.Data;

@Data
public class UpdateMediaRequest {

    private String title;
    private String author;
    private String description;
    private MediaType type;
    private Integer releaseYear;
    private String genre;
    private String imageUrl;
    private String contentUrl;
}