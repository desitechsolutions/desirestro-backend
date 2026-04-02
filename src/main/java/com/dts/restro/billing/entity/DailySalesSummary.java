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
    @Index(name = "idx_daily_sales", columnList = "restaurant_id, sales_date"),
    @Index(name = "idx_sale_date", columnList = "sales_date")
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

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    // Order statistics (by order type)
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

    // Bill counts
    @Column(name = "total_bills")
    @Builder.Default
    private Integer totalBills = 0;

    @Column(name = "paid_bills")
    @Builder.Default
    private Integer paidBills = 0;

    @Column(name = "pending_bills")
    @Builder.Default
    private Integer pendingBills = 0;

    @Column(name = "cancelled_bills")
    @Builder.Default
    private Integer cancelledBills = 0;

    // Revenue breakdown
    @Column(name = "total_revenue", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "subtotal_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "service_charge_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal serviceChargeAmount = BigDecimal.ZERO;

    @Column(name = "packaging_charge_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal packagingChargeAmount = BigDecimal.ZERO;

    @Column(name = "delivery_charge_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryChargeAmount = BigDecimal.ZERO;

    @Column(name = "round_off_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal roundOffAmount = BigDecimal.ZERO;

    @Column(name = "pending_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    // Tax breakdown
    @Column(name = "cgst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "total_tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalTaxAmount = BigDecimal.ZERO;

    // Payment method breakdown
    @Column(name = "cash_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "card_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cardAmount = BigDecimal.ZERO;

    @Column(name = "upi_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal upiAmount = BigDecimal.ZERO;

    @Column(name = "net_banking_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal netBankingAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    // Customer metrics
    @Column(name = "unique_customers")
    @Builder.Default
    private Integer uniqueCustomers = 0;

    @Column(name = "new_customers")
    @Builder.Default
    private Integer newCustomers = 0;

    @Column(name = "average_bill_value", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal averageBillValue = BigDecimal.ZERO;

    // Item metrics
    @Column(name = "total_items_sold")
    @Builder.Default
    private Integer totalItemsSold = 0;

    @Column(name = "unique_items_sold")
    @Builder.Default
    private Integer uniqueItemsSold = 0;

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
}

// Made with Bob
