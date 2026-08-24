package com.collection.mapper;

import com.collection.domain.Collection;
import com.collection.dto.request.CollectionRequest;
import com.collection.dto.response.CollectionResponse;
import com.collection.infrastructure.persistence.entity.CollectionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CollectionMapper")
class CollectionMapperTest {


    private static final String ID          = "col-123";
    private static final String USER_ID     = "user-456";
    private static final String NAME        = "Ma collection";
    private static final String DESCRIPTION = "Une description";
    private static final boolean IS_PUBLIC  = true;
    private static final List<String> MEDIA_IDS = Arrays.asList("media-1", "media-2");


    private CollectionEntity buildEntity(List<String> mediaIds) {
        CollectionEntity entity = new CollectionEntity();
        entity.setId(ID);
        entity.setUserId(USER_ID);
        entity.setName(NAME);
        entity.setDescription(DESCRIPTION);
        entity.setPublic(IS_PUBLIC);
        entity.setMediaIds(mediaIds);
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 12, 0));
        return entity;
    }


    private Collection buildDomain(List<String> mediaIds) {
        Collection collection = new Collection(ID, USER_ID, NAME, DESCRIPTION, IS_PUBLIC);
        if (mediaIds != null) {
            mediaIds.forEach(collection::addMedia);
        }
        return collection;
    }



    @Nested
    @DisplayName("toDomain(CollectionEntity entity)")
    class ToDomainFromEntity {

        @Test
        @DisplayName("doit mapper tous les champs de base")
        void shouldMapAllBaseFields() {
            CollectionEntity entity = buildEntity(MEDIA_IDS);

            Collection result = CollectionMapper.toDomain(entity);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getName()).isEqualTo(NAME),
                    () -> assertThat(result.getDescription()).isEqualTo(DESCRIPTION),
                    () -> assertThat(result.isPublic()).isEqualTo(IS_PUBLIC)
            );
        }

        @Test
        @DisplayName("doit ajouter tous les mediaIds via addMedia")
        void shouldAddAllMediaIds() {
            CollectionEntity entity = buildEntity(MEDIA_IDS);

            Collection result = CollectionMapper.toDomain(entity);

            assertThat(result.getMediaIds())
                    .containsExactlyInAnyOrderElementsOf(MEDIA_IDS);
        }

        @Test
        @DisplayName("doit gérer mediaIds null sans lever d'exception")
        void shouldHandleNullMediaIds() {
            CollectionEntity entity = buildEntity(null);

            assertDoesNotThrow(() -> {
                Collection result = CollectionMapper.toDomain(entity);
                assertThat(result.getMediaIds()).isEmpty();
            });
        }

        @Test
        @DisplayName("doit gérer une liste de mediaIds vide")
        void shouldHandleEmptyMediaIds() {
            CollectionEntity entity = buildEntity(Collections.emptyList());

            Collection result = CollectionMapper.toDomain(entity);

            assertThat(result.getMediaIds()).isEmpty();
        }

        @Test
        @DisplayName("doit mapper isPublic=false correctement")
        void shouldMapPublicFalse() {
            CollectionEntity entity = buildEntity(MEDIA_IDS);
            entity.setPublic(false);

            Collection result = CollectionMapper.toDomain(entity);

            assertThat(result.isPublic()).isFalse();
        }

        @Test
        @DisplayName("addMedia étant idempotent, les doublons ne doivent pas être dupliqués")
        void shouldNotDuplicateMediaId() {
            List<String> withDuplicate = Arrays.asList("media-1", "media-1", "media-2");
            CollectionEntity entity = buildEntity(withDuplicate);

            Collection result = CollectionMapper.toDomain(entity);

            assertThat(result.getMediaIds())
                    .containsExactlyInAnyOrder("media-1", "media-2");
        }
    }


    @Nested
    @DisplayName("toEntity(Collection domain)")
    class ToEntity {

        @Test
        @DisplayName("doit mapper tous les champs de base")
        void shouldMapAllBaseFields() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getName()).isEqualTo(NAME),
                    () -> assertThat(result.getDescription()).isEqualTo(DESCRIPTION),
                    () -> assertThat(result.isPublic()).isEqualTo(IS_PUBLIC)
            );
        }

        @Test
        @DisplayName("doit mapper mediaIds")
        void shouldMapMediaIds() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertThat(result.getMediaIds())
                    .containsExactlyInAnyOrderElementsOf(MEDIA_IDS);
        }

        @Test
        @DisplayName("doit mapper createdAt identique à celui du domaine")
        void shouldMapCreatedAt() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertThat(result.getCreatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getCreatedAt());
        }

        @Test
        @DisplayName("doit mapper updatedAt identique à celui du domaine")
        void shouldMapUpdatedAt() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertThat(result.getUpdatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getUpdatedAt());
        }

        @Test
        @DisplayName("doit mapper isPublic=false correctement")
        void shouldMapPublicFalse() {
            Collection domain = new Collection(ID, USER_ID, NAME, DESCRIPTION, false);

            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertThat(result.isPublic()).isFalse();
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionEntity r1 = CollectionMapper.toEntity(domain);
            CollectionEntity r2 = CollectionMapper.toEntity(domain);

            assertThat(r1).isNotSameAs(r2);
        }

        @Test
        @DisplayName("doit mapper une collection sans mediaIds (liste vide)")
        void shouldMapEmptyMediaIds() {
            Collection domain = buildDomain(Collections.emptyList());

            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertThat(result.getMediaIds()).isEmpty();
        }
    }


    @Nested
    @DisplayName("toDomain(CollectionRequest request, String userId)")
    class ToDomainFromRequest {

        private CollectionRequest request;

        @BeforeEach
        void setUp() {
            request = mock(CollectionRequest.class);
            when(request.getName()).thenReturn(NAME);
            when(request.getDescription()).thenReturn(DESCRIPTION);
            when(request.isPublic()).thenReturn(IS_PUBLIC);
        }

        @Test
        @DisplayName("doit mapper le nom, la description et isPublic depuis la requête")
        void shouldMapRequestFields() {
            Collection result = CollectionMapper.toDomain(request, USER_ID);

            assertAll(
                    () -> assertThat(result.getName()).isEqualTo(NAME),
                    () -> assertThat(result.getDescription()).isEqualTo(DESCRIPTION),
                    () -> assertThat(result.isPublic()).isEqualTo(IS_PUBLIC)
            );
        }

        @Test
        @DisplayName("doit utiliser le userId fourni")
        void shouldUseProvidedUserId() {
            Collection result = CollectionMapper.toDomain(request, USER_ID);

            assertThat(result.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("doit générer un ID non nul et non vide")
        void shouldGenerateNonNullId() {
            Collection result = CollectionMapper.toDomain(request, USER_ID);

            assertThat(result.getId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("doit générer un ID unique à chaque appel")
        void shouldGenerateUniqueId() {
            Collection r1 = CollectionMapper.toDomain(request, USER_ID);
            Collection r2 = CollectionMapper.toDomain(request, USER_ID);

            assertThat(r1.getId()).isNotEqualTo(r2.getId());
        }

        @Test
        @DisplayName("doit générer un ID au format UUID valide")
        void shouldGenerateValidUuidFormat() {
            Collection result = CollectionMapper.toDomain(request, USER_ID);

            assertThat(result.getId())
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("doit mapper isPublic=false depuis la requête")
        void shouldMapPublicFalseFromRequest() {
            when(request.isPublic()).thenReturn(false);

            Collection result = CollectionMapper.toDomain(request, USER_ID);

            assertThat(result.isPublic()).isFalse();
        }

        @Test
        @DisplayName("la liste mediaIds doit être vide à la création")
        void shouldHaveEmptyMediaIdsOnCreation() {
            Collection result = CollectionMapper.toDomain(request, USER_ID);

            assertThat(result.getMediaIds()).isEmpty();
        }

        @Test
        @DisplayName("createdAt et updatedAt doivent être initialisés par le constructeur")
        void shouldInitializeTimestamps() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            Collection result = CollectionMapper.toDomain(request, USER_ID);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getCreatedAt()).isBetween(before, after);
            assertThat(result.getUpdatedAt()).isBetween(before, after);
        }
    }


    @Nested
    @DisplayName("toResponse(Collection domain)")
    class ToResponse {

        @Test
        @DisplayName("doit mapper tous les champs de base")
        void shouldMapAllBaseFields() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionResponse result = CollectionMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getName()).isEqualTo(NAME),
                    () -> assertThat(result.getDescription()).isEqualTo(DESCRIPTION),
                    () -> assertThat(result.isPublic()).isEqualTo(IS_PUBLIC)
            );
        }

        @Test
        @DisplayName("doit mapper mediaIds")
        void shouldMapMediaIds() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionResponse result = CollectionMapper.toResponse(domain);

            assertThat(result.getMediaIds())
                    .containsExactlyInAnyOrderElementsOf(MEDIA_IDS);
        }

        @Test
        @DisplayName("doit mapper createdAt identique à celui du domaine")
        void shouldMapCreatedAt() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionResponse result = CollectionMapper.toResponse(domain);

            assertThat(result.getCreatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getCreatedAt());
        }

        @Test
        @DisplayName("doit mapper updatedAt identique à celui du domaine")
        void shouldMapUpdatedAt() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionResponse result = CollectionMapper.toResponse(domain);

            assertThat(result.getUpdatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getUpdatedAt());
        }

        @Test
        @DisplayName("doit mapper isPublic=false correctement")
        void shouldMapPublicFalse() {
            Collection domain = new Collection(ID, USER_ID, NAME, DESCRIPTION, false);

            CollectionResponse result = CollectionMapper.toResponse(domain);

            assertThat(result.isPublic()).isFalse();
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionResponse r1 = CollectionMapper.toResponse(domain);
            CollectionResponse r2 = CollectionMapper.toResponse(domain);

            assertThat(r1).isNotSameAs(r2);
        }
    }


    @Nested
    @DisplayName("Roundtrip Entity → Domain → Entity")
    class Roundtrip {

        @Test
        @DisplayName("aucune donnée ne doit être perdue lors du roundtrip")
        void entityToDomainToEntity_shouldPreserveAllData() {
            CollectionEntity original = buildEntity(MEDIA_IDS);

            Collection domain       = CollectionMapper.toDomain(original);
            CollectionEntity result = CollectionMapper.toEntity(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(original.getId()),
                    () -> assertThat(result.getUserId()).isEqualTo(original.getUserId()),
                    () -> assertThat(result.getName()).isEqualTo(original.getName()),
                    () -> assertThat(result.getDescription()).isEqualTo(original.getDescription()),
                    () -> assertThat(result.isPublic()).isEqualTo(original.isPublic()),
                    () -> assertThat(result.getMediaIds())
                            .containsExactlyInAnyOrderElementsOf(original.getMediaIds())
            );
        }

        @Test
        @DisplayName("toEntity et toResponse doivent être cohérents entre eux")
        void toEntityAndToResponse_shouldBeConsistent() {
            Collection domain = buildDomain(MEDIA_IDS);

            CollectionEntity   entity   = CollectionMapper.toEntity(domain);
            CollectionResponse response = CollectionMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(response.getId()).isEqualTo(entity.getId()),
                    () -> assertThat(response.getUserId()).isEqualTo(entity.getUserId()),
                    () -> assertThat(response.getName()).isEqualTo(entity.getName()),
                    () -> assertThat(response.getDescription()).isEqualTo(entity.getDescription()),
                    () -> assertThat(response.isPublic()).isEqualTo(entity.isPublic()),
                    () -> assertThat(response.getMediaIds())
                            .containsExactlyInAnyOrderElementsOf(entity.getMediaIds())
            );
        }
    }
}