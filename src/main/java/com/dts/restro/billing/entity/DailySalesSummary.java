package com.dts.restro.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily Sales Summary entity for reporting and analytics
 * Aggregates daily sales data for quick reporting
 */
@Entity
@Table(name = "daily_sales_summary", indexes = {
    @Index(name = "idx_daily_sales", columnList = "restaurant_id, sale_date"),
    @Index(name = "idx_sale_date", columnList = "sale_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    
    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;
    
    // Order statistics
    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;
    
    @Column(name = "dine_in_orders")
    @Builder.Default
    private Integer dineInOrders = 0;
    
    @Column(name = "takeaway_orders")
    @Builder.Default
    private Integer takeawayOrders = 0;
    
    @Column(name = "delivery_orders")
    @Builder.Default
    private Integer deliveryOrders = 0;
    
    @Column(name = "cancelled_orders")
    @Builder.Default
    private Integer cancelledOrders = 0;
    
    // Revenue breakdown
    @Column(name = "gross_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal grossSales = BigDecimal.ZERO;
    
    @Column(name = "total_discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    
    @Column(name = "net_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal netSales = BigDecimal.ZERO;
    
    // Tax breakdown
    @Column(name = "total_cgst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCgst = BigDecimal.ZERO;
    
    @Column(name = "total_sgst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalSgst = BigDecimal.ZERO;
    
    @Column(name = "total_igst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalIgst = BigDecimal.ZERO;
    
    @Column(name = "total_tax", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalTax = BigDecimal.ZERO;
    
    // Additional charges
    @Column(name = "total_service_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalServiceCharge = BigDecimal.ZERO;
    
    @Column(name = "total_packaging_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPackagingCharges = BigDecimal.ZERO;
    
    @Column(name = "total_delivery_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDeliveryCharges = BigDecimal.ZERO;
    
    // Payment method breakdown
    @Column(name = "cash_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cashSales = BigDecimal.ZERO;
    
    @Column(name = "upi_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal upiSales = BigDecimal.ZERO;
    
    @Column(name = "card_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cardSales = BigDecimal.ZERO;
    
    @Column(name = "wallet_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal walletSales = BigDecimal.ZERO;
    
    @Column(name = "credit_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal creditSales = BigDecimal.ZERO;
    
    @Column(name = "online_sales", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal onlineSales = BigDecimal.ZERO;
    
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
     * Calculate net sales from gross sales and discount
     */
    public void calculateNetSales() {
        if (grossSales != null && totalDiscount != null) {
            netSales = grossSales.subtract(totalDiscount);
        }
    }
    
    /**
     * Calculate total tax from CGST, SGST, and IGST
     */
    public void calculateTotalTax() {
        totalTax = BigDecimal.ZERO;
        if (totalCgst != null) totalTax = totalTax.add(totalCgst);
        if (totalSgst != null) totalTax = totalTax.add(totalSgst);
        if (totalIgst != null) totalTax = totalTax.add(totalIgst);
    }
}

// Made with Bob
