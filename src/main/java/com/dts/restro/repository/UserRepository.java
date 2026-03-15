package com.dts.restro.repository;

import com.dts.restro.common.annotation.SkipRestaurantFilter;
import com.dts.restro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Cross-tenant repository — must not have the restaurant filter applied,
 * as authentication queries need to find users across all restaurants.
 */
@SkipRestaurantFilter
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
