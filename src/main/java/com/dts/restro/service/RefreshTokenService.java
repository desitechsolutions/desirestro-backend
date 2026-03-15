package com.dts.restro.service;

import com.dts.restro.entity.RefreshToken;
import com.dts.restro.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages creation, rotation and deletion of JWT refresh tokens.
 * Only one active refresh token per user is kept — a new login replaces
 * any previously issued token for that username.
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /** Creates (or replaces) the refresh token for a given user. */
    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);

        RefreshToken token = new RefreshToken();
        token.setUsername(username);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the token string, rotates it (old → deleted, new → persisted)
     * and returns the fresh token entity.
     *
     * @throws IllegalArgumentException if the token does not exist
     * @throws IllegalStateException    if the token has expired
     */
    public RefreshToken rotateRefreshToken(String tokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (existing.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existing);
            throw new IllegalStateException("Refresh token expired. Please log in again.");
        }

        // Rotate: delete old, create new
        refreshTokenRepository.delete(existing);
        return createRefreshToken(existing.getUsername());
    }

    /** Deletes refresh token(s) for a user on logout or password change. */
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    /** Deletes a specific refresh token by its value (e.g. from a cookie). */
    public void deleteByToken(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }
}
