package com.dts.restro.menu.repository;

import com.dts.restro.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdAndAvailableTrue(Long categoryId);
    List<MenuItem> findByAvailableTrue();
    List<MenuItem> findByName(String name);
    List<MenuItem> findByNameContainingIgnoreCaseAndAvailableTrue(String name);

    @Query("SELECT m FROM MenuItem m JOIN FETCH m.category WHERE m.available = true")
    List<MenuItem> findAvailableWithCategory();

    @Query("SELECT m FROM MenuItem m JOIN FETCH m.category WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) AND m.available = true")
    List<MenuItem> findAvailableByNameWithCategory(@Param("search") String search);

    long countByRestaurantId(Long restaurantId);
}