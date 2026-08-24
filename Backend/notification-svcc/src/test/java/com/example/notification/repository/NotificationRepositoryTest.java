package com.example.notification.repository;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository repository;

    private static final Long USER_1 = 1L;
    private static final Long USER_2 = 2L;



    private void insertRaw(Long userId, NotificationType type, NotificationStatus status,
                           LocalDateTime createdAt) {
        entityManager.getEntityManager()
                .createNativeQuery(
                        "INSERT INTO notifications (user_id, type, status, message, created_at) " +
                                "VALUES (?, ?, ?, ?, ?)")
                .setParameter(1, userId)
                .setParameter(2, type.name())
                .setParameter(3, status.name())
                .setParameter(4, "msg-" + type + "-" + userId)
                .setParameter(5, createdAt)
                .executeUpdate();
    }


    @BeforeEach
    void setUp() {
        LocalDateTime base = LocalDateTime.now();

        insertRaw(USER_1, NotificationType.MEDIA_LIKED,   NotificationStatus.UNREAD, base.minusHours(3));
        insertRaw(USER_1, NotificationType.COMMENT_ADDED, NotificationStatus.UNREAD, base.minusHours(2));
        insertRaw(USER_1, NotificationType.RECO_READY,    NotificationStatus.READ,   base.minusHours(1));

        insertRaw(USER_2, NotificationType.BROADCAST,   NotificationStatus.UNREAD, base.minusHours(5));
        insertRaw(USER_2, NotificationType.MEDIA_LIKED, NotificationStatus.READ,   base.minusHours(4));

        entityManager.flush();
        entityManager.clear();
    }



    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc — returns only user's notifications")
    void findByUserId_returnsOnlyThatUser() {
        Page<Notification> page = repository.findByUserIdOrderByCreatedAtDesc(USER_1, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Notification::getUserId).containsOnly(USER_1);
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc — results are ordered newest first")
    void findByUserId_orderedByCreatedAtDesc() {
        Page<Notification> page = repository.findByUserIdOrderByCreatedAtDesc(USER_1, PageRequest.of(0, 10));

        List<LocalDateTime> dates = page.getContent().stream().map(Notification::getCreatedAt).toList();
        for (int i = 0; i < dates.size() - 1; i++) {
            assertThat(dates.get(i)).isAfterOrEqualTo(dates.get(i + 1));
        }
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc — pagination works correctly")
    void findByUserId_paginationWorks() {
        Page<Notification> firstPage  = repository.findByUserIdOrderByCreatedAtDesc(USER_1, PageRequest.of(0, 2));
        Page<Notification> secondPage = repository.findByUserIdOrderByCreatedAtDesc(USER_1, PageRequest.of(1, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc — returns empty page for unknown user")
    void findByUserId_unknownUser_emptyPage() {
        Page<Notification> page = repository.findByUserIdOrderByCreatedAtDesc(999L, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }


    @Test
    @DisplayName("countByUserIdAndStatus — counts UNREAD correctly for user 1")
    void countByUserIdAndStatus_unread() {
        long count = repository.countByUserIdAndStatus(USER_1, NotificationStatus.UNREAD);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByUserIdAndStatus — counts READ correctly for user 1")
    void countByUserIdAndStatus_read() {
        long count = repository.countByUserIdAndStatus(USER_1, NotificationStatus.READ);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countByUserIdAndStatus — returns 0 for unknown user")
    void countByUserIdAndStatus_unknownUser_zero() {
        long count = repository.countByUserIdAndStatus(999L, NotificationStatus.UNREAD);
        assertThat(count).isZero();
    }


    @Test
    @DisplayName("markAsRead — marks target notification as READ and sets readAt")
    void markAsRead_success() {
        Notification target = repository
                .findByUserIdOrderByCreatedAtDesc(USER_1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .findFirst().orElseThrow();

        int updated = repository.markAsRead(target.getId(), USER_1, LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        Notification refreshed = repository.findById(target.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(refreshed.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsRead — returns 0 when notification belongs to another user")
    void markAsRead_wrongUser_returnsZero() {
        Notification user1Notif = repository
                .findByUserIdOrderByCreatedAtDesc(USER_1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .findFirst().orElseThrow();

        int updated = repository.markAsRead(user1Notif.getId(), USER_2, LocalDateTime.now());
        assertThat(updated).isZero();
    }

    @Test
    @DisplayName("markAsRead — returns 0 for non-existent notification id")
    void markAsRead_unknownId_returnsZero() {
        int updated = repository.markAsRead(9999L, USER_1, LocalDateTime.now());
        assertThat(updated).isZero();
    }


    @Test
    @DisplayName("markAllAsRead — marks all UNREAD notifications for user as READ")
    void markAllAsRead_updatesAllUnread() {
        int updated = repository.markAllAsRead(USER_1, LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        assertThat(updated).isEqualTo(2);
        long remaining = repository.countByUserIdAndStatus(USER_1, NotificationStatus.UNREAD);
        assertThat(remaining).isZero();
    }

    @Test
    @DisplayName("markAllAsRead — does not affect another user's notifications")
    void markAllAsRead_doesNotAffectOtherUsers() {
        repository.markAllAsRead(USER_1, LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        long user2Unread = repository.countByUserIdAndStatus(USER_2, NotificationStatus.UNREAD);
        assertThat(user2Unread).isEqualTo(1);
    }

    @Test
    @DisplayName("markAllAsRead — returns 0 for user with no UNREAD notifications")
    void markAllAsRead_noUnread_returnsZero() {
        repository.markAllAsRead(USER_2, LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        int updated = repository.markAllAsRead(USER_2, LocalDateTime.now());
        assertThat(updated).isZero();
    }


    @Test
    @DisplayName("countByType — returns one entry per distinct type")
    void countByType_returnsGroupedResults() {
        List<Object[]> result = repository.countByType();

        assertThat(result).isNotEmpty();
        Object[] mediaLikedRow = result.stream()
                .filter(r -> r[0] == NotificationType.MEDIA_LIKED)
                .findFirst().orElseThrow();
        assertThat((Long) mediaLikedRow[1]).isEqualTo(2L);
    }

    @Test
    @DisplayName("countByType — total across all types equals total notification count")
    void countByType_totalMatchesAllNotifications() {
        List<Object[]> result = repository.countByType();
        long total = result.stream().mapToLong(r -> (Long) r[1]).sum();
        assertThat(total).isEqualTo(repository.count());
    }


    @Test
    @DisplayName("countRead — returns correct number of READ notifications across all users")
    void countRead_correctValue() {
        long readCount = repository.countRead();
        assertThat(readCount).isEqualTo(2);
    }

    @Test
    @DisplayName("countRead — increases after markAllAsRead")
    void countRead_increasesAfterMarkAllAsRead() {
        long before = repository.countRead();
        repository.markAllAsRead(USER_1, LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();
        long after = repository.countRead();
        assertThat(after).isEqualTo(before + 2);
    }


    @Test
    @DisplayName("findByTypeOrderByCreatedAtDesc — returns only matching type")
    void findByType_returnsOnlyMatchingType() {
        Page<Notification> page = repository.findByTypeOrderByCreatedAtDesc(
                NotificationType.MEDIA_LIKED, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Notification::getType)
                .containsOnly(NotificationType.MEDIA_LIKED);
    }

    @Test
    @DisplayName("findByTypeOrderByCreatedAtDesc — results ordered newest first")
    void findByType_orderedByCreatedAtDesc() {
        Page<Notification> page = repository.findByTypeOrderByCreatedAtDesc(
                NotificationType.MEDIA_LIKED, Pageable.unpaged());

        List<LocalDateTime> dates = page.getContent().stream().map(Notification::getCreatedAt).toList();
        for (int i = 0; i < dates.size() - 1; i++) {
            assertThat(dates.get(i)).isAfterOrEqualTo(dates.get(i + 1));
        }
    }

    @Test
    @DisplayName("findByTypeOrderByCreatedAtDesc — returns empty page for type with no notifications")
    void findByType_noMatch_emptyPage() {
        Page<Notification> page = repository.findByTypeOrderByCreatedAtDesc(
                NotificationType.MEDIA_ACCEPTED, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }


    @Test
    @DisplayName("findDistinctUserIds — returns exactly the users that have notifications")
    void findDistinctUserIds_returnsBothUsers() {
        List<Long> userIds = repository.findDistinctUserIds();
        assertThat(userIds).containsExactlyInAnyOrder(USER_1, USER_2);
    }

    @Test
    @DisplayName("findDistinctUserIds — no duplicates even if user has multiple notifications")
    void findDistinctUserIds_noDuplicates() {
        List<Long> userIds = repository.findDistinctUserIds();
        assertThat(userIds).doesNotHaveDuplicates();
    }



    @Test
    @DisplayName("@PrePersist — createdAt is set automatically, status defaults to UNREAD")
    void prePersist_setsDefaults() {
        Notification n = Notification.builder()
                .userId(3L)
                .type(NotificationType.BROADCAST)
                .message("test prePersist")
                .build();

        Notification saved = entityManager.persistFlushFind(n);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }
}
