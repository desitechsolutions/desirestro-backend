package com.dts.restro.staff.repository;

import com.dts.restro.staff.entity.Leave;
import com.dts.restro.staff.entity.Staff;
import com.dts.restro.staff.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    
    /**
     * Find leaves by status
     */
    List<Leave> findByStatus(LeaveStatus status);
    
    /**
     * Find leaves by restaurant ID and status
     */
    @Query("SELECT l FROM Leave l WHERE l.restaurantId = :restaurantId AND l.status = :status ORDER BY l.appliedDate DESC")
    List<Leave> findByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") LeaveStatus status);
    
    /**
     * Find leaves by staff ordered by applied date descending
     */
    List<Leave> findByStaffOrderByAppliedDateDesc(Staff staff);
    
    /**
     * Find overlapping leaves for a staff member
     * Excludes rejected leaves
     */
    @Query("SELECT l FROM Leave l WHERE l.staff.id = :staffId " +
           "AND l.status != 'REJECTED' " +
           "AND ((l.fromDate <= :toDate AND l.toDate >= :fromDate))")
    List<Leave> findOverlappingLeaves(
        @Param("staffId") Long staffId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
    
    /**
     * Find leaves by restaurant ID and date range
     */
    @Query("SELECT l FROM Leave l WHERE l.restaurantId = :restaurantId " +
           "AND ((l.fromDate <= :endDate AND l.toDate >= :startDate)) " +
           "ORDER BY l.fromDate DESC")
    List<Leave> findByRestaurantIdAndDateRange(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Count pending leaves by restaurant ID
     */
    @Query("SELECT COUNT(l) FROM Leave l WHERE l.restaurantId = :restaurantId AND l.status = 'PENDING'")
    long countPendingLeavesByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    /**
     * Find approved leaves for a staff member in a date range
     */
    @Query("SELECT l FROM Leave l WHERE l.staff.id = :staffId " +
           "AND l.status = 'APPROVED' " +
           "AND ((l.fromDate <= :endDate AND l.toDate >= :startDate))")
    List<Leave> findApprovedLeavesByStaffAndDateRange(
        @Param("staffId") Long staffId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

// Made with Bob
