package com.mediatheque.media_svc.dto;

import com.mediatheque.media_svc.model.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMediaRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "L'auteur est obligatoire")
    private String author;

    private String description;

    @NotNull(message = "Le type est obligatoire")
    private MediaType type;

    private Integer releaseYear;
    private String genre;
    private String imageUrl;
    private String contentUrl;
    private Long ownerId;
    private String ownerUsername;
}