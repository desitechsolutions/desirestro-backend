package com.dts.restro.auth.service;

import com.dts.restro.auth.dto.ForgotPasswordRequest;
import com.dts.restro.auth.dto.ResetPasswordRequest;
import com.dts.restro.auth.entity.PasswordResetToken;
import com.dts.restro.auth.entity.User;
import com.dts.restro.auth.repository.PasswordResetTokenRepository;
import com.dts.restro.auth.repository.UserRepository;
import com.dts.restro.exception.BusinessValidationException;
import com.dts.restro.restaurant.entity.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    @Value("${app.reset-token-expiration-minutes:30}")
    private int resetTokenExpirationMinutes;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElse(null);

        if (user == null) {
            log.warn("Password reset requested for unknown user/email: {}", request.getUsernameOrEmail());
            return;
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Password reset attempted for user {} but no email on file", user.getUsername());
            throw new BusinessValidationException(
                    "No email address on file for this account. Please contact your administrator.");
        }

        Restaurant restaurant = user.getRestaurant();
        if (restaurant == null) {
            log.warn("Password reset for SUPER_ADMIN {} without restaurant context — skipping", user.getUsername());
            throw new BusinessValidationException(
                    "Password reset is not available for this account type. Please contact your administrator.");
        }

        tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .forEach(tokenRepository::delete);

        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenStr);
        token.setUser(user);
        token.setRestaurant(restaurant);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));
        token.setUsed(false);
        tokenRepository.save(token);

        String resetLink = frontendUrl + "/reset-password?token=" + tokenStr;

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                resetLink,
                restaurant
        );

        log.info("Password reset initiated for user: {}", user.getUsername());
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessValidationException("Invalid or expired reset token."));

        if (token.isUsed()) {
            throw new BusinessValidationException("This reset link has already been used.");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new BusinessValidationException("Reset link has expired. Please request a new one.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessValidationException("Password must be at least 6 characters.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        if (user.getEmail() != null) {
            emailService.sendPasswordResetConfirmationEmail(
                    user.getEmail(),
                    user.getFullName() != null ? user.getFullName() : user.getUsername(),
                    token.getRestaurant()
            );
        }

        log.info("Password reset completed for user: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String tokenStr) {
        return tokenRepository.findByToken(tokenStr)
                .map(t -> !t.isUsed() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
