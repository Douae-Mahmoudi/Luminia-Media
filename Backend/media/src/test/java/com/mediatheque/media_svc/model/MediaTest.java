package com.mediatheque.media_svc.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Media – tests unitaires")
class MediaTest {


    private Media buildMinimal() {
        return Media.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .type(MediaType.BOOK)
                .build();
    }


    @Nested
    @DisplayName("1. Builder")
    class BuilderTests {

        @Test
        @DisplayName("status vaut AVAILABLE par défaut (@Builder.Default)")
        void defaultStatus_isAvailable() {
            Media media = buildMinimal();
            assertThat(media.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        }

        @Test
        @DisplayName("champs requis correctement affectés")
        void requiredFields_areSet() {
            Media media = buildMinimal();
            assertThat(media.getTitle()).isEqualTo("Clean Code");
            assertThat(media.getAuthor()).isEqualTo("Robert C. Martin");
            assertThat(media.getType()).isEqualTo(MediaType.BOOK);
        }

        @Test
        @DisplayName("champs optionnels sont null par défaut")
        void optionalFields_areNullByDefault() {
            Media media = buildMinimal();
            assertThat(media.getId()).isNull();
            assertThat(media.getDescription()).isNull();
            assertThat(media.getOwnerId()).isNull();
            assertThat(media.getReleaseYear()).isNull();
            assertThat(media.getGenre()).isNull();
            assertThat(media.getImageUrl()).isNull();
            assertThat(media.getContentUrl()).isNull();
            assertThat(media.getOwnerUsername()).isNull();
            assertThat(media.getCreatedAt()).isNull();
            assertThat(media.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("builder complet – type FILM, status PENDING")
        void fullBuilder_film_pending() {
            LocalDateTime now = LocalDateTime.now();
            Media media = Media.builder()
                    .id(1L)
                    .title("Inception")
                    .author("Christopher Nolan")
                    .description("Un film de science-fiction.")
                    .type(MediaType.FILM)
                    .ownerId(42L)
                    .status(MediaStatus.PENDING)
                    .releaseYear(2010)
                    .genre("Sci-Fi")
                    .imageUrl("https://example.com/inception.jpg")
                    .contentUrl("https://example.com/inception.mp4")
                    .ownerUsername("cnolan")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            assertThat(media.getId()).isEqualTo(1L);
            assertThat(media.getTitle()).isEqualTo("Inception");
            assertThat(media.getAuthor()).isEqualTo("Christopher Nolan");
            assertThat(media.getDescription()).isEqualTo("Un film de science-fiction.");
            assertThat(media.getType()).isEqualTo(MediaType.FILM);
            assertThat(media.getOwnerId()).isEqualTo(42L);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.PENDING);
            assertThat(media.getReleaseYear()).isEqualTo(2010);
            assertThat(media.getGenre()).isEqualTo("Sci-Fi");
            assertThat(media.getImageUrl()).isEqualTo("https://example.com/inception.jpg");
            assertThat(media.getContentUrl()).isEqualTo("https://example.com/inception.mp4");
            assertThat(media.getOwnerUsername()).isEqualTo("cnolan");
            assertThat(media.getCreatedAt()).isEqualTo(now);
            assertThat(media.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("builder – type GAME, status UNAVAILABLE")
        void fullBuilder_game_unavailable() {
            Media media = Media.builder()
                    .title("The Witcher 3")
                    .author("CD Projekt Red")
                    .type(MediaType.GAME)
                    .status(MediaStatus.UNAVAILABLE)
                    .releaseYear(2015)
                    .genre("RPG")
                    .build();

            assertThat(media.getType()).isEqualTo(MediaType.GAME);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.UNAVAILABLE);
            assertThat(media.getReleaseYear()).isEqualTo(2015);
        }

        @Test
        @DisplayName("builder – type PODCAST, status REJECTED")
        void fullBuilder_podcast_rejected() {
            Media media = Media.builder()
                    .title("Huberman Lab")
                    .author("Andrew Huberman")
                    .type(MediaType.PODCAST)
                    .status(MediaStatus.REJECTED)
                    .build();

            assertThat(media.getType()).isEqualTo(MediaType.PODCAST);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.REJECTED);
        }

        @Test
        @DisplayName("status explicitement surchargé via le builder")
        void builder_overridesDefaultStatus() {
            Media media = Media.builder()
                    .title("Titre")
                    .author("Auteur")
                    .type(MediaType.FILM)
                    .status(MediaStatus.UNAVAILABLE)
                    .build();

            assertThat(media.getStatus()).isEqualTo(MediaStatus.UNAVAILABLE);
        }
    }


    @Nested
    @DisplayName("2. Énumérations MediaType & MediaStatus")
    class EnumCoverageTests {

        @Test
        @DisplayName("MediaType contient exactement BOOK, FILM, GAME, PODCAST")
        void mediaType_hasExactlyFourValues() {
            assertThat(MediaType.values())
                    .containsExactlyInAnyOrder(
                            MediaType.BOOK,
                            MediaType.FILM,
                            MediaType.GAME,
                            MediaType.PODCAST
                    );
        }

        @Test
        @DisplayName("MediaStatus contient exactement PENDING, AVAILABLE, REJECTED, UNAVAILABLE")
        void mediaStatus_hasExactlyFourValues() {
            assertThat(MediaStatus.values())
                    .containsExactlyInAnyOrder(
                            MediaStatus.PENDING,
                            MediaStatus.AVAILABLE,
                            MediaStatus.REJECTED,
                            MediaStatus.UNAVAILABLE
                    );
        }

        @Test
        @DisplayName("Media de type BOOK avec status AVAILABLE")
        void mediaType_BOOK_status_AVAILABLE() {
            Media media = Media.builder()
                    .title("Le Petit Prince")
                    .author("Antoine de Saint-Exupéry")
                    .type(MediaType.BOOK)
                    .status(MediaStatus.AVAILABLE)
                    .build();

            assertThat(media.getType()).isEqualTo(MediaType.BOOK);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Media de type FILM avec status PENDING")
        void mediaType_FILM_status_PENDING() {
            Media media = Media.builder()
                    .title("Dune Part Two")
                    .author("Denis Villeneuve")
                    .type(MediaType.FILM)
                    .status(MediaStatus.PENDING)
                    .build();

            assertThat(media.getType()).isEqualTo(MediaType.FILM);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.PENDING);
        }

        @Test
        @DisplayName("Media de type GAME avec status REJECTED")
        void mediaType_GAME_status_REJECTED() {
            Media media = Media.builder()
                    .title("Elden Ring")
                    .author("FromSoftware")
                    .type(MediaType.GAME)
                    .status(MediaStatus.REJECTED)
                    .build();

            assertThat(media.getType()).isEqualTo(MediaType.GAME);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.REJECTED);
        }

        @Test
        @DisplayName("Media de type PODCAST avec status UNAVAILABLE")
        void mediaType_PODCAST_status_UNAVAILABLE() {
            Media media = Media.builder()
                    .title("Lex Fridman Podcast")
                    .author("Lex Fridman")
                    .type(MediaType.PODCAST)
                    .status(MediaStatus.UNAVAILABLE)
                    .build();

            assertThat(media.getType()).isEqualTo(MediaType.PODCAST);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.UNAVAILABLE);
        }
    }



