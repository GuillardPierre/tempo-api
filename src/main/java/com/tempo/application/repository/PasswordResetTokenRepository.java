package com.tempo.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.passwordReset.PasswordResetToken;
import com.tempo.application.model.user.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findFirstByUserAndConsumedAtIsNullOrderByCreatedAtDesc(User user);
}
