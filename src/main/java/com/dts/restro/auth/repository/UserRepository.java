package com.dts.restro.auth.repository;

import com.dts.restro.common.annotation.SkipRestaurantFilter;
import com.dts.restro.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Cross-tenant repository — must not have the restaurant filter applied,
 * as authentication queries need to find users across all restaurants.
 */
@SkipRestaurantFilter
public interface UserRepository extends JpaRepository<User, Long> {

    // Optimized with FETCH JOIN to prevent LazyInitializationException during login
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.restaurant WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    Boolean existsByUsername(String username);

    // Optimized for login/forgot-password flows that check both fields
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.restaurant " +
            "WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    List<User> findByRestaurantId(Long restaurantId);

    long countByActiveTrue();

    long countByRestaurantId(Long restaurantId);

    long countByRestaurantIdAndActiveTrue(Long restaurantId);
}