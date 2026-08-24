package com.collection.Integration;

import com.collection.domain.Comment;
import com.collection.infrastructure.persistence.JpaCommentRepository;
import com.collection.infrastructure.persistence.adapter.CommentRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CommentRepositoryAdapter.class)
class CommentRepositoryAdapterIntegrationTest {

    @Autowired private CommentRepositoryAdapter adapter;
    @Autowired private JpaCommentRepository jpa;

    private Comment fakeComment;

    @BeforeEach
    void setUp() {
        jpa.deleteAll();
        fakeComment = new Comment(UUID.randomUUID().toString(), "user-01", "media-001", "Super film !");
    }


    @Test
    void save_shouldPersistComment() {
        Comment saved = adapter.save(fakeComment);

        assertNotNull(saved);
        assertEquals(fakeComment.getId(), saved.getId());
        assertEquals("user-01", saved.getUserId());
        assertEquals("media-001", saved.getMediaId());
        assertEquals("Super film !", saved.getContent());
    }

    @Test
    void save_shouldPersistTimestamps() {
        Comment saved = adapter.save(fakeComment);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }


    @Test
    void findById_shouldReturnComment() {
        adapter.save(fakeComment);

        Optional<Comment> result = adapter.findById(fakeComment.getId());

        assertTrue(result.isPresent());
        assertEquals(fakeComment.getId(), result.get().getId());
        assertEquals("Super film !", result.get().getContent());
    }

    @Test
    void findById_notFound_shouldReturnEmpty() {
        Optional<Comment> result = adapter.findById("not-exist");

        assertTrue(result.isEmpty());
    }


    @Test
    void findByMediaId_shouldReturnAllCommentsForMedia() {
        adapter.save(fakeComment);
        Comment second = new Comment(UUID.randomUUID().toString(), "user-02", "media-001", "Bof...");
        adapter.save(second);

        List<Comment> result = adapter.findByMediaId("media-001");

        assertEquals(2, result.size());
    }

    @Test
    void findByMediaId_shouldNotReturnOtherMediaComments() {
        adapter.save(fakeComment);
        Comment other = new Comment(UUID.randomUUID().toString(), "user-01", "media-999", "Autre media");
        adapter.save(other);

        List<Comment> result = adapter.findByMediaId("media-001");

        assertEquals(1, result.size());
        assertEquals("media-001", result.get(0).getMediaId());
    }

    @Test
    void findByMediaId_noComments_shouldReturnEmpty() {
        List<Comment> result = adapter.findByMediaId("media-inconnu");

        assertTrue(result.isEmpty());
    }


    @Test
    void findByUserId_shouldReturnAllCommentsForUser() {
        adapter.save(fakeComment);
        Comment second = new Comment(UUID.randomUUID().toString(), "user-01", "media-002", "Autre commentaire");
        adapter.save(second);

        List<Comment> result = adapter.findByUserId("user-01");

        assertEquals(2, result.size());
    }

    @Test
    void findByUserId_shouldNotReturnOtherUsersComments() {
        adapter.save(fakeComment); // user-01
        Comment other = new Comment(UUID.randomUUID().toString(), "user-02", "media-001", "Commentaire user 2");
        adapter.save(other);

        List<Comment> result = adapter.findByUserId("user-01");

        assertEquals(1, result.size());
        assertEquals("user-01", result.get(0).getUserId());
    }


    @Test
    void existsById_shouldReturnTrueWhenExists() {
        adapter.save(fakeComment);

        assertTrue(adapter.existsById(fakeComment.getId()));
    }

    @Test
    void existsById_shouldReturnFalseWhenNotExists() {
        assertFalse(adapter.existsById("not-exist"));
    }


    @Test
    void deleteById_shouldRemoveComment() {
        adapter.save(fakeComment);

        adapter.deleteById(fakeComment.getId());

        assertTrue(adapter.findById(fakeComment.getId()).isEmpty());
    }

    @Test
    void deleteById_shouldNotAffectOtherComments() {
        adapter.save(fakeComment);
        Comment other = new Comment(UUID.randomUUID().toString(), "user-02", "media-001", "Autre");
        adapter.save(other);

        adapter.deleteById(fakeComment.getId());

        assertTrue(adapter.findById(other.getId()).isPresent());
    }
}
