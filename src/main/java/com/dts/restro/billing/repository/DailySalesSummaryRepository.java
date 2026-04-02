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
    Optional<DailySalesSummary> findByRestaurantIdAndSalesDate(Long restaurantId, LocalDate salesDate);

    /**
     * Find summaries for a date range
     */
    List<DailySalesSummary> findByRestaurantIdAndSalesDateBetween(
            Long restaurantId, LocalDate startDate, LocalDate endDate);

    /**
     * Get total sales for a date range
     */
    @Query("SELECT COALESCE(SUM(d.totalRevenue), 0) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.salesDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSales(@Param("restaurantId") Long restaurantId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * Get total tax for a date range
     */
    @Query("SELECT COALESCE(SUM(d.totalTaxAmount), 0) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.salesDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalTax(@Param("restaurantId") Long restaurantId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    /**
     * Get total bills for a date range
     */
    @Query("SELECT COALESCE(SUM(d.totalBills), 0) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.salesDate BETWEEN :startDate AND :endDate")
    Integer getTotalOrders(@Param("restaurantId") Long restaurantId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    /**
     * Get average daily sales for a date range
     */
    @Query("SELECT AVG(d.totalRevenue) FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.salesDate BETWEEN :startDate AND :endDate")
    BigDecimal getAverageDailySales(@Param("restaurantId") Long restaurantId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Get monthly sales summary
     */
    @Query("SELECT YEAR(d.salesDate), MONTH(d.salesDate), SUM(d.totalRevenue) " +
           "FROM DailySalesSummary d " +
           "WHERE d.restaurantId = :restaurantId " +
           "AND d.salesDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(d.salesDate), MONTH(d.salesDate) " +
           "ORDER BY YEAR(d.salesDate), MONTH(d.salesDate)")
    List<Object[]> getMonthlySales(@Param("restaurantId") Long restaurantId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Check if summary exists for date
     */
    boolean existsByRestaurantIdAndSalesDate(Long restaurantId, LocalDate salesDate);

    /**
     * Delete summaries older than specified date
     */
    void deleteByRestaurantIdAndSalesDateBefore(Long restaurantId, LocalDate date);
}

// Made with Bob
