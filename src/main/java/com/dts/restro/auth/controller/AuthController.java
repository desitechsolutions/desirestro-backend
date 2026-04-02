package com.dts.restro.auth.controller;

import com.dts.restro.common.ApiResponse;
import com.dts.restro.auth.dto.AuthRequest;
import com.dts.restro.auth.dto.AuthResponse;
import com.dts.restro.auth.dto.ChangePasswordRequest;
import com.dts.restro.auth.dto.RegisterRequest;
import com.dts.restro.auth.dto.ForgotPasswordRequest;
import com.dts.restro.auth.dto.ResetPasswordRequest;
import com.dts.restro.auth.entity.RefreshToken;
import com.dts.restro.auth.entity.User;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.auth.security.JwtUtil;
import com.dts.restro.auth.service.AuthService;
import com.dts.restro.auth.service.RefreshTokenService;
import com.dts.restro.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, token refresh and logout")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired
    private PasswordResetService passwordResetService;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          JwtUtil jwtUtil) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new restaurant + owner.
     * Supply {@code restaurantName} to create a new tenant; omit it to add staff.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new restaurant owner (creates restaurant tenant)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        Restaurant restaurant = user.getRestaurant();
        Long restaurantId = restaurant != null ? restaurant.getId() : null;
        String accessToken = jwtUtil.generateToken(user, restaurantId);

        AuthResponse body = buildAuthResponse(user, accessToken);

        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken.getToken()).toString())
                .body(ApiResponse.success(body, "Registration successful"));
    }

    /** Authenticates a user and issues access + refresh tokens. */
    @PostMapping("/login")
    @Operation(summary = "Login — returns access token and sets refresh-token cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        String accessToken = authService.authenticate(request.getUsername(), request.getPassword());
        User user = authService.getUserByUsername(request.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        log.info("Login successful: user={} restaurant={}",
                user.getUsername(),
                user.getRestaurant() != null ? user.getRestaurant().getId() : "none");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken.getToken()).toString())
                .body(ApiResponse.success(buildAuthResponse(user, accessToken), "Login successful"));
    }

    /** Rotates the refresh token and issues a new access token. */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using the HTTP-only refresh-token cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenValue) {

        if (refreshTokenValue == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Refresh token missing"));
        }

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenValue);
        User user = authService.getUserByUsername(newRefreshToken.getUsername());

        Long restaurantId = user.getRestaurant() != null ? user.getRestaurant().getId() : null;
        String newAccessToken = jwtUtil.generateToken(user, restaurantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(newRefreshToken.getToken()).toString())
                .body(ApiResponse.success(buildAuthResponse(user, newAccessToken), "Token refreshed"));
    }

    /** Logs out the user by revoking the refresh token. */
    @PostMapping("/logout")
    @Operation(summary = "Logout — revokes refresh token and clears cookie")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenValue) {

        if (refreshTokenValue != null) {
            refreshTokenService.deleteByToken(refreshTokenValue);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(null, "Logged out successfully"));
    }

    /** Changes the authenticated user's password and invalidates all refresh tokens. */
    @PostMapping("/change-password")
    @Operation(summary = "Change password for the authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        authService.changePassword(
                authentication.getName(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        refreshTokenService.deleteByUsername(authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(null, "Password changed. Please log in again."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset link via email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiatePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(
            null, "If an account with that username/email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful. Please log in."));
    }

    @GetMapping("/validate-reset-token")
    @Operation(summary = "Check if a password reset token is still valid")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
        boolean valid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(valid, "Token validation"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, String accessToken) {
        Restaurant restaurant = user.getRestaurant();
        return new AuthResponse(
                accessToken,
                user.getRole().name(),
                user.getFullName(),
                restaurant != null ? restaurant.getId() : null,
                restaurant != null ? restaurant.getName() : null
        );
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .sameSite("Strict")
                .maxAge(0)
                .build();
    }
}

