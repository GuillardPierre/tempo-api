package com.tempo.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tempo.application.exceptions.TokenRefreshException;
import com.tempo.application.model.refreshToken.RefreshToken;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.ResfreshTokenRepository;
import com.tempo.application.repository.UserRepository;

@Service
public class RefreshTokenService {
    
    @Autowired
    ResfreshTokenRepository resfreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email);   
        resfreshTokenRepository.findByUser(user)
            .ifPresent(token -> resfreshTokenRepository.delete(token));
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(1000 * 60 * 60 * 24 * 7)) // 7 days
                .build();
        return resfreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return resfreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().compareTo(Instant.now()) < 0) {
            resfreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign in request");
        }
        return token;
    }

    public void deleteByUser(User user) {
        resfreshTokenRepository.deleteAllByUser(user);
    }
}
