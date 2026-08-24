package com.mediatheque.auth_svc.repository;

import com.mediatheque.auth_svc.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    void deleteByUser_Id(Long userId);
    Optional<PasswordResetToken> findByUser_IdAndUsedFalse(Long userId);
}