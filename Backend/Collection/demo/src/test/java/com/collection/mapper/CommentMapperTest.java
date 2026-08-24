package com.collection.mapper;

import com.collection.domain.Comment;
import com.collection.dto.request.CommentRequest;
import com.collection.dto.response.CommentResponse;
import com.collection.infrastructure.persistence.entity.CommentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CommentMapper")
class CommentMapperTest {


    private static final String ID       = "cmt-001";
    private static final String USER_ID  = "user-123";
    private static final String MEDIA_ID = "media-456";
    private static final String CONTENT  = "Super commentaire !";

    private CommentEntity buildEntity(String content) {
        CommentEntity entity = new CommentEntity();
        entity.setId(ID);
        entity.setUserId(USER_ID);
        entity.setMediaId(MEDIA_ID);
        entity.setContent(content);
        entity.setCreatedAt(LocalDateTime.of(2024, 3, 10, 8, 0));
        entity.setUpdatedAt(LocalDateTime.of(2024, 3, 11, 9, 30));
        return entity;
    }

    private Comment buildDomain() {
        return new Comment(ID, USER_ID, MEDIA_ID, CONTENT);
    }



    @Nested
    @DisplayName("toDomain(CommentEntity entity)")
    class ToDomainFromEntity {

        @Test
        @DisplayName("doit mapper tous les champs depuis l'entity")
        void shouldMapAllFields() {
            CommentEntity entity = buildEntity(CONTENT);

            Comment result = CommentMapper.toDomain(entity);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID),
                    () -> assertThat(result.getContent()).isEqualTo(CONTENT)
            );
        }

        @Test
        @DisplayName("doit initialiser createdAt et updatedAt via le constructeur du domaine")
        void shouldInitializeTimestampsFromConstructor() {
            CommentEntity entity = buildEntity(CONTENT);
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            Comment result = CommentMapper.toDomain(entity);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            // Le constructeur de Comment appelle LocalDateTime.now() → timestamps récents
            assertThat(result.getCreatedAt()).isBetween(before, after);
            assertThat(result.getUpdatedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("doit mapper un contenu au maximum autorisé (1000 caractères)")
        void shouldMapMaxLengthContent() {
            String maxContent = "A".repeat(1000);
            CommentEntity entity = buildEntity(maxContent);

            Comment result = CommentMapper.toDomain(entity);

            assertThat(result.getContent()).hasSize(1000);
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            CommentEntity entity = buildEntity(CONTENT);

            Comment r1 = CommentMapper.toDomain(entity);
            Comment r2 = CommentMapper.toDomain(entity);

            assertThat(r1).isNotSameAs(r2);
        }
    }


    @Nested
    @DisplayName("toEntity(Comment domain)")
    class ToEntity {

        @Test
        @DisplayName("doit mapper tous les champs depuis le domaine")
        void shouldMapAllFields() {
            Comment domain = buildDomain();

            CommentEntity result = CommentMapper.toEntity(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID),
                    () -> assertThat(result.getContent()).isEqualTo(CONTENT)
            );
        }

        @Test
        @DisplayName("doit mapper createdAt identique à celui du domaine")
        void shouldMapCreatedAt() {
            Comment domain = buildDomain();

            CommentEntity result = CommentMapper.toEntity(domain);

            assertThat(result.getCreatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getCreatedAt());
        }

        @Test
        @DisplayName("doit mapper updatedAt identique à celui du domaine")
        void shouldMapUpdatedAt() {
            Comment domain = buildDomain();

            CommentEntity result = CommentMapper.toEntity(domain);

            assertThat(result.getUpdatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getUpdatedAt());
        }

        @Test
        @DisplayName("après edit(), updatedAt dans l'entity doit refléter la modification")
        void shouldReflectUpdatedAtAfterEdit() throws InterruptedException {
            Comment domain = buildDomain();
            LocalDateTime createdAt = domain.getCreatedAt();

            Thread.sleep(10); // garantit que updatedAt > createdAt
            domain.edit("Contenu modifié");

            CommentEntity result = CommentMapper.toEntity(domain);

            assertThat(result.getUpdatedAt())
                    .isNotNull()
                    .isAfter(createdAt);
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Comment domain = buildDomain();

            CommentEntity r1 = CommentMapper.toEntity(domain);
            CommentEntity r2 = CommentMapper.toEntity(domain);

            assertThat(r1).isNotSameAs(r2);
        }
    }


    @Nested
    @DisplayName("toDomain(CommentRequest request, String userId)")
    class ToDomainFromRequest {

        private CommentRequest request;

        @BeforeEach
        void setUp() {
            request = mock(CommentRequest.class);
            when(request.getMediaId()).thenReturn(MEDIA_ID);
            when(request.getContent()).thenReturn(CONTENT);
        }

        @Test
        @DisplayName("doit mapper mediaId et content depuis la requête")
        void shouldMapRequestFields() {
            Comment result = CommentMapper.toDomain(request, USER_ID);

            assertAll(
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID),
                    () -> assertThat(result.getContent()).isEqualTo(CONTENT)
            );
        }

        @Test
        @DisplayName("doit utiliser le userId fourni")
        void shouldUseProvidedUserId() {
            Comment result = CommentMapper.toDomain(request, USER_ID);

            assertThat(result.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("doit générer un ID non nul et non vide")
        void shouldGenerateNonNullId() {
            Comment result = CommentMapper.toDomain(request, USER_ID);

            assertThat(result.getId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("doit générer un ID au format UUID valide")
        void shouldGenerateValidUuidFormat() {
            Comment result = CommentMapper.toDomain(request, USER_ID);

            assertThat(result.getId())
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("doit générer un ID unique à chaque appel")
        void shouldGenerateUniqueId() {
            Comment r1 = CommentMapper.toDomain(request, USER_ID);
            Comment r2 = CommentMapper.toDomain(request, USER_ID);

            assertThat(r1.getId()).isNotEqualTo(r2.getId());
        }

        @Test
        @DisplayName("createdAt et updatedAt doivent être initialisés par le constructeur")
        void shouldInitializeTimestamps() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            Comment result = CommentMapper.toDomain(request, USER_ID);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getCreatedAt()).isBetween(before, after);
            assertThat(result.getUpdatedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("doit accepter un contenu d'exactement 1000 caractères")
        void shouldAcceptMaxLengthContent() {
            String maxContent = "X".repeat(1000);
            when(request.getContent()).thenReturn(maxContent);

            Comment result = CommentMapper.toDomain(request, USER_ID);

            assertThat(result.getContent()).hasSize(1000);
        }
    }


    @Nested
    @DisplayName("toResponse(Comment domain)")
    class ToResponse {

        @Test
        @DisplayName("doit mapper tous les champs depuis le domaine")
        void shouldMapAllFields() {
            Comment domain = buildDomain();

            CommentResponse result = CommentMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(ID),
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getMediaId()).isEqualTo(MEDIA_ID),
                    () -> assertThat(result.getContent()).isEqualTo(CONTENT)
            );
        }

        @Test
        @DisplayName("doit mapper createdAt identique à celui du domaine")
        void shouldMapCreatedAt() {
            Comment domain = buildDomain();

            CommentResponse result = CommentMapper.toResponse(domain);

            assertThat(result.getCreatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getCreatedAt());
        }

        @Test
        @DisplayName("doit mapper updatedAt identique à celui du domaine")
        void shouldMapUpdatedAt() {
            Comment domain = buildDomain();

            CommentResponse result = CommentMapper.toResponse(domain);

            assertThat(result.getUpdatedAt())
                    .isNotNull()
                    .isEqualTo(domain.getUpdatedAt());
        }

        @Test
        @DisplayName("après edit(), la réponse doit refléter le nouveau contenu et updatedAt")
        void shouldReflectEditedContent() throws InterruptedException {
            Comment domain = buildDomain();
            Thread.sleep(10);
            domain.edit("Nouveau contenu");

            CommentResponse result = CommentMapper.toResponse(domain);

            assertThat(result.getContent()).isEqualTo("Nouveau contenu");
            assertThat(result.getUpdatedAt()).isEqualTo(domain.getUpdatedAt());
        }

        @Test
        @DisplayName("doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachCall() {
            Comment domain = buildDomain();

            CommentResponse r1 = CommentMapper.toResponse(domain);
            CommentResponse r2 = CommentMapper.toResponse(domain);

            assertThat(r1).isNotSameAs(r2);
        }
    }



    @Nested
    @DisplayName("Validations de Comment propagées au mapper")
    class DomainValidation {

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si content est null")
        void toDomainFromEntity_shouldThrowIfContentNull() {
            CommentEntity entity = buildEntity(null);

            assertThrows(IllegalArgumentException.class,
                    () -> CommentMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si content est blanc")
        void toDomainFromEntity_shouldThrowIfContentBlank() {
            CommentEntity entity = buildEntity("   ");

            assertThrows(IllegalArgumentException.class,
                    () -> CommentMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Entity) doit lever IllegalArgumentException si content dépasse 1000 caractères")
        void toDomainFromEntity_shouldThrowIfContentTooLong() {
            CommentEntity entity = buildEntity("A".repeat(1001));

            assertThrows(IllegalArgumentException.class,
                    () -> CommentMapper.toDomain(entity));
        }

        @Test
        @DisplayName("toDomain(Request) doit lever IllegalArgumentException si content est blanc")
        void toDomainFromRequest_shouldThrowIfContentBlank() {
            CommentRequest request = mock(CommentRequest.class);
            when(request.getMediaId()).thenReturn(MEDIA_ID);
            when(request.getContent()).thenReturn("  ");

            assertThrows(IllegalArgumentException.class,
                    () -> CommentMapper.toDomain(request, USER_ID));
        }

        @Test
        @DisplayName("toDomain(Request) doit lever IllegalArgumentException si content dépasse 1000 caractères")
        void toDomainFromRequest_shouldThrowIfContentTooLong() {
            CommentRequest request = mock(CommentRequest.class);
            when(request.getMediaId()).thenReturn(MEDIA_ID);
            when(request.getContent()).thenReturn("B".repeat(1001));

            assertThrows(IllegalArgumentException.class,
                    () -> CommentMapper.toDomain(request, USER_ID));
        }
    }



    @Nested
    @DisplayName("Roundtrip Entity → Domain → Entity")
    class Roundtrip {

        @Test
        @DisplayName("les champs métier ne doivent pas être perdus lors du roundtrip")
        void entityToDomainToEntity_shouldPreserveBusinessFields() {
            CommentEntity original = buildEntity(CONTENT);

            Comment domain        = CommentMapper.toDomain(original);
            CommentEntity result  = CommentMapper.toEntity(domain);

            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(original.getId()),
                    () -> assertThat(result.getUserId()).isEqualTo(original.getUserId()),
                    () -> assertThat(result.getMediaId()).isEqualTo(original.getMediaId()),
                    () -> assertThat(result.getContent()).isEqualTo(original.getContent())
            );
        }

        @Test
        @DisplayName("toEntity et toResponse doivent être cohérents entre eux")
        void toEntityAndToResponse_shouldBeConsistent() {
            Comment domain = buildDomain();

            CommentEntity   entity   = CommentMapper.toEntity(domain);
            CommentResponse response = CommentMapper.toResponse(domain);

            assertAll(
                    () -> assertThat(response.getId()).isEqualTo(entity.getId()),
                    () -> assertThat(response.getUserId()).isEqualTo(entity.getUserId()),
                    () -> assertThat(response.getMediaId()).isEqualTo(entity.getMediaId()),
                    () -> assertThat(response.getContent()).isEqualTo(entity.getContent()),
                    () -> assertThat(response.getCreatedAt()).isEqualTo(entity.getCreatedAt()),
                    () -> assertThat(response.getUpdatedAt()).isEqualTo(entity.getUpdatedAt())
            );
        }
    }
}
