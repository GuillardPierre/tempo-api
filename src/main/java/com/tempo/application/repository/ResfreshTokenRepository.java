package com.tempo.application.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.refreshToken.RefreshToken;


public interface ResfreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
}
