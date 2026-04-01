package com.dts.restro.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer entity for managing restaurant customers
 * Supports credit accounts, loyalty points, and GSTIN for business customers
 */
@Entity
@Table(name = "customer", indexes = {
    @Index(name = "idx_customer_phone", columnList = "phone"),
    @Index(name = "idx_customer_name", columnList = "name"),
    @Index(name = "idx_customer_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "gstin", length = 15)
    private String gstin;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "city", length = 50)
    private String city;
    
    @Column(name = "state", length = 50)
    private String state;
    
    @Column(name = "pincode", length = 10)
    private String pincode;
    
    @Column(name = "credit_limit", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;
    
    @Column(name = "credit_balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal creditBalance = BigDecimal.ZERO;
    
    @Column(name = "loyalty_points")
    @Builder.Default
    private Integer loyaltyPoints = 0;
    
    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;
    
    @Column(name = "total_spent", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if customer has available credit
     */
    public boolean hasAvailableCredit(BigDecimal amount) {
        if (creditLimit == null || creditBalance == null) {
            return false;
        }
        BigDecimal availableCredit = creditLimit.subtract(creditBalance);
        return availableCredit.compareTo(amount) >= 0;
    }
    
    /**
     * Add credit balance
     */
    public void addCreditBalance(BigDecimal amount) {
        if (creditBalance == null) {
            creditBalance = BigDecimal.ZERO;
        }
        creditBalance = creditBalance.add(amount);
    }
    
    /**
     * Reduce credit balance (when payment is made)
     */
    public void reduceCreditBalance(BigDecimal amount) {
        if (creditBalance == null) {
            creditBalance = BigDecimal.ZERO;
        }
        creditBalance = creditBalance.subtract(amount);
        if (creditBalance.compareTo(BigDecimal.ZERO) < 0) {
            creditBalance = BigDecimal.ZERO;
        }
    }
    
    /**
     * Add loyalty points
     */
    public void addLoyaltyPoints(Integer points) {
        if (loyaltyPoints == null) {
            loyaltyPoints = 0;
        }
        loyaltyPoints += points;
    }
    
    /**
     * Redeem loyalty points
     */
    public boolean redeemLoyaltyPoints(Integer points) {
        if (loyaltyPoints == null || loyaltyPoints < points) {
            return false;
        }
        loyaltyPoints -= points;
        return true;
    }
    
    /**
     * Update order statistics
     */
    public void updateOrderStats(BigDecimal orderAmount) {
        if (totalOrders == null) {
            totalOrders = 0;
        }
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        totalOrders++;
        totalSpent = totalSpent.add(orderAmount);
    }
}

// Made with Bob
