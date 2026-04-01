package com.dts.restro.billing.repository;

import com.dts.restro.billing.entity.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailySalesSummary entity
 */
@Repository
public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Long> {
    
    /**
     * Find summary by restaurant and date
     */
    Optional<DailySalesSummary> findByRestaurantIdAndSaleDate(Long restaurantId, LocalDate saleDate);
    
    /**
     * Find summaries for a date range
     */
    List<DailySalesSummary> findByRestaurantIdAndSaleDateBetweenOrderBySaleDateDesc(
            Long restaurantId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get total sales for a date range
     */
    @Query("SELECT COALESCE(SUM(d.netSales), 0) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSales(@Param("restaurantId") Long restaurantId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);
    
    /**
     * Get total tax for a date range
     */
    @Query("SELECT COALESCE(SUM(d.totalTax), 0) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalTax(@Param("restaurantId") Long restaurantId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);
    
    /**
     * Get total orders for a date range
     */
    @Query("SELECT COALESCE(SUM(d.totalOrders), 0) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.saleDate BETWEEN :startDate AND :endDate")
    Integer getTotalOrders(@Param("restaurantId") Long restaurantId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);
    
    /**
     * Get average daily sales for a date range
     */
    @Query("SELECT AVG(d.netSales) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getAverageDailySales(@Param("restaurantId") Long restaurantId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly sales summary
     */
    @Query("SELECT YEAR(d.saleDate), MONTH(d.saleDate), SUM(d.netSales) " +
           "FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.saleDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(d.saleDate), MONTH(d.saleDate) " +
           "ORDER BY YEAR(d.saleDate), MONTH(d.saleDate)")
    List<Object[]> getMonthlySales(@Param("restaurantId") Long restaurantId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Check if summary exists for date
     */
    boolean existsByRestaurantIdAndSaleDate(Long restaurantId, LocalDate saleDate);
    
    /**
     * Delete summaries older than specified date
     */
    void deleteByRestaurantIdAndSaleDateBefore(Long restaurantId, LocalDate date);
}

// Made with Bob
