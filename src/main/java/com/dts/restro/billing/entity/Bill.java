package com.dts.restro.billing.entity;

import com.dts.restro.billing.enums.BillStatus;
import com.dts.restro.billing.enums.OrderType;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.enums.TaxType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bill entity for restaurant billing with Indian GST support
 */
@Entity
@Table(name = "bill", indexes = {
    @Index(name = "idx_bill_number", columnList = "restaurant_id, bill_number"),
    @Index(name = "idx_bill_date", columnList = "bill_time"),
    @Index(name = "idx_bill_payment", columnList = "payment_method, is_paid"),
    @Index(name = "idx_bill_status", columnList = "is_paid, is_cancelled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    
    @Column(name = "bill_number", nullable = false, length = 50)
    private String billNumber;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "table_number")
    private Integer tableNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 20)
    @Builder.Default
    private OrderType orderType = OrderType.DINE_IN;
    
    // Amount breakdown
    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "taxable_amount", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxableAmount = BigDecimal.ZERO;
    
    // Tax details
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", length = 20)
    @Builder.Default
    private TaxType taxType = TaxType.CGST_SGST;
    
    @Column(name = "cgst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cgstRate = new BigDecimal("9.00");
    
    @Column(name = "sgst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal sgstRate = new BigDecimal("9.00");
    
    @Column(name = "igst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal igstRate = BigDecimal.ZERO;
    
    @Column(name = "cgst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;
    
    @Column(name = "sgst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;
    
    @Column(name = "igst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;
    
    @Column(name = "total_tax", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalTax = BigDecimal.ZERO;
    
    // Additional charges
    @Column(name = "service_charge_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal serviceChargeRate = new BigDecimal("10.00");
    
    @Column(name = "service_charge_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal serviceChargeAmount = BigDecimal.ZERO;
    
    @Column(name = "packaging_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal packagingCharges = BigDecimal.ZERO;
    
    @Column(name = "delivery_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryCharges = BigDecimal.ZERO;
    
    // Discounts
    @Column(name = "discount_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_reason", length = 200)
    private String discountReason;
    
    // Final amounts
    @Column(name = "total_before_round_off", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalBeforeRoundOff;
    
    @Column(name = "round_off_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal roundOffAmount = BigDecimal.ZERO;
    
    @Column(name = "grand_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal grandTotal;
    
    // Payment details
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;
    
    @Column(name = "paid_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Column(name = "change_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal changeAmount = BigDecimal.ZERO;
    
    // Staff details
    @Column(name = "captain_id")
    private Long captainId;
    
    @Column(name = "cashier_id")
    private Long cashierId;
    
    // Status
    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = false;
    
    @Column(name = "is_cancelled")
    @Builder.Default
    private Boolean isCancelled = false;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    // Timestamps
    @Column(name = "bill_time")
    private LocalDateTime billTime;
    
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (billTime == null) {
            billTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark bill as paid
     */
    public void markAsPaid(PaymentMethod method, BigDecimal amount, String reference) {
        this.isPaid = true;
        this.paymentMethod = method;
        this.paidAmount = amount;
        this.paymentReference = reference;
        this.paymentTime = LocalDateTime.now();
        
        if (amount.compareTo(grandTotal) > 0) {
            this.changeAmount = amount.subtract(grandTotal);
        } else {
            this.changeAmount = BigDecimal.ZERO;
        }
    }

    /** Compatibility alias: returns billTime */
    public LocalDateTime getBillDate() {
        return billTime;
    }

    /** Compatibility alias: returns grandTotal */
    public BigDecimal getFinalAmount() {
        return grandTotal;
    }

    /**
     * Cancel bill
     */
    public void cancel(String reason) {
        this.isCancelled = true;
        this.cancellationReason = reason;
    }

    /**
     * Get computed bill status based on isPaid / isCancelled flags
     */
    public BillStatus getStatus() {
        if (Boolean.TRUE.equals(isCancelled)) return BillStatus.CANCELLED;
        if (Boolean.TRUE.equals(isPaid)) return BillStatus.PAID;
        return BillStatus.PENDING;
    }
}

// Made with Bob

// Made with Bob
