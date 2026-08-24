package com.collection.mapper;

import com.collection.domain.Favorite;
import com.collection.dto.request.FavoriteRequest;
import com.collection.dto.response.FavoriteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FavoriteMapper")
class FavoriteMapperTest {


    private static final String USER_ID  = "user-123";
    private static final String MEDIA_ID = "media-456";

    private Favorite buildDomain() {
        return new Favorite("fav-001", USER_ID, MEDIA_ID);
    }

    private FavoriteRequest buildRequest(String mediaId) {
        FavoriteRequest request = mock(FavoriteRequest.class);
        when(request.getMediaId()).thenReturn(mediaId);
        return request;
    }


    @Nested
    @DisplayName("toDomain(FavoriteRequest request, String userId)")
    class ToDomainFromRequest {

        private FavoriteRequest request;

        @BeforeEach
        void setUp() {
            request = buildRequest(MEDIA_ID);
        }

        @Test
        @DisplayName("doit mapper mediaId depuis la requête")
        void shouldMapMediaId() {
            Favorite result = FavoriteMapper.toDomain(request, USER_ID);

            assertThat(result.getMediaId()).isEqualTo(MEDIA_ID);
        }

        @Test
        @DisplayName("doit utiliser le userId fourni")
        void shouldUseProvidedUserId() {
            Favorite result = FavoriteMapper.toDomain(request, USER_ID);

            assertThat(result.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("doit générer un ID non nul et non vide")
        void shouldGenerateNonNullId() {
            Favorite result = FavoriteMapper.toDomain(request, USER_ID);

            assertThat(result.getId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("doit générer un ID au format UUID valide")
        void shouldGenerateValidUuidFormat() {
            Favorite result = FavoriteMapper.toDomain(request, USER_ID);

            assertThat(result.getId())
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("doit générer un ID unique à chaque appel")
        void shouldGenerateUniqueId() {
            Favorite r1 = FavoriteMapper.toDomain(request, USER_ID);
            Favorite r2 = FavoriteMapper.toDomain(request, USER_ID);

            assertThat(r1.getId()).isNotEqualTo(r2.getId());
        }

        @Test
        @DisplayName("favoritedAt doit être initialisé par le constructeur")
        void shouldInitializeFavoritedAt() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            Favorite result = FavoriteMapper.toDomain(request, USER_ID);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getFavoritedAt()).isBetween(before, after);
        }
    }



    @Nested
    @DisplayName("toResponse(Favorite domain)")
    class ToResponse {

        @Test
        @DisplayName("doit mapper tous les champs depuis le domaine")
        void shouldMapAllFields() {
            Favorite domain = buildDomain();

            FavoriteResponse result = FavoriteMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(domain.getId()),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID)
            );
        }

        @Test
        @DisplayName("doit mapper favoritedAt identique à celui du domaine")
        void shouldMapFavoritedAt() {
            Favorite domain = buildDomain();

            FavoriteResponse result = FavoriteMapper.toResponse(domain);

            assertThat(result.getFavoritedAt())
                    .isNotNull()
                    .isEqualTo(domain.getFavoritedAt());
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Favorite domain = buildDomain();

            FavoriteResponse r1 = FavoriteMapper.toResponse(domain);
            FavoriteResponse r2 = FavoriteMapper.toResponse(domain);

            assertThat(r1).isNotSameAs(r2);
        }
    }



    @Nested
    @DisplayName("Validations de Favorite propagées au mapper")
    class DomainValidation {

        @Test
        @DisplayName("toDomain doit lever IllegalArgumentException si userId est null")
        void shouldThrowIfUserIdNull() {
            FavoriteRequest request = buildRequest(MEDIA_ID);

            assertThrows(IllegalArgumentException.class,
                    () -> FavoriteMapper.toDomain(request, null));
        }

        @Test
        @DisplayName("toDomain doit lever IllegalArgumentException si userId est blanc")
        void shouldThrowIfUserIdBlank() {
            FavoriteRequest request = buildRequest(MEDIA_ID);

            assertThrows(IllegalArgumentException.class,
                    () -> FavoriteMapper.toDomain(request, "   "));
        }

        @Test
        @DisplayName("toDomain doit lever IllegalArgumentException si mediaId est null")
        void shouldThrowIfMediaIdNull() {
            FavoriteRequest request = buildRequest(null);

            assertThrows(IllegalArgumentException.class,
                    () -> FavoriteMapper.toDomain(request, USER_ID));
        }

        @Test
        @DisplayName("toDomain doit lever IllegalArgumentException si mediaId est blanc")
        void shouldThrowIfMediaIdBlank() {
            FavoriteRequest request = buildRequest("  ");

            assertThrows(IllegalArgumentException.class,
                    () -> FavoriteMapper.toDomain(request, USER_ID));
        }
    }



    @Nested
    @DisplayName("Roundtrip toDomain → toResponse")
    class Roundtrip {

        @Test
        @DisplayName("toResponse doit être cohérent avec le domaine issu de toDomain")
        void shouldBeConsistentAfterRoundtrip() {
            FavoriteRequest request = buildRequest(MEDIA_ID);

            Favorite domain         = FavoriteMapper.toDomain(request, USER_ID);
            FavoriteResponse result = FavoriteMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(domain.getId()),
                    () -> assertThat(result.getUserId()).isEqualTo(domain.getUserId()),
                    () -> assertThat(result.getMediaId()).isEqualTo(domain.getMediaId()),
                    () -> assertThat(result.getFavoritedAt()).isEqualTo(domain.getFavoritedAt())
            );
        }
    }
}
