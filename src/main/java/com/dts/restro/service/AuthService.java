package com.dts.restro.service;

import com.dts.restro.dto.RegisterRequest;
import com.dts.restro.entity.Restaurant;
import com.dts.restro.entity.Role;
import com.dts.restro.entity.User;
import com.dts.restro.exception.BusinessValidationException;
import com.dts.restro.exception.DuplicateResourceException;
import com.dts.restro.repository.RestaurantRepository;
import com.dts.restro.repository.UserRepository;
import com.dts.restro.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       RestaurantRepository restaurantRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user.
     *
     * <p>If {@code request.getRestaurantName()} is provided a new restaurant
     * tenant is created atomically and the user is assigned the OWNER role.
     * Otherwise the user is treated as staff to be linked to a restaurant
     * by an OWNER after creation.</p>
     */
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        Restaurant restaurant = null;

        if (request.getRestaurantName() != null && !request.getRestaurantName().isBlank()) {
            // New restaurant owner registration
            String code = generateRestaurantCode(request.getRestaurantName());

            restaurant = Restaurant.builder()
                    .name(request.getRestaurantName())
                    .address(request.getRestaurantAddress())
                    .phone(request.getRestaurantPhone())
                    .email(request.getRestaurantEmail())
                    .state(request.getRestaurantState())
                    .gstin(request.getGstin())
                    .code(code)
                    .build();

            restaurant = restaurantRepository.save(restaurant);
            log.info("New restaurant created: id={} name={} code={}", restaurant.getId(), restaurant.getName(), code);
        }

        Role role = (restaurant != null) ? Role.OWNER : request.getRole();

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .restaurant(restaurant)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: username={} role={} restaurantId={}",
                saved.getUsername(), saved.getRole(),
                restaurant != null ? restaurant.getId() : "none");
        return saved;
    }

    /**
     * Authenticates a user and returns a JWT access token with the tenant's
     * {@code restaurantId} embedded.
     */
    public String authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Long restaurantId = (user.getRestaurant() != null) ? user.getRestaurant().getId() : null;
        return jwtUtil.generateToken(userDetails, restaurantId);
    }

    /**
     * Fetches the User entity by username (used in AuthController for building the response).
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /** Changes password after verifying the current one. */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", username);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Generates a URL-safe, unique restaurant code from the restaurant name. */
    private String generateRestaurantCode(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(WHITESPACE.matcher(normalized).replaceAll("-"))
                .replaceAll("")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        // Append short UUID suffix to ensure global uniqueness
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String code = slug + "-" + suffix;

        // Guard against collisions (extremely unlikely with UUID suffix but belt-and-suspenders)
        while (restaurantRepository.existsByCode(code)) {
            code = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return code;
    }
}
