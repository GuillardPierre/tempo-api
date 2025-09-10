package com.tempo.application.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tempo.application.model.passwordReset.PasswordResetToken;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.PasswordResetTokenRepository;
import com.tempo.application.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final int OTP_LENGTH = 6;
    private static final int DEFAULT_TTL_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmail(email.toLowerCase());
        // Réponse générique même si l'utilisateur n'existe pas
        if (user == null) {
            return;
        }

        String code = generateNumericCode(OTP_LENGTH);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCodeHash(encoder.encode(code));
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(Duration.ofMinutes(DEFAULT_TTL_MINUTES)));
        token.setAttempts(0);
        tokenRepository.save(token);

        emailService.sendPasswordResetCode(user.getEmail(), code, DEFAULT_TTL_MINUTES);
    }

    @Transactional
    public boolean confirm(String email, String code, String newPasswordPlain) {
        User user = userRepository.findByEmail(email.toLowerCase());
        if (user == null) {
            return false;
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findFirstByUserAndConsumedAtIsNullOrderByCreatedAtDesc(user);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        PasswordResetToken token = tokenOpt.get();

        if (token.getConsumedAt() != null) {
            return false;
        }
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }
        if (token.getAttempts() != null && token.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        boolean matches = encoder.matches(code, token.getCodeHash());
        token.setAttempts((token.getAttempts() == null ? 0 : token.getAttempts()) + 1);

        if (!matches) {
            tokenRepository.save(token);
            return false;
        }

        // contrôle mot de passe min 4 chars (exigence utilisateur)
        if (newPasswordPlain == null || newPasswordPlain.length() < 4) {
            return false;
        }

        user.setPassword(encoder.encode(newPasswordPlain));
        token.setConsumedAt(Instant.now());
        tokenRepository.save(token);
        userRepository.save(user);

        // Invalider tous les refresh tokens existants pour cet utilisateur
        refreshTokenService.deleteByUser(user);

        return true;
    }

    private String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int digit = secureRandom.nextInt(10);
            sb.append(digit);
        }
        return sb.toString();
    }
}
