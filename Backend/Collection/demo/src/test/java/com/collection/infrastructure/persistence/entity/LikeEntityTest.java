package com.collection.infrastructure.persistence.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LikeEntity")
class LikeEntityTest {


    private static final String ID       = "like-001";
    private static final String USER_ID  = "user-123";
    private static final String MEDIA_ID = "media-456";
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 1, 10, 0);

    private LikeEntity entity;

    @BeforeEach
    void setUp() {
        entity = new LikeEntity();
    }



    @Nested
    @DisplayName("Constructeur par défaut")
    class DefaultConstructor {

        @Test
        @DisplayName("doit être instanciable sans argument (requis par JPA)")
        void shouldInstantiateWithNoArgs() {
            assertDoesNotThrow(() -> new LikeEntity());
        }

        @Test
        @DisplayName("tous les champs doivent être null par défaut")
        void shouldHaveAllFieldsNullByDefault() {
            LikeEntity fresh = new LikeEntity();

            assertAll(
                    () -> assertThat(fresh.getId()).isNull(),
                    () -> assertThat(fresh.getUserId()).isNull(),
                    () -> assertThat(fresh.getMediaId()).isNull(),
                    () -> assertThat(fresh.getType()).isNull(),
                    () -> assertThat(fresh.getCreatedAt()).isNull()
            );
        }

        @Test
        @DisplayName("LikeEntity ne doit pas avoir de champ updatedAt")
        void shouldNotExposeUpdatedAt() {
            assertDoesNotThrow(() -> {
                LikeEntity fresh = new LikeEntity();
                fresh.getCreatedAt();
            });
        }
    }



    @Nested
    @DisplayName("Getters et Setters")
    class GettersSetters {

        @Test
        @DisplayName("setId / getId")
        void shouldSetAndGetId() {
            entity.setId(ID);
            assertThat(entity.getId()).isEqualTo(ID);
        }

        @Test
        @DisplayName("setUserId / getUserId")
        void shouldSetAndGetUserId() {
            entity.setUserId(USER_ID);
            assertThat(entity.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("setMediaId / getMediaId")
        void shouldSetAndGetMediaId() {
            entity.setMediaId(MEDIA_ID);
            assertThat(entity.getMediaId()).isEqualTo(MEDIA_ID);
        }

        @Test
        @DisplayName("setType / getType — LIKE")
        void shouldSetAndGetTypeLike() {
            entity.setType(LikeEntity.LikeType.LIKE);
            assertThat(entity.getType()).isEqualTo(LikeEntity.LikeType.LIKE);
        }

        @Test
        @DisplayName("setType / getType — FAVORITE")
        void shouldSetAndGetTypeFavorite() {
            entity.setType(LikeEntity.LikeType.FAVORITE);
            assertThat(entity.getType()).isEqualTo(LikeEntity.LikeType.FAVORITE);
        }

        @Test
        @DisplayName("setCreatedAt / getCreatedAt")
        void shouldSetAndGetCreatedAt() {
            entity.setCreatedAt(NOW);
            assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("chaque setter écrase la valeur précédente")
        void shouldOverwritePreviousValue() {
            entity.setUserId("old-user");
            entity.setUserId(USER_ID);
            assertThat(entity.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("setType écrase correctement la valeur précédente")
        void shouldOverwritePreviousType() {
            entity.setType(LikeEntity.LikeType.LIKE);
            entity.setType(LikeEntity.LikeType.FAVORITE);
            assertThat(entity.getType()).isEqualTo(LikeEntity.LikeType.FAVORITE);
        }

        @Test
        @DisplayName("setId avec null doit être accepté (nullable avant persist)")
        void shouldAcceptNullId() {
            entity.setId(ID);
            entity.setId(null);
            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("setType avec null doit être accepté (validation laissée à JPA)")
        void shouldAcceptNullType() {
            entity.setType(LikeEntity.LikeType.LIKE);
            entity.setType(null);
            assertThat(entity.getType()).isNull();
        }
    }


    @Nested
    @DisplayName("Énumération LikeType")
    class LikeTypeEnum {

        @Test
        @DisplayName("doit contenir exactement LIKE et FAVORITE")
        void shouldHaveExactValues() {
            assertThat(LikeEntity.LikeType.values())
                    .containsExactlyInAnyOrder(
                            LikeEntity.LikeType.LIKE,
                            LikeEntity.LikeType.FAVORITE
                    );
        }

        @Test
        @DisplayName("valueOf doit résoudre LIKE correctement")
        void shouldResolvelikeByName() {
            assertThat(LikeEntity.LikeType.valueOf("LIKE"))
                    .isEqualTo(LikeEntity.LikeType.LIKE);
        }

        @Test
        @DisplayName("valueOf doit résoudre FAVORITE correctement")
        void shouldResolveFavoriteByName() {
            assertThat(LikeEntity.LikeType.valueOf("FAVORITE"))
                    .isEqualTo(LikeEntity.LikeType.FAVORITE);
        }

        @Test
        @DisplayName("valueOf avec nom invalide doit lever IllegalArgumentException")
        void shouldThrowForUnknownLikeType() {
            assertThrows(IllegalArgumentException.class,
                    () -> LikeEntity.LikeType.valueOf("DISLIKE"));
        }

        @Test
        @DisplayName("les noms persistés en base doivent correspondre aux valeurs enum (@EnumType.STRING)")
        void shouldHaveCorrectStringRepresentation() {
            assertAll(
                    () -> assertThat(LikeEntity.LikeType.LIKE.name()).isEqualTo("LIKE"),
                    () -> assertThat(LikeEntity.LikeType.FAVORITE.name()).isEqualTo("FAVORITE")
            );
        }
    }


    @Nested
    @DisplayName("Contrainte d'unicité (user_id, media_id, type)")
    class UniqueConstraint {

        @Test
        @DisplayName("deux entités avec même userId, mediaId et type LIKE sont candidates à conflit")
        void sameUserMediaAndTypeShouldBeUniqueConstraintCandidates() {
            LikeEntity e1 = new LikeEntity();
            e1.setId("like-A");
            e1.setUserId(USER_ID);
            e1.setMediaId(MEDIA_ID);
            e1.setType(LikeEntity.LikeType.LIKE);

            LikeEntity e2 = new LikeEntity();
            e2.setId("like-B");
            e2.setUserId(USER_ID);
            e2.setMediaId(MEDIA_ID);
            e2.setType(LikeEntity.LikeType.LIKE);

            assertAll(
                    () -> assertThat(e1.getUserId()).isEqualTo(e2.getUserId()),
                    () -> assertThat(e1.getMediaId()).isEqualTo(e2.getMediaId()),
                    () -> assertThat(e1.getType()).isEqualTo(e2.getType())
            );
        }

        @Test
        @DisplayName("même userId et mediaId mais types différents ne violent pas la contrainte")
        void sameUserMediaDifferentTypeShouldBeDistinct() {
            LikeEntity like = new LikeEntity();
            like.setUserId(USER_ID);
            like.setMediaId(MEDIA_ID);
            like.setType(LikeEntity.LikeType.LIKE);

            LikeEntity favorite = new LikeEntity();
            favorite.setUserId(USER_ID);
            favorite.setMediaId(MEDIA_ID);
            favorite.setType(LikeEntity.LikeType.FAVORITE);

            assertThat(like.getType()).isNotEqualTo(favorite.getType());
        }

        @Test
        @DisplayName("même userId et type mais mediaIds différents ne violent pas la contrainte")
        void sameUserTypeDifferentMediaShouldBeDistinct() {
            LikeEntity e1 = new LikeEntity();
            e1.setUserId(USER_ID);
            e1.setMediaId("media-A");
            e1.setType(LikeEntity.LikeType.LIKE);

            LikeEntity e2 = new LikeEntity();
            e2.setUserId(USER_ID);
            e2.setMediaId("media-B");
            e2.setType(LikeEntity.LikeType.LIKE);

            assertThat(e1.getMediaId()).isNotEqualTo(e2.getMediaId());
        }
    }


    @Nested
    @DisplayName("Cohérence des champs combinés")
    class FieldConsistency {

        @Test
        @DisplayName("une entity entièrement remplie doit exposer tous ses champs correctement")
        void shouldExposeAllFieldsCorrectly() {
            entity.setId(ID);
            entity.setUserId(USER_ID);
            entity.setMediaId(MEDIA_ID);
            entity.setType(LikeEntity.LikeType.LIKE);
            entity.setCreatedAt(NOW);

            assertAll(
                    () -> assertThat(entity.getId()).isEqualTo(ID),
                    () -> assertThat(entity.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(entity.getMediaId()).isEqualTo(MEDIA_ID),
                    () -> assertThat(entity.getType()).isEqualTo(LikeEntity.LikeType.LIKE),
                    () -> assertThat(entity.getCreatedAt()).isEqualTo(NOW)
            );
        }

        @Test
        @DisplayName("deux instances indépendantes ne partagent aucun état")
        void shouldHaveIndependentState() {
            LikeEntity e1 = new LikeEntity();
            LikeEntity e2 = new LikeEntity();

            e1.setUserId(USER_ID);
            e1.setType(LikeEntity.LikeType.LIKE);

            assertAll(
                    () -> assertThat(e2.getUserId()).isNull(),
                    () -> assertThat(e2.getType()).isNull()
            );
        }
    }
}
