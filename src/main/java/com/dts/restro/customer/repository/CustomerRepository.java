package com.dts.restro.customer.repository;

import com.dts.restro.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find customer by phone number and restaurant
     */
    Optional<Customer> findByRestaurantIdAndPhone(Long restaurantId, String phone);
    
    /**
     * Find customer by email and restaurant
     */
    Optional<Customer> findByRestaurantIdAndEmail(Long restaurantId, String email);
    
    /**
     * Find customer by GSTIN and restaurant
     */
    Optional<Customer> findByRestaurantIdAndGstin(Long restaurantId, String gstin);
    
    /**
     * Find all customers for a restaurant
     */
    Page<Customer> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    /**
     * Find active customers for a restaurant
     */
    Page<Customer> findByRestaurantIdAndIsActive(Long restaurantId, Boolean isActive, Pageable pageable);
    
    /**
     * Search customers by name or phone
     */
    @Query("SELECT c FROM Customer c WHERE c.restaurantId = :restaurantId " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.phone LIKE CONCAT('%', :search, '%'))")
    Page<Customer> searchCustomers(@Param("restaurantId") Long restaurantId, 
                                   @Param("search") String search, 
                                   Pageable pageable);
    
    /**
     * Find customers with credit balance
     */
    @Query("SELECT c FROM Customer c WHERE c.restaurantId = :restaurantId " +
           "AND c.creditBalance > 0 ORDER BY c.creditBalance DESC")
    List<Customer> findCustomersWithCreditBalance(@Param("restaurantId") Long restaurantId);
    
    /**
     * Find top customers by total spent
     */
    @Query("SELECT c FROM Customer c WHERE c.restaurantId = :restaurantId " +
           "ORDER BY c.totalSpent DESC")
    Page<Customer> findTopCustomersBySpent(@Param("restaurantId") Long restaurantId, Pageable pageable);
    
    /**
     * Find customers with loyalty points
     */
    @Query("SELECT c FROM Customer c WHERE c.restaurantId = :restaurantId " +
           "AND c.loyaltyPoints >= :minPoints ORDER BY c.loyaltyPoints DESC")
    List<Customer> findCustomersWithLoyaltyPoints(@Param("restaurantId") Long restaurantId, 
                                                  @Param("minPoints") Integer minPoints);
    
    /**
     * Count active customers for a restaurant
     */
    Long countByRestaurantIdAndIsActive(Long restaurantId, Boolean isActive);
    
    /**
     * Get total credit balance for a restaurant
     */
    @Query("SELECT COALESCE(SUM(c.creditBalance), 0) FROM Customer c " +
           "WHERE c.restaurantId = :restaurantId AND c.isActive = true")
    BigDecimal getTotalCreditBalance(@Param("restaurantId") Long restaurantId);
    
    /**
     * Get total loyalty points for a restaurant
     */
    @Query("SELECT COALESCE(SUM(c.loyaltyPoints), 0) FROM Customer c " +
           "WHERE c.restaurantId = :restaurantId AND c.isActive = true")
    Long getTotalLoyaltyPoints(@Param("restaurantId") Long restaurantId);
    
    /**
     * Find customers by city
     */
    List<Customer> findByRestaurantIdAndCity(Long restaurantId, String city);
    
    /**
     * Find customers by state
     */
    List<Customer> findByRestaurantIdAndState(Long restaurantId, String state);
    
    /**
     * Check if phone number exists for restaurant
     */
    boolean existsByRestaurantIdAndPhone(Long restaurantId, String phone);
    
    /**
     * Check if email exists for restaurant
     */
    boolean existsByRestaurantIdAndEmail(Long restaurantId, String email);
    
    /**
     * Check if GSTIN exists for restaurant
     */
    boolean existsByRestaurantIdAndGstin(Long restaurantId, String gstin);

    /**
     * Count new customers registered between two timestamps
     */
    long countByRestaurantIdAndCreatedAtBetween(Long restaurantId,
                                                java.time.LocalDateTime start,
                                                java.time.LocalDateTime end);
}

// Made with Bob
