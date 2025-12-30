package com.dts.restro.controller;

import com.dts.restro.dto.AuthRequest;
import com.dts.restro.dto.AuthResponse;
import com.dts.restro.dto.RegisterRequest;
import com.dts.restro.entity.User;
import com.dts.restro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // For React frontend
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    public AuthController(UserDetailsService userDetailsService, AuthService authService) {
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        User user = (User) userDetailsService.loadUserByUsername(request.getUsername());
        AuthResponse response = new AuthResponse(token, user.getRole().name(), user.getFullName());
        return ResponseEntity.ok(response);
    }
}