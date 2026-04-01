package com.dts.restro.billing.repository;

import com.dts.restro.billing.entity.Bill;
import com.dts.restro.billing.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Bill entity
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    /**
     * Find bill by bill number and restaurant
     */
    Optional<Bill> findByRestaurantIdAndBillNumber(Long restaurantId, String billNumber);
    
    /**
     * Find bill by order ID
     */
    Optional<Bill> findByOrderId(Long orderId);
    
    /**
     * Find all bills for a restaurant
     */
    Page<Bill> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    /**
     * Find bills by payment status
     */
    Page<Bill> findByRestaurantIdAndIsPaid(Long restaurantId, Boolean isPaid, Pageable pageable);
    
    /**
     * Find bills by date range
     */
    @Query("SELECT b FROM Bill b WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "ORDER BY b.billTime DESC")
    Page<Bill> findByDateRange(@Param("restaurantId") Long restaurantId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               Pageable pageable);
    
    /**
     * Find bills by customer
     */
    Page<Bill> findByRestaurantIdAndCustomerId(Long restaurantId, Long customerId, Pageable pageable);
    
    /**
     * Find bills by payment method
     */
    Page<Bill> findByRestaurantIdAndPaymentMethod(Long restaurantId, PaymentMethod paymentMethod, Pageable pageable);
    
    /**
     * Find unpaid bills
     */
    @Query("SELECT b FROM Bill b WHERE b.restaurantId = :restaurantId " +
           "AND b.isPaid = false AND b.isCancelled = false " +
           "ORDER BY b.billTime DESC")
    List<Bill> findUnpaidBills(@Param("restaurantId") Long restaurantId);
    
    /**
     * Get total sales for a date range
     */
    @Query("SELECT COALESCE(SUM(b.grandTotal), 0) FROM Bill b " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isPaid = true AND b.isCancelled = false")
    BigDecimal getTotalSales(@Param("restaurantId") Long restaurantId,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total tax collected for a date range
     */
    @Query("SELECT COALESCE(SUM(b.totalTax), 0) FROM Bill b " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isPaid = true AND b.isCancelled = false")
    BigDecimal getTotalTax(@Param("restaurantId") Long restaurantId,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count bills for a date range
     */
    @Query("SELECT COUNT(b) FROM Bill b " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isCancelled = false")
    Long countBillsByDateRange(@Param("restaurantId") Long restaurantId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get sales by payment method
     */
    @Query("SELECT b.paymentMethod, COALESCE(SUM(b.grandTotal), 0) FROM Bill b " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isPaid = true AND b.isCancelled = false " +
           "GROUP BY b.paymentMethod")
    List<Object[]> getSalesByPaymentMethod(@Param("restaurantId") Long restaurantId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get average bill value
     */
    @Query("SELECT AVG(b.grandTotal) FROM Bill b " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isPaid = true AND b.isCancelled = false")
    BigDecimal getAverageBillValue(@Param("restaurantId") Long restaurantId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Check if bill number exists
     */
    boolean existsByRestaurantIdAndBillNumber(Long restaurantId, String billNumber);
}

// Made with Bob
