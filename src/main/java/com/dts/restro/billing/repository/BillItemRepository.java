package com.dts.restro.billing.repository;

import com.dts.restro.billing.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BillItem entity
 */
@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    
    /**
     * Find all items for a bill
     */
    List<BillItem> findByBillId(Long billId);
    
    /**
     * Find items by menu item ID
     */
    List<BillItem> findByMenuItemId(Long menuItemId);
    
    /**
     * Get top selling items for a restaurant
     */
    @Query("SELECT bi.menuItemId, bi.itemName, SUM(bi.quantity) as totalQuantity, " +
           "SUM(bi.itemTotal) as totalRevenue " +
           "FROM BillItem bi " +
           "JOIN Bill b ON bi.billId = b.id " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isPaid = true AND b.isCancelled = false " +
           "GROUP BY bi.menuItemId, bi.itemName " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> getTopSellingItems(@Param("restaurantId") Long restaurantId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get sales by category
     */
    @Query("SELECT bi.category, SUM(bi.itemTotal) as totalRevenue " +
           "FROM BillItem bi " +
           "JOIN Bill b ON bi.billId = b.id " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND b.billTime BETWEEN :startDate AND :endDate " +
           "AND b.isPaid = true AND b.isCancelled = false " +
           "GROUP BY bi.category " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getSalesByCategory(@Param("restaurantId") Long restaurantId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Delete all items for a bill
     */
    void deleteByBillId(Long billId);
}

// Made with Bob
