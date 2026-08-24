package com.collection.infrastructure.persistence.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollectionEntity")
class CollectionEntityTest {


    private static final String ID          = "col-001";
    private static final String USER_ID     = "user-123";
    private static final String NAME        = "Ma collection";
    private static final String DESCRIPTION = "Une description";
    private static final LocalDateTime NOW  = LocalDateTime.of(2024, 6, 1, 10, 0);

    private CollectionEntity entity;

    @BeforeEach
    void setUp() {
        entity = new CollectionEntity();
    }



    @Nested
    @DisplayName("Constructeur par défaut")
    class DefaultConstructor {

        @Test
        @DisplayName("doit être instanciable sans argument (requis par JPA)")
        void shouldInstantiateWithNoArgs() {
            assertDoesNotThrow(() -> new CollectionEntity());
        }

        @Test
        @DisplayName("mediaIds doit être initialisé à une liste vide (non null)")
        void shouldInitializeMediaIdsAsEmptyList() {
            CollectionEntity fresh = new CollectionEntity();

            assertThat(fresh.getMediaIds())
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("les autres champs doivent être null par défaut")
        void shouldHaveNullFieldsByDefault() {
            CollectionEntity fresh = new CollectionEntity();

            assertAll(
                    () -> assertThat(fresh.getId()).isNull(),
                    () -> assertThat(fresh.getUserId()).isNull(),
                    () -> assertThat(fresh.getName()).isNull(),
                    () -> assertThat(fresh.getDescription()).isNull(),
                    () -> assertThat(fresh.getType()).isNull(),
                    () -> assertThat(fresh.getWatchlistStatus()).isNull(),
                    () -> assertThat(fresh.getCreatedAt()).isNull(),
                    () -> assertThat(fresh.getUpdatedAt()).isNull()
            );
        }

        @Test
        @DisplayName("isPublic doit être false par défaut (valeur primitive)")
        void shouldHavePublicFalseByDefault() {
            assertThat(new CollectionEntity().isPublic()).isFalse();
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
        @DisplayName("setName / getName")
        void shouldSetAndGetName() {
            entity.setName(NAME);
            assertThat(entity.getName()).isEqualTo(NAME);
        }

        @Test
        @DisplayName("setDescription / getDescription")
        void shouldSetAndGetDescription() {
            entity.setDescription(DESCRIPTION);
            assertThat(entity.getDescription()).isEqualTo(DESCRIPTION);
        }

        @Test
        @DisplayName("setPublic(true) / isPublic")
        void shouldSetPublicTrue() {
            entity.setPublic(true);
            assertThat(entity.isPublic()).isTrue();
        }

        @Test
        @DisplayName("setPublic(false) / isPublic")
        void shouldSetPublicFalse() {
            entity.setPublic(true);
            entity.setPublic(false);
            assertThat(entity.isPublic()).isFalse();
        }

        @Test
        @DisplayName("setType / getType — COLLECTION")
        void shouldSetAndGetTypeCollection() {
            entity.setType(CollectionEntity.CollectionType.COLLECTION);
            assertThat(entity.getType()).isEqualTo(CollectionEntity.CollectionType.COLLECTION);
        }

        @Test
        @DisplayName("setType / getType — WATCHLIST")
        void shouldSetAndGetTypeWatchlist() {
            entity.setType(CollectionEntity.CollectionType.WATCHLIST);
            assertThat(entity.getType()).isEqualTo(CollectionEntity.CollectionType.WATCHLIST);
        }

        @Test
        @DisplayName("setWatchlistStatus / getWatchlistStatus — TO_WATCH")
        void shouldSetAndGetWatchlistStatusToWatch() {
            entity.setWatchlistStatus(CollectionEntity.WatchlistStatus.TO_WATCH);
            assertThat(entity.getWatchlistStatus())
                    .isEqualTo(CollectionEntity.WatchlistStatus.TO_WATCH);
        }

        @Test
        @DisplayName("setWatchlistStatus / getWatchlistStatus — WATCHING")
        void shouldSetAndGetWatchlistStatusWatching() {
            entity.setWatchlistStatus(CollectionEntity.WatchlistStatus.WATCHING);
            assertThat(entity.getWatchlistStatus())
                    .isEqualTo(CollectionEntity.WatchlistStatus.WATCHING);
        }

        @Test
        @DisplayName("setWatchlistStatus / getWatchlistStatus — WATCHED")
        void shouldSetAndGetWatchlistStatusWatched() {
            entity.setWatchlistStatus(CollectionEntity.WatchlistStatus.WATCHED);
            assertThat(entity.getWatchlistStatus())
                    .isEqualTo(CollectionEntity.WatchlistStatus.WATCHED);
        }

        @Test
        @DisplayName("setMediaIds / getMediaIds")
        void shouldSetAndGetMediaIds() {
            List<String> mediaIds = Arrays.asList("m-1", "m-2", "m-3");
            entity.setMediaIds(mediaIds);
            assertThat(entity.getMediaIds()).containsExactlyElementsOf(mediaIds);
        }

        @Test
        @DisplayName("setMediaIds avec liste vide")
        void shouldSetAndGetEmptyMediaIds() {
            entity.setMediaIds(List.of());
            assertThat(entity.getMediaIds()).isEmpty();
        }

        @Test
        @DisplayName("setCreatedAt / getCreatedAt")
        void shouldSetAndGetCreatedAt() {
            entity.setCreatedAt(NOW);
            assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("setUpdatedAt / getUpdatedAt")
        void shouldSetAndGetUpdatedAt() {
            LocalDateTime updatedAt = NOW.plusDays(1);
            entity.setUpdatedAt(updatedAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("setId avec null doit être accepté (nullable en base)")
        void shouldAcceptNullId() {
            entity.setId(ID);
            entity.setId(null);
            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("setWatchlistStatus avec null doit être accepté (colonne nullable)")
        void shouldAcceptNullWatchlistStatus() {
            entity.setWatchlistStatus(CollectionEntity.WatchlistStatus.WATCHING);
            entity.setWatchlistStatus(null);
            assertThat(entity.getWatchlistStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("Énumérations")
    class Enums {

        @Test
        @DisplayName("CollectionType doit contenir exactement COLLECTION et WATCHLIST")
        void shouldHaveExactCollectionTypeValues() {
            assertThat(CollectionEntity.CollectionType.values())
                    .containsExactlyInAnyOrder(
                            CollectionEntity.CollectionType.COLLECTION,
                            CollectionEntity.CollectionType.WATCHLIST
                    );
        }

        @Test
        @DisplayName("WatchlistStatus doit contenir exactement TO_WATCH, WATCHING et WATCHED")
        void shouldHaveExactWatchlistStatusValues() {
            assertThat(CollectionEntity.WatchlistStatus.values())
                    .containsExactlyInAnyOrder(
                            CollectionEntity.WatchlistStatus.TO_WATCH,
                            CollectionEntity.WatchlistStatus.WATCHING,
                            CollectionEntity.WatchlistStatus.WATCHED
                    );
        }

        @Test
        @DisplayName("CollectionType.valueOf doit résoudre correctement les noms")
        void shouldResolveCollectionTypeByName() {
            assertAll(
                    () -> assertThat(CollectionEntity.CollectionType.valueOf("COLLECTION"))
                            .isEqualTo(CollectionEntity.CollectionType.COLLECTION),
                    () -> assertThat(CollectionEntity.CollectionType.valueOf("WATCHLIST"))
                            .isEqualTo(CollectionEntity.CollectionType.WATCHLIST)
            );
        }

        @Test
        @DisplayName("WatchlistStatus.valueOf doit résoudre correctement les noms")
        void shouldResolveWatchlistStatusByName() {
            assertAll(
                    () -> assertThat(CollectionEntity.WatchlistStatus.valueOf("TO_WATCH"))
                            .isEqualTo(CollectionEntity.WatchlistStatus.TO_WATCH),
                    () -> assertThat(CollectionEntity.WatchlistStatus.valueOf("WATCHING"))
                            .isEqualTo(CollectionEntity.WatchlistStatus.WATCHING),
                    () -> assertThat(CollectionEntity.WatchlistStatus.valueOf("WATCHED"))
                            .isEqualTo(CollectionEntity.WatchlistStatus.WATCHED)
            );
        }

        @Test
        @DisplayName("CollectionType.valueOf avec nom invalide doit lever IllegalArgumentException")
        void shouldThrowForUnknownCollectionType() {
            assertThrows(IllegalArgumentException.class,
                    () -> CollectionEntity.CollectionType.valueOf("UNKNOWN"));
        }

        @Test
        @DisplayName("WatchlistStatus.valueOf avec nom invalide doit lever IllegalArgumentException")
        void shouldThrowForUnknownWatchlistStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> CollectionEntity.WatchlistStatus.valueOf("UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("Cohérence des champs combinés")
    class FieldConsistency {

        @Test
        @DisplayName("une entity entièrement remplie doit exposer tous ses champs correctement")
        void shouldExposeAllFieldsCorrectly() {
            List<String> mediaIds = Arrays.asList("m-1", "m-2");

            entity.setId(ID);
            entity.setUserId(USER_ID);
            entity.setName(NAME);
            entity.setDescription(DESCRIPTION);
            entity.setPublic(true);
            entity.setType(CollectionEntity.CollectionType.COLLECTION);
            entity.setWatchlistStatus(CollectionEntity.WatchlistStatus.TO_WATCH);
            entity.setMediaIds(mediaIds);
            entity.setCreatedAt(NOW);
            entity.setUpdatedAt(NOW.plusHours(1));

            assertAll(
                    () -> assertThat(entity.getId()).isEqualTo(ID),
                    () -> assertThat(entity.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(entity.getName()).isEqualTo(NAME),
                    () -> assertThat(entity.getDescription()).isEqualTo(DESCRIPTION),
                    () -> assertThat(entity.isPublic()).isTrue(),
                    () -> assertThat(entity.getType()).isEqualTo(CollectionEntity.CollectionType.COLLECTION),
                    () -> assertThat(entity.getWatchlistStatus()).isEqualTo(CollectionEntity.WatchlistStatus.TO_WATCH),
                    () -> assertThat(entity.getMediaIds()).containsExactlyElementsOf(mediaIds),
                    () -> assertThat(entity.getCreatedAt()).isEqualTo(NOW),
                    () -> assertThat(entity.getUpdatedAt()).isEqualTo(NOW.plusHours(1))
            );
        }

        @Test
        @DisplayName("updatedAt peut être postérieur à createdAt")
        void shouldAllowUpdatedAtAfterCreatedAt() {
            entity.setCreatedAt(NOW);
            entity.setUpdatedAt(NOW.plusDays(7));

            assertThat(entity.getUpdatedAt()).isAfter(entity.getCreatedAt());
        }

        @Test
        @DisplayName("deux instances indépendantes ne partagent pas la même liste mediaIds")
        void shouldHaveIndependentMediaIdLists() {
            CollectionEntity e1 = new CollectionEntity();
            CollectionEntity e2 = new CollectionEntity();

            e1.getMediaIds().add("m-1");

            assertThat(e2.getMediaIds()).doesNotContain("m-1");
        }
    }
}