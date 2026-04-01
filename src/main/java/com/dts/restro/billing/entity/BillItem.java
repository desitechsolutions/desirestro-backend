package com.dts.restro.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bill Item entity representing individual items in a bill
 */
@Entity
@Table(name = "bill_item", indexes = {
    @Index(name = "idx_bill_item", columnList = "bill_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bill_id", nullable = false)
    private Long billId;
    
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;
    
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;
    
    @Column(name = "item_code", length = 50)
    private String itemCode;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "item_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal itemTotal;
    
    @Column(name = "spice_level", length = 20)
    private String spiceLevel;
    
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    @Column(name = "is_veg")
    @Builder.Default
    private Boolean isVeg = true;
    
    @Column(name = "is_jain")
    @Builder.Default
    private Boolean isJain = false;
    
    @Column(name = "hsn_code", length = 20)
    private String hsnCode;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Calculate item total if not set
        if (itemTotal == null && unitPrice != null && quantity != null) {
            itemTotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    /**
     * Calculate item total
     */
    public void calculateTotal() {
        if (unitPrice != null && quantity != null) {
            itemTotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}

// Made with Bob
