package com.collection.mapper;

import com.collection.domain.Like;
import com.collection.dto.request.LikeRequest;
import com.collection.dto.response.LikeResponse;
import com.collection.infrastructure.persistence.entity.LikeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LikeMapper")
class LikeMapperTest {


    private static final String ID       = "like-001";
    private static final String USER_ID  = "user-123";
    private static final String MEDIA_ID = "media-456";


    private LikeEntity buildEntity() {
        LikeEntity entity = new LikeEntity();
        entity.setId(ID);
        entity.setUserId(USER_ID);
        entity.setMediaId(MEDIA_ID);
        entity.setCreatedAt(LocalDateTime.of(2024, 5, 1, 12, 0));
        entity.setType(LikeEntity.LikeType.LIKE);
        return entity;
    }


    private Like buildDomain() {
        return new Like(ID, USER_ID, MEDIA_ID);
    }

    private LikeRequest buildRequest(String mediaId) {
        LikeRequest request = mock(LikeRequest.class);
        when(request.getMediaId()).thenReturn(mediaId);
        return request;
    }


    @Nested
    @DisplayName("toDomain(LikeEntity entity)")
    class ToDomainFromEntity {

        @Test
        @DisplayName("doit mapper id, userId et mediaId depuis l'entity")
        void shouldMapAllFields() {
            LikeEntity entity = buildEntity();

            Like result = LikeMapper.toDomain(entity);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID)
            );
        }

        @Test
        @DisplayName("likedAt doit être initialisé par le constructeur du domaine (pas depuis l'entity)")
        void shouldInitializeLikedAtFromConstructor() {
            LikeEntity entity = buildEntity();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            Like result = LikeMapper.toDomain(entity);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            // Le constructeur de Like appelle LocalDateTime.now() → likedAt récent
            assertThat(result.getLikedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            LikeEntity entity = buildEntity();

            Like r1 = LikeMapper.toDomain(entity);
            Like r2 = LikeMapper.toDomain(entity);

            assertThat(r1).isNotSameAs(r2);
        }
    }


    @Nested
    @DisplayName("toEntity(Like domain)")
    class ToEntity {

        @Test
        @DisplayName("doit mapper id, userId et mediaId depuis le domaine")
        void shouldMapAllFields() {
            Like domain = buildDomain();

            LikeEntity result = LikeMapper.toEntity(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID)
            );
        }

        @Test
        @DisplayName("createdAt de l'entity doit correspondre à likedAt du domaine")
        void shouldMapLikedAtToEntityCreatedAt() {
            Like domain = buildDomain();

            LikeEntity result = LikeMapper.toEntity(domain);

            assertThat(result.getCreatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getLikedAt());
        }

        @Test
        @DisplayName("le type doit être forcé à LIKE par défaut")
        void shouldSetDefaultTypeLike() {
            Like domain = buildDomain();

            LikeEntity result = LikeMapper.toEntity(domain);

            assertThat(result.getType()).isEqualTo(LikeEntity.LikeType.LIKE);
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Like domain = buildDomain();

            LikeEntity r1 = LikeMapper.toEntity(domain);
            LikeEntity r2 = LikeMapper.toEntity(domain);

            assertThat(r1).isNotSameAs(r2);
        }
    }



    @Nested
    @DisplayName("toDomain(LikeRequest request, String userId)")
    class ToDomainFromRequest {

        private LikeRequest request;

        @BeforeEach
        void setUp() {
            request = buildRequest(MEDIA_ID);
        }

        @Test
        @DisplayName("doit mapper mediaId depuis la requête")
        void shouldMapMediaId() {
            Like result = LikeMapper.toDomain(request, USER_ID);

            assertThat(result.getMediaId()).isEqualTo(MEDIA_ID);
        }

        @Test
        @DisplayName("doit utiliser le userId fourni")
        void shouldUseProvidedUserId() {
            Like result = LikeMapper.toDomain(request, USER_ID);

            assertThat(result.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("doit générer un ID non nul et non vide")
        void shouldGenerateNonNullId() {
            Like result = LikeMapper.toDomain(request, USER_ID);

            assertThat(result.getId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("doit générer un ID au format UUID valide")
        void shouldGenerateValidUuidFormat() {
            Like result = LikeMapper.toDomain(request, USER_ID);

            assertThat(result.getId())
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("doit générer un ID unique à chaque appel")
        void shouldGenerateUniqueId() {
            Like r1 = LikeMapper.toDomain(request, USER_ID);
            Like r2 = LikeMapper.toDomain(request, USER_ID);

            assertThat(r1.getId()).isNotEqualTo(r2.getId());
        }

        @Test
        @DisplayName("likedAt doit être initialisé par le constructeur")
        void shouldInitializeLikedAt() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            Like result = LikeMapper.toDomain(request, USER_ID);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getLikedAt()).isBetween(before, after);
        }
    }



    @Nested
    @DisplayName("toResponse(Like domain)")
    class ToResponse {

        @Test
        @DisplayName("doit mapper tous les champs depuis le domaine")
        void shouldMapAllFields() {
            Like domain = buildDomain();

            LikeResponse result = LikeMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID)
            );
        }

        @Test
        @DisplayName("doit mapper likedAt identique à celui du domaine")
        void shouldMapLikedAt() {
            Like domain = buildDomain();

            LikeResponse result = LikeMapper.toResponse(domain);

            assertThat(result.getLikedAt())
                    .isNotNull()
                    .isEqualTo(domain.getLikedAt());
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Like domain = buildDomain();

            LikeResponse r1 = LikeMapper.toResponse(domain);
            LikeResponse r2 = LikeMapper.toResponse(domain);

            assertThat(r1).isNotSameAs(r2);
        }
    }



    @Nested
    @DisplayName("Validations de Like propagées au mapper")
    class DomainValidation {

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si userId est null")
        void toDomainFromEntity_shouldThrowIfUserIdNull() {
            LikeEntity entity = buildEntity();
            entity.setUserId(null);

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si userId est blanc")
        void toDomainFromEntity_shouldThrowIfUserIdBlank() {
            LikeEntity entity = buildEntity();
            entity.setUserId("  ");

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si mediaId est null")
        void toDomainFromEntity_shouldThrowIfMediaIdNull() {
            LikeEntity entity = buildEntity();
            entity.setMediaId(null);

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si mediaId est blanc")
        void toDomainFromEntity_shouldThrowIfMediaIdBlank() {
            LikeEntity entity = buildEntity();
            entity.setMediaId("   ");

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Request) doit lever IllegalArgumentException si userId est null")
        void toDomainFromRequest_shouldThrowIfUserIdNull() {
            LikeRequest request = buildRequest(MEDIA_ID);

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(request, null));
        }

        @Test
        @DisplayName("toDomain(Request) doit lever IllegalArgumentException si userId est blanc")
        void toDomainFromRequest_shouldThrowIfUserIdBlank() {
            LikeRequest request = buildRequest(MEDIA_ID);

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(request, "   "));
        }

        @Test
        @DisplayName("toDomain(Request) doit lever IllegalArgumentException si mediaId est null")
        void toDomainFromRequest_shouldThrowIfMediaIdNull() {
            LikeRequest request = buildRequest(null);

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(request, USER_ID));
        }

        @Test
        @DisplayName("toDomain(Request) doit lever IllegalArgumentException si mediaId est blanc")
        void toDomainFromRequest_shouldThrowIfMediaIdBlank() {
            LikeRequest request = buildRequest("  ");

            assertThrows(IllegalArgumentException.class,
                    () -> LikeMapper.toDomain(request, USER_ID));
        }
    }



    @Nested
    @DisplayName("Roundtrip")
    class Roundtrip {

        @Test
        @DisplayName("Entity → Domain → Entity : champs métier préservés")
        void entityToDomainToEntity_shouldPreserveBusinessFields() {
            LikeEntity original = buildEntity();

            Like domain       = LikeMapper.toDomain(original);
            LikeEntity result = LikeMapper.toEntity(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(original.getId()),
                    () -> assertThat(result.getUserId()).isEqualTo(original.getUserId()),
                    () -> assertThat(result.getMediaId()).isEqualTo(original.getMediaId()),
                    () -> assertThat(result.getType()).isEqualTo(LikeEntity.LikeType.LIKE)
            );
        }

        @Test
        @DisplayName("toDomain(Request) → toEntity → toResponse : cohérence complète")
        void requestToDomainToEntityToResponse_shouldBeConsistent() {
            LikeRequest request = buildRequest(MEDIA_ID);

            Like domain         = LikeMapper.toDomain(request, USER_ID);
            LikeEntity entity   = LikeMapper.toEntity(domain);
            LikeResponse response = LikeMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(entity.getId()).isEqualTo(domain.getId()),
                    () -> assertThat(response.getId()).isEqualTo(domain.getId()),
                    () -> assertThat(entity.getUserId()).isEqualTo(response.getUserId()),
                    () -> assertThat(entity.getMediaId()).isEqualTo(response.getMediaId()),
                    () -> assertThat(entity.getCreatedAt()).isEqualTo(response.getLikedAt()),
                    () -> assertThat(entity.getType()).isEqualTo(LikeEntity.LikeType.LIKE)
            );
        }
    }
}
