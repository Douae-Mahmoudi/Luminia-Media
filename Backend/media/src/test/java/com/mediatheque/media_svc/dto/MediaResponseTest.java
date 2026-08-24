package com.mediatheque.media_svc.dto;

import com.mediatheque.media_svc.model.MediaStatus;
import com.mediatheque.media_svc.model.MediaType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MediaResponseTest {

    private MediaResponse buildFullResponse() {
        return MediaResponse.builder()
                .id(1L)
                .title("Mon média")
                .author("John Doe")
                .description("Une description")
                .type(MediaType.BOOK)
                .status(MediaStatus.AVAILABLE)
                .releaseYear(2024)
                .genre("Fiction")
                .imageUrl("https://example.com/image.jpg")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                .ownerId(824036515L)
                .contentUrl("https://example.com/content.mp4")
                .ownerUsername("johndoe")
                .build();
    }


    @Test
    void builder_shouldSetAllFields() {
        MediaResponse response = buildFullResponse();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Mon média");
        assertThat(response.getAuthor()).isEqualTo("John Doe");
        assertThat(response.getDescription()).isEqualTo("Une description");
        assertThat(response.getType()).isEqualTo(MediaType.BOOK);
        assertThat(response.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        assertThat(response.getReleaseYear()).isEqualTo(2024);
        assertThat(response.getGenre()).isEqualTo("Fiction");
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
        assertThat(response.getOwnerId()).isEqualTo(824036515L);
        assertThat(response.getContentUrl()).isEqualTo("https://example.com/content.mp4");
        assertThat(response.getOwnerUsername()).isEqualTo("johndoe");
    }

    @Test
    void builder_shouldAllowNullOptionalFields() {
        MediaResponse response = MediaResponse.builder()
                .id(1L)
                .title("Mon média")
                .author("John Doe")
                .type(MediaType.BOOK)
                .status(MediaStatus.PENDING)
                .build();

        assertThat(response.getDescription()).isNull();
        assertThat(response.getReleaseYear()).isNull();
        assertThat(response.getGenre()).isNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
        assertThat(response.getOwnerId()).isNull();
        assertThat(response.getContentUrl()).isNull();
        assertThat(response.getOwnerUsername()).isNull();
    }



    @Test
    void noArgsConstructor_shouldCreateEmptyResponse() {
        MediaResponse response = new MediaResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getTitle()).isNull();
        assertThat(response.getAuthor()).isNull();
        assertThat(response.getOwnerId()).isNull();
        assertThat(response.getContentUrl()).isNull();
        assertThat(response.getOwnerUsername()).isNull();
    }


    @Test
    void allArgsConstructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        MediaResponse response = new MediaResponse(
                1L, "Mon média", "John Doe", "Description",
                MediaType.BOOK, MediaStatus.AVAILABLE,
                2024, "Fiction", "https://example.com/image.jpg",
                now, now, 824036515L,
                "https://example.com/content.mp4",
                "johndoe"
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Mon média");
        assertThat(response.getOwnerId()).isEqualTo(824036515L);
        assertThat(response.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        assertThat(response.getContentUrl()).isEqualTo("https://example.com/content.mp4");
        assertThat(response.getOwnerUsername()).isEqualTo("johndoe");
    }


    @Test
    void setters_shouldUpdateFields() {
        MediaResponse response = new MediaResponse();
        response.setId(2L);
        response.setTitle("Nouveau titre");
        response.setStatus(MediaStatus.REJECTED);
        response.setOwnerId(999L);
        response.setContentUrl("https://example.com/new-content.mp4");
        response.setOwnerUsername("newuser");

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getTitle()).isEqualTo("Nouveau titre");
        assertThat(response.getStatus()).isEqualTo(MediaStatus.REJECTED);
        assertThat(response.getOwnerId()).isEqualTo(999L);
        assertThat(response.getContentUrl()).isEqualTo("https://example.com/new-content.mp4");
        assertThat(response.getOwnerUsername()).isEqualTo("newuser");
    }


    @Test
    void equals_shouldReturnTrue_whenSameFields() {
        MediaResponse r1 = buildFullResponse();
        MediaResponse r2 = buildFullResponse();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        MediaResponse r1 = buildFullResponse();
        MediaResponse r2 = buildFullResponse();
        r2.setId(99L);

        assertThat(r1).isNotEqualTo(r2);
    }


    @Test
    void toString_shouldContainKeyFields() {
        MediaResponse response = buildFullResponse();
        String str = response.toString();

        assertThat(str).contains("Mon média");
        assertThat(str).contains("John Doe");
        assertThat(str).contains("AVAILABLE");
        assertThat(str).contains("824036515");
        assertThat(str).contains("johndoe");
    }


    @Test
    void status_shouldSupportAllValues() {
        for (MediaStatus status : MediaStatus.values()) {
            MediaResponse response = MediaResponse.builder()
                    .status(status)
                    .build();
            assertThat(response.getStatus()).isEqualTo(status);
        }
    }


    @Test
    void type_shouldSupportAllValues() {
        for (MediaType type : MediaType.values()) {
            MediaResponse response = MediaResponse.builder()
                    .type(type)
                    .build();
            assertThat(response.getType()).isEqualTo(type);
        }
    }
}