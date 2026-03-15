package com.dts.restro.auth.repository;

import com.dts.restro.common.annotation.SkipRestaurantFilter;
import com.dts.restro.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Cross-tenant repository — refresh tokens are not scoped by restaurant. */
@SkipRestaurantFilter
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.username = :username")
    void deleteByUsername(String username);
}
