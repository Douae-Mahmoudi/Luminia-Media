package com.mediatheque.media_svc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalMediaResponse {
    private String title;
    private String author;
    private String genre;
    private Integer releaseYear;
    private String description;
    private String coverUrl;
    private String readUrl;
    private String source;
    private String externalId;
}
