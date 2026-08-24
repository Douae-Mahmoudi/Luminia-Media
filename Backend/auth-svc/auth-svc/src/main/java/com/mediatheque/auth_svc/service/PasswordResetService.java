package com.mediatheque.auth_svc.service;

import com.mediatheque.auth_svc.model.PasswordResetToken;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.PasswordResetTokenRepository;
import com.mediatheque.auth_svc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        // Supprimer les anciens tokens avant d'en créer un nouveau
        tokenRepository.deleteByUser_Id(user.getId());

        String code = generateSixDigitCode();

        PasswordResetToken token = PasswordResetToken.builder()
                .code(code)
                .user(user)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(token);
        emailService.sendPasswordResetCode(email, code);
    }


    @Transactional
    public void verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        PasswordResetToken resetToken = tokenRepository.findByUser_IdAndUsedFalse(user.getId())
                .orElseThrow(() -> new RuntimeException("Aucun code actif trouvé"));

        validateToken(resetToken, code);
    }



    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        PasswordResetToken resetToken = tokenRepository.findByUser_IdAndUsedFalse(user.getId())
                .orElseThrow(() -> new RuntimeException("Aucun code actif trouvé"));

        validateToken(resetToken, code);

        // Changer le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }


    private void validateToken(PasswordResetToken token, String code) {
        if (!token.getCode().equals(code)) {
            throw new RuntimeException("Code incorrect");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new RuntimeException("Code expiré");
        }
    }


    private String generateSixDigitCode() {
        int number = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}
