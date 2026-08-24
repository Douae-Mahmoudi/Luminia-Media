package com.example.notification.consumer;

import com.example.notification.domain.enums.NotificationType;
import com.example.notification.domain.enums.ReferenceType;
import com.example.notification.event.CommentAddedEvent;
import com.example.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CollectionEventConsumer collectionEventConsumer;


    private CommentAddedEvent commentEvent(Long mediaId, Long ownerId, Long commenterId,
                                           String username, String title, String content) {
        CommentAddedEvent e = new CommentAddedEvent();
        e.setMediaId(mediaId);
        e.setOwnerId(ownerId);
        e.setCommentedByUserId(commenterId);
        e.setCommentedByUsername(username);
        e.setMediaTitle(title);
        e.setCommentContent(content);
        return e;
    }



    @Nested
    @DisplayName("onCommentAdded()")
    class OnCommentAddedTests {

        @Test
        @DisplayName("should send COMMENT_ADDED notification when commenter is not the owner")
        void shouldSendNotificationWhenCommenterIsNotOwner() {
            collectionEventConsumer.onCommentAdded(
                    commentEvent(10L, 1L, 2L, "alice", "Mon film", "Très beau travail !"), "comment-added");

            verify(notificationService).send(
                    eq(1L),
                    eq(NotificationType.COMMENT_ADDED),
                    eq("alice a commenté votre média \"Mon film\" : \"Très beau travail !\""),
                    eq(10L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should NOT send notification when commenter is the owner (self-comment)")
        void shouldIgnoreWhenCommenterIsOwner() {
            collectionEventConsumer.onCommentAdded(
                    commentEvent(10L, 1L, 1L, "alice", "Mon film", "Mon propre commentaire"), "comment-added");

            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("should send notification without preview when commentContent is null")
        void shouldSendWithoutPreviewWhenContentIsNull() {
            collectionEventConsumer.onCommentAdded(
                    commentEvent(5L, 1L, 2L, "bob", "Clip", null), "comment-added");

            verify(notificationService).send(
                    eq(1L),
                    eq(NotificationType.COMMENT_ADDED),
                    eq("bob a commenté votre média \"Clip\""),
                    eq(5L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should truncate preview when comment exceeds 60 characters")
        void shouldTruncatePreviewWhenTooLong() {
            String longComment    = "a".repeat(61);
            String expectedPreview = "a".repeat(60) + "…";

            collectionEventConsumer.onCommentAdded(
                    commentEvent(7L, 1L, 3L, "charlie", "Doc", longComment), "comment-added");

            verify(notificationService).send(
                    eq(1L),
                    eq(NotificationType.COMMENT_ADDED),
                    eq("charlie a commenté votre média \"Doc\" : \"" + expectedPreview + "\""),
                    eq(7L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should NOT truncate when comment is exactly 60 characters")
        void shouldNotTruncateWhenExactly60Chars() {
            String exactComment = "b".repeat(60);

            collectionEventConsumer.onCommentAdded(
                    commentEvent(8L, 1L, 4L, "diana", "Série", exactComment), "comment-added");

            verify(notificationService).send(
                    eq(1L),
                    eq(NotificationType.COMMENT_ADDED),
                    eq("diana a commenté votre média \"Série\" : \"" + exactComment + "\""),
                    eq(8L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should send notification with short comment without truncation")
        void shouldSendWithShortComment() {
            collectionEventConsumer.onCommentAdded(
                    commentEvent(9L, 1L, 5L, "eve", "Court", ""), "comment-added");

            verify(notificationService).send(
                    eq(1L),
                    eq(NotificationType.COMMENT_ADDED),
                    eq("eve a commenté votre média \"Court\" : \"\""),
                    eq(9L),
                    eq(ReferenceType.MEDIA)
            );
        }

        @Test
        @DisplayName("should call notificationService exactly once per valid event")
        void shouldCallServiceExactlyOnce() {
            collectionEventConsumer.onCommentAdded(
                    commentEvent(1L, 1L, 2L, "x", "y", "z"), "comment-added");

            verify(notificationService, times(1)).send(any(), any(), any(), any(), any());
        }
    }
}