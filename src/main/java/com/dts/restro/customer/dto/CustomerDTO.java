package com.dts.restro.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Customer entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    
    private Long id;
    private Long restaurantId;
    private String name;
    private String phone;
    private String email;
    private String gstin;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private BigDecimal creditLimit;
    private BigDecimal creditBalance;
    private BigDecimal availableCredit;
    private Integer loyaltyPoints;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Made with Bob