    @Nested
    @DisplayName("3. Constructeurs Lombok")
    class ConstructorTests {

        @Test
        @DisplayName("NoArgsConstructor crée un objet non null")
        void noArgsConstructor_createsNonNullObject() {
            Media media = new Media();
            assertThat(media).isNotNull();
        }

        @Test
        @DisplayName("NoArgsConstructor : champs null sauf status qui vaut AVAILABLE")
        void noArgsConstructor_fieldsAreNull() {
            Media media = new Media();

            assertThat(media.getId()).isNull();
            assertThat(media.getTitle()).isNull();
            assertThat(media.getAuthor()).isNull();
            assertThat(media.getType()).isNull();
            assertThat(media.getDescription()).isNull();
            assertThat(media.getOwnerId()).isNull();
            assertThat(media.getReleaseYear()).isNull();
            assertThat(media.getGenre()).isNull();
            assertThat(media.getImageUrl()).isNull();
            assertThat(media.getContentUrl()).isNull();
            assertThat(media.getOwnerUsername()).isNull();
            assertThat(media.getCreatedAt()).isNull();
            assertThat(media.getUpdatedAt()).isNull();

            assertThat(media.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
        }

        @Test
        @DisplayName("AllArgsConstructor – BOOK / AVAILABLE")
        void allArgsConstructor_book_available() {
            LocalDateTime ts = LocalDateTime.of(2024, 6, 1, 10, 0);
            Media media = new Media(
                    10L, "Titre", "Auteur", "Desc",
                    MediaType.BOOK, 99L, MediaStatus.AVAILABLE,
                    2020, "Roman", "https://img.test/cover.jpg",
                    "https://img.test/content.mp4",
                    ts, ts,
                    "auteur_user"
            );

            assertThat(media.getId()).isEqualTo(10L);
            assertThat(media.getType()).isEqualTo(MediaType.BOOK);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.AVAILABLE);
            assertThat(media.getContentUrl()).isEqualTo("https://img.test/content.mp4");
            assertThat(media.getOwnerUsername()).isEqualTo("auteur_user");
        }

