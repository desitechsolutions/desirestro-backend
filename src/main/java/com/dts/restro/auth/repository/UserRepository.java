package com.dts.restro.auth.repository;

import com.dts.restro.common.annotation.SkipRestaurantFilter;
import com.dts.restro.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Cross-tenant repository — must not have the restaurant filter applied,
 * as authentication queries need to find users across all restaurants.
 */
@SkipRestaurantFilter
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
}
