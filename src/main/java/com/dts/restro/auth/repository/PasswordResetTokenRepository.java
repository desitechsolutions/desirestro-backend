package com.dts.restro.auth.repository;

import com.dts.restro.auth.entity.PasswordResetToken;
import com.dts.restro.common.annotation.SkipRestaurantFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@SkipRestaurantFilter
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT t FROM PasswordResetToken t JOIN FETCH t.user JOIN FETCH t.restaurant WHERE t.token = :token")
    Optional<PasswordResetToken> findByToken(@Param("token") String token);
}
