package com.tempo.application.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.refreshToken.RefreshToken;
import com.tempo.application.model.user.User;


public interface ResfreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteAllByUser(User user);
}
