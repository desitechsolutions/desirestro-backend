package com.dts.restro.staff.repository;

import com.dts.restro.staff.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * Find attendance by staff ID and date with null clock-out
     */
    Optional<Attendance> findByStaffIdAndDateAndClockOutIsNull(Long staffId, LocalDate date);
    
    /**
     * Find all attendance records for a specific date
     */
    List<Attendance> findByDate(LocalDate date);
    
    /**
     * Find attendance by restaurant ID and date
     */
    @Query("SELECT a FROM Attendance a WHERE a.restaurant.id = :restaurantId AND a.date = :date ORDER BY a.clockIn DESC")
    List<Attendance> findByRestaurantIdAndDate(@Param("restaurantId") Long restaurantId, @Param("date") LocalDate date);
    
    /**
     * Count active attendance for today (clocked in but not clocked out)
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.staff.id = :staffId AND a.date = :date AND a.clockOut IS NULL")
    long countActiveAttendanceForToday(@Param("staffId") Long staffId, @Param("date") LocalDate date);
    
    /**
     * Find attendance by staff ID and date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.staff.id = :staffId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<Attendance> findByStaffIdAndDateBetween(
        @Param("staffId") Long staffId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find attendance by restaurant ID and date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.restaurant.id = :restaurantId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC, a.clockIn DESC")
    List<Attendance> findByRestaurantIdAndDateBetween(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

// Made with Bob
