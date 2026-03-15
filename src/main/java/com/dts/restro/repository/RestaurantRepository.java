package com.dts.restro.repository;

import com.dts.restro.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);
}
