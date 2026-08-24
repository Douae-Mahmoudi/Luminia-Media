package com.collection.infrastructure.persistence.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommentEntity")
class CommentEntityTest {


    private static final String ID       = "cmt-001";
    private static final String USER_ID  = "user-123";
    private static final String MEDIA_ID = "media-456";
    private static final String CONTENT  = "Super commentaire !";
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 1, 10, 0);

    private CommentEntity entity;

    @BeforeEach
    void setUp() {
        entity = new CommentEntity();
    }



    @Nested
    @DisplayName("Constructeur par défaut")
    class DefaultConstructor {

        @Test
        @DisplayName("doit être instanciable sans argument (requis par JPA)")
        void shouldInstantiateWithNoArgs() {
            assertDoesNotThrow(() -> new CommentEntity());
        }

        @Test
        @DisplayName("tous les champs doivent être null par défaut")
        void shouldHaveAllFieldsNullByDefault() {
            CommentEntity fresh = new CommentEntity();

            assertAll(
                    () -> assertThat(fresh.getId()).isNull(),
                    () -> assertThat(fresh.getUserId()).isNull(),
                    () -> assertThat(fresh.getMediaId()).isNull(),
                    () -> assertThat(fresh.getContent()).isNull(),
                    () -> assertThat(fresh.getCreatedAt()).isNull(),
                    () -> assertThat(fresh.getUpdatedAt()).isNull()
            );
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
        @DisplayName("setContent / getContent")
        void shouldSetAndGetContent() {
            entity.setContent(CONTENT);
            assertThat(entity.getContent()).isEqualTo(CONTENT);
        }

        @Test
        @DisplayName("setContent avec exactement 1000 caractères (limite de colonne)")
        void shouldAcceptContentAtMaxLength() {
            String maxContent = "A".repeat(1000);
            entity.setContent(maxContent);
            assertThat(entity.getContent()).hasSize(1000);
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
            LocalDateTime updatedAt = NOW.plusHours(3);
            entity.setUpdatedAt(updatedAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("chaque setter écrase la valeur précédente")
        void shouldOverwritePreviousValue() {
            entity.setContent("Premier contenu");
            entity.setContent("Contenu modifié");
            assertThat(entity.getContent()).isEqualTo("Contenu modifié");
        }

        @Test
        @DisplayName("setId avec null doit être accepté (nullable avant persist)")
        void shouldAcceptNullId() {
            entity.setId(ID);
            entity.setId(null);
            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("setContent avec null doit être accepté (validation laissée à JPA/domaine)")
        void shouldAcceptNullContent() {
            entity.setContent(CONTENT);
            entity.setContent(null);
            assertThat(entity.getContent()).isNull();
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
            entity.setContent(CONTENT);
            entity.setCreatedAt(NOW);
            entity.setUpdatedAt(NOW.plusMinutes(30));

            assertAll(
                    () -> assertThat(entity.getId()).isEqualTo(ID),
                    () -> assertThat(entity.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(entity.getMediaId()).isEqualTo(MEDIA_ID),
                    () -> assertThat(entity.getContent()).isEqualTo(CONTENT),
                    () -> assertThat(entity.getCreatedAt()).isEqualTo(NOW),
                    () -> assertThat(entity.getUpdatedAt()).isEqualTo(NOW.plusMinutes(30))
            );
        }

        @Test
        @DisplayName("updatedAt peut être postérieur à createdAt")
        void shouldAllowUpdatedAtAfterCreatedAt() {
            entity.setCreatedAt(NOW);
            entity.setUpdatedAt(NOW.plusDays(2));

            assertThat(entity.getUpdatedAt()).isAfter(entity.getCreatedAt());
        }

        @Test
        @DisplayName("createdAt et updatedAt peuvent être identiques (création sans modification)")
        void shouldAllowSameCreatedAtAndUpdatedAt() {
            entity.setCreatedAt(NOW);
            entity.setUpdatedAt(NOW);

            assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
        }

        @Test
        @DisplayName("deux instances indépendantes ne partagent aucun état")
        void shouldHaveIndependentState() {
            CommentEntity e1 = new CommentEntity();
            CommentEntity e2 = new CommentEntity();

            e1.setId(ID);
            e1.setContent(CONTENT);

            assertAll(
                    () -> assertThat(e2.getId()).isNull(),
                    () -> assertThat(e2.getContent()).isNull()
            );
        }
    }
}
