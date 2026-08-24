package com.example.notification.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponse Tests")
class PageResponseTest {


    @Test
    @DisplayName("Builder crée un PageResponse avec tous les champs")
    void builder_shouldCreatePageResponseWithAllFields() {
        List<String> content = List.of("item1", "item2", "item3");

        PageResponse<String> response = PageResponse.<String>builder()
                .content(content)
                .currentPage(0)
                .totalPages(5)
                .totalElements(50L)
                .last(false)
                .build();

        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(50L);
        assertThat(response.isLast()).isFalse();
    }

    @Test
    @DisplayName("NoArgsConstructor crée un objet vide")
    void noArgsConstructor_shouldCreateEmptyResponse() {
        PageResponse<String> response = new PageResponse<>();

        assertThat(response.getContent()).isNull();
        assertThat(response.getCurrentPage()).isZero();
        assertThat(response.getTotalPages()).isZero();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor initialise tous les champs")
    void allArgsConstructor_shouldSetAllFields() {
        List<Integer> content = List.of(1, 2, 3);

        PageResponse<Integer> response = new PageResponse<>(content, 2, 10, 100L, false);

        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(100L);
        assertThat(response.isLast()).isFalse();
    }


    @Test
    @DisplayName("from() mappe correctement une Page Spring en PageResponse")
    void from_shouldMapPageToPageResponse() {
        List<String> items = List.of("a", "b", "c");
        Page<String> page = new PageImpl<>(items, PageRequest.of(0, 3), 9);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getContent()).isEqualTo(items);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getTotalElements()).isEqualTo(9L);
        assertThat(response.isLast()).isFalse();
    }

    @Test
    @DisplayName("from() retourne last=true sur la dernière page")
    void from_shouldReturnLastTrueOnLastPage() {
        List<String> items = List.of("x", "y");
        Page<String> page = new PageImpl<>(items, PageRequest.of(2, 3), 8);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.isLast()).isTrue();
        assertThat(response.getCurrentPage()).isEqualTo(2);
    }

    @Test
    @DisplayName("from() fonctionne avec une page vide")
    void from_shouldHandleEmptyPage() {
        Page<String> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> response = PageResponse.from(emptyPage);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getTotalPages()).isZero();
        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("from() fonctionne avec une seule page")
    void from_shouldHandleSinglePage() {
        List<String> items = List.of("only");
        Page<String> page = new PageImpl<>(items, PageRequest.of(0, 10), 1);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("from() fonctionne avec un type générique NotificationResponse")
    void from_shouldWorkWithNotificationResponseType() {
        NotificationResponse notif = NotificationResponse.builder()
                .id(1L).message("test").build();
        Page<NotificationResponse> page = new PageImpl<>(
                List.of(notif), PageRequest.of(0, 20), 1
        );

        PageResponse<NotificationResponse> response = PageResponse.from(page);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
    }



    @Test
    @DisplayName("Deux PageResponse identiques sont égaux")
    void equals_shouldReturnTrueForIdenticalResponses() {
        List<String> content = List.of("a", "b");

        PageResponse<String> r1 = PageResponse.<String>builder()
                .content(content).currentPage(0).totalPages(2)
                .totalElements(4L).last(false).build();

        PageResponse<String> r2 = PageResponse.<String>builder()
                .content(content).currentPage(0).totalPages(2)
                .totalElements(4L).last(false).build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("Deux PageResponse différents ne sont pas égaux")
    void equals_shouldReturnFalseForDifferentResponses() {
        PageResponse<String> r1 = PageResponse.<String>builder()
                .content(List.of("a")).currentPage(0).totalPages(1)
                .totalElements(1L).last(true).build();

        PageResponse<String> r2 = PageResponse.<String>builder()
                .content(List.of("b", "c")).currentPage(1).totalPages(3)
                .totalElements(9L).last(false).build();

        assertThat(r1).isNotEqualTo(r2);
    }
}