        @Test
        @DisplayName("AllArgsConstructor – PODCAST / PENDING")
        void allArgsConstructor_podcast_pending() {
            LocalDateTime ts = LocalDateTime.now();
            Media media = new Media(
                    2L, "Tech Podcast", "Host", null,
                    MediaType.PODCAST, null, MediaStatus.PENDING,
                    2023, "Tech", null,
                    null,
                    ts, ts,
                    null
            );

            assertThat(media.getType()).isEqualTo(MediaType.PODCAST);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.PENDING);
            assertThat(media.getOwnerId()).isNull();
            assertThat(media.getImageUrl()).isNull();
            assertThat(media.getContentUrl()).isNull();
            assertThat(media.getOwnerUsername()).isNull();
        }
    }


    @Nested
    @DisplayName("4. Getters / Setters (@Data)")
    class GetterSetterTests {

        @Test
        @DisplayName("setType accepte toutes les valeurs de MediaType")
        void setter_allMediaTypes() {
            Media media = buildMinimal();
            for (MediaType type : MediaType.values()) {
                media.setType(type);
                assertThat(media.getType()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("setStatus accepte toutes les valeurs de MediaStatus")
        void setter_allMediaStatuses() {
            Media media = buildMinimal();
            for (MediaStatus status : MediaStatus.values()) {
                media.setStatus(status);
                assertThat(media.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("setStatus PENDING")
        void setter_status_pending() {
            Media media = buildMinimal();
            media.setStatus(MediaStatus.PENDING);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.PENDING);
        }

        @Test
        @DisplayName("setStatus REJECTED")
        void setter_status_rejected() {
            Media media = buildMinimal();
            media.setStatus(MediaStatus.REJECTED);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.REJECTED);
        }

        @Test
        @DisplayName("setStatus UNAVAILABLE")
        void setter_status_unavailable() {
            Media media = buildMinimal();
            media.setStatus(MediaStatus.UNAVAILABLE);
            assertThat(media.getStatus()).isEqualTo(MediaStatus.UNAVAILABLE);
        }

        @Test
        @DisplayName("setOwnerId accepte null (colonne nullable)")
        void setter_ownerIdAcceptsNull() {
            Media media = buildMinimal();
            media.setOwnerId(null);
            assertThat(media.getOwnerId()).isNull();
        }

        @Test
        @DisplayName("setOwnerId stocke une valeur non-null")
        void setter_ownerId() {
            Media media = buildMinimal();
            media.setOwnerId(7L);
            assertThat(media.getOwnerId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("setTitle met à jour la valeur")
        void setter_title() {
            Media media = buildMinimal();
            media.setTitle("Nouveau Titre");
            assertThat(media.getTitle()).isEqualTo("Nouveau Titre");
        }

        @Test
        @DisplayName("setContentUrl met à jour la valeur")
        void setter_contentUrl() {
            Media media = buildMinimal();
            media.setContentUrl("https://example.com/content.mp4");
            assertThat(media.getContentUrl()).isEqualTo("https://example.com/content.mp4");
        }

        @Test
        @DisplayName("setOwnerUsername met à jour la valeur")
        void setter_ownerUsername() {
            Media media = buildMinimal();
            media.setOwnerUsername("newuser");
            assertThat(media.getOwnerUsername()).isEqualTo("newuser");
        }
    }


    @Nested
    @DisplayName("5. Callbacks JPA")
    class JpaLifecycleTests {

        @Test
        @DisplayName("prePersist() initialise createdAt")
        void prePersist_setsCreatedAt() {
            Media media = buildMinimal();
            assertThat(media.getCreatedAt()).isNull();

            media.prePersist();

            assertThat(media.getCreatedAt()).isNotNull();
            assertThat(media.getCreatedAt())
                    .isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("prePersist() initialise updatedAt")
        void prePersist_setsUpdatedAt() {
            Media media = buildMinimal();
            media.prePersist();

            assertThat(media.getUpdatedAt()).isNotNull();
            assertThat(media.getUpdatedAt())
                    .isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("prePersist() : createdAt et updatedAt sont très proches")
        void prePersist_timestampsAreClose() {
            Media media = buildMinimal();
            media.prePersist();

            assertThat(media.getCreatedAt())
                    .isCloseTo(media.getUpdatedAt(), within(100, ChronoUnit.MILLIS));
        }

        @Test
        @DisplayName("preUpdate() rafraîchit updatedAt sans toucher createdAt")
        void preUpdate_refreshesUpdatedAtOnly() throws InterruptedException {
            Media media = buildMinimal();
            media.prePersist();

            LocalDateTime originalCreatedAt = media.getCreatedAt();
            LocalDateTime originalUpdatedAt = media.getUpdatedAt();

            Thread.sleep(10);
            media.preUpdate();

            assertThat(media.getCreatedAt())
                    .as("createdAt ne doit pas changer après preUpdate")
                    .isEqualTo(originalCreatedAt);

            assertThat(media.getUpdatedAt())
                    .as("updatedAt doit être postérieur ou égal à l'original")
                    .isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("preUpdate() peut être appelé plusieurs fois")
        void preUpdate_canBeCalledMultipleTimes() throws InterruptedException {
            Media media = buildMinimal();
            media.prePersist();

            Thread.sleep(5);
            media.preUpdate();
            LocalDateTime first = media.getUpdatedAt();

            Thread.sleep(5);
            media.preUpdate();
            LocalDateTime second = media.getUpdatedAt();

            assertThat(second).isAfterOrEqualTo(first);
        }

        @Test
        @DisplayName("prePersist() fonctionne quel que soit le type (GAME / REJECTED)")
        void prePersist_worksWithAnyEnumCombination() {
            Media media = Media.builder()
                    .title("Among Us")
                    .author("Innersloth")
                    .type(MediaType.GAME)
                    .status(MediaStatus.REJECTED)
                    .build();

            media.prePersist();

            assertThat(media.getCreatedAt()).isNotNull();
            assertThat(media.getUpdatedAt()).isNotNull();
        }
    }


    @Nested
    @DisplayName("6. equals / hashCode / toString")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("deux instances avec les mêmes champs sont égales")
        void equals_sameFields_areEqual() {
            LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 0, 0);
            Media a = Media.builder()
                    .id(1L).title("T").author("A")
                    .type(MediaType.FILM).status(MediaStatus.AVAILABLE)
                    .createdAt(ts).updatedAt(ts).build();
            Media b = Media.builder()
                    .id(1L).title("T").author("A")
                    .type(MediaType.FILM).status(MediaStatus.AVAILABLE)
                    .createdAt(ts).updatedAt(ts).build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("ids différents → instances non égales")
        void equals_differentId_areNotEqual() {
            Media a = Media.builder().id(1L).title("T").author("A")
                    .type(MediaType.PODCAST).build();
            Media b = Media.builder().id(2L).title("T").author("A")
                    .type(MediaType.PODCAST).build();

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("statuts différents → instances non égales")
        void equals_differentStatus_areNotEqual() {
            Media a = Media.builder().title("T").author("A")
                    .type(MediaType.BOOK).status(MediaStatus.PENDING).build();
            Media b = Media.builder().title("T").author("A")
                    .type(MediaType.BOOK).status(MediaStatus.REJECTED).build();

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("types différents → instances non égales")
        void equals_differentType_areNotEqual() {
            Media a = Media.builder().title("T").author("A").type(MediaType.BOOK).build();
            Media b = Media.builder().title("T").author("A").type(MediaType.GAME).build();

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("toString() contient les champs principaux")
        void toString_containsKeyFields() {
            Media media = Media.builder()
                    .id(5L)
                    .title("Titre Test")
                    .author("Auteur Test")
                    .type(MediaType.GAME)
                    .status(MediaStatus.PENDING)
                    .contentUrl("https://example.com/content.mp4")
                    .ownerUsername("testuser")
                    .build();

            String result = media.toString();

            assertThat(result).contains("5");
            assertThat(result).contains("Titre Test");
            assertThat(result).contains("Auteur Test");
            assertThat(result).contains("GAME");
            assertThat(result).contains("PENDING");
            assertThat(result).contains("testuser");
        }

        @Test
        @DisplayName("une instance est égale à elle-même")
        void equals_selfReference() {
            Media media = buildMinimal();
            assertThat(media).isEqualTo(media);
        }

        @Test
        @DisplayName("une instance n'est pas égale à null")
        void equals_notEqualToNull() {
            Media media = buildMinimal();
            assertThat(media).isNotEqualTo(null);
        }
    }
}
