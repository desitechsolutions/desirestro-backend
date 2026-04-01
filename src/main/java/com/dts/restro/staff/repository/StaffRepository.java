package com.dts.restro.staff.repository;

import com.dts.restro.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    
    /**
     * Find all staff by restaurant ID ordered by join date descending
     */
    @Query("SELECT s FROM Staff s WHERE s.restaurantId = :restaurantId ORDER BY s.joinDate DESC")
    List<Staff> findAllByRestaurantIdOrderByJoinDateDesc(@Param("restaurantId") Long restaurantId);
    
    /**
     * Find staff by ID and restaurant ID
     */
    @Query("SELECT s FROM Staff s WHERE s.id = :id AND s.restaurantId = :restaurantId")
    Optional<Staff> findByIdAndRestaurantId(@Param("id") Long id, @Param("restaurantId") Long restaurantId);
    
    /**
     * Count staff by restaurant ID
     */
    @Query("SELECT COUNT(s) FROM Staff s WHERE s.restaurantId = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    /**
     * Find staff by user ID
     */
    @Query("SELECT s FROM Staff s WHERE s.user.id = :userId")
    Optional<Staff> findByUserId(@Param("userId") Long userId);
}

// Made with Bob
