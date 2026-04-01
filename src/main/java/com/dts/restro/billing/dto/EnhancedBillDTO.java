package com.dts.restro.billing.dto;

import com.dts.restro.billing.enums.OrderType;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.enums.TaxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Bill DTO with Indian restaurant features
 * Includes CGST/SGST/IGST, service charge, packaging, discounts, and round-off
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedBillDTO {
    
    private Long billId;
    private String billNumber;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantGstin;
    private String restaurantAddress;
    private String restaurantPhone;
    
    // Order details
    private Long orderId;
    private String kotNumber;
    private Integer tableNumber;
    private String tableName;
    private OrderType orderType;
    private LocalDateTime orderTime;
    private LocalDateTime billTime;
    
    // Customer details
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerGstin;
    private String customerAddress;
    
    // Items
    private List<BillItemDTO> items;
    
    // Amount breakdown
    private BigDecimal subtotal;           // Sum of all items before tax
    private BigDecimal taxableAmount;      // Amount on which tax is calculated
    
    // Tax details
    private TaxType taxType;               // CGST_SGST or IGST
    private BigDecimal cgstRate;           // 9% for CGST_SGST, 0 for IGST
    private BigDecimal sgstRate;           // 9% for CGST_SGST, 0 for IGST
    private BigDecimal igstRate;           // 0 for CGST_SGST, 18% for IGST
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalTax;           // Sum of all taxes
    
    // Additional charges
    private BigDecimal serviceChargeRate;  // Usually 10%
    private BigDecimal serviceChargeAmount;
    private BigDecimal packagingCharges;   // For takeaway/delivery
    private BigDecimal deliveryCharges;    // For delivery orders
    
    // Discounts
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private String discountReason;
    
    // Final amounts
    private BigDecimal totalBeforeRoundOff;
    private BigDecimal roundOffAmount;     // Can be positive or negative
    private BigDecimal grandTotal;
    
    // Payment details
    private PaymentMethod paymentMethod;
    private String paymentReference;       // UPI transaction ID, card last 4 digits, etc.
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    
    // Staff details
    private String captainName;
    private String cashierName;
    
    // Additional info
    private String notes;
    private Boolean isPaid;
    private Boolean isCancelled;
    private String cancellationReason;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillItemDTO {
        private Long itemId;
        private String itemName;
        private String itemCode;
        private String category;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal itemTotal;
        private String spiceLevel;
        private String specialInstructions;
        private Boolean isVeg;
        private Boolean isJain;
        private String hsnCode;            // HSN code for GST
    }
    
    /**
     * Calculate all amounts based on items and charges
     */
    public void calculateAmounts() {
        // Calculate subtotal from items
        this.subtotal = items.stream()
            .map(BillItemDTO::getItemTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate service charge
        if (serviceChargeRate != null && serviceChargeRate.compareTo(BigDecimal.ZERO) > 0) {
            this.serviceChargeAmount = subtotal.multiply(serviceChargeRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.serviceChargeAmount = BigDecimal.ZERO;
        }
        
        // Taxable amount = subtotal + service charge
        this.taxableAmount = subtotal.add(serviceChargeAmount);
        
        // Calculate discount
        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = taxableAmount.multiply(discountRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        } else if (discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
        
        // Amount after discount
        BigDecimal amountAfterDiscount = taxableAmount.subtract(discountAmount);
        
        // Calculate tax based on tax type
        if (taxType == TaxType.CGST_SGST) {
            this.cgstRate = new BigDecimal("9.00");
            this.sgstRate = new BigDecimal("9.00");
            this.igstRate = BigDecimal.ZERO;
            
            this.cgstAmount = amountAfterDiscount.multiply(cgstRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            this.sgstAmount = amountAfterDiscount.multiply(sgstRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            this.igstAmount = BigDecimal.ZERO;
        } else if (taxType == TaxType.IGST) {
            this.cgstRate = BigDecimal.ZERO;
            this.sgstRate = BigDecimal.ZERO;
            this.igstRate = new BigDecimal("18.00");
            
            this.cgstAmount = BigDecimal.ZERO;
            this.sgstAmount = BigDecimal.ZERO;
            this.igstAmount = amountAfterDiscount.multiply(igstRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            // NO_TAX
            this.cgstRate = BigDecimal.ZERO;
            this.sgstRate = BigDecimal.ZERO;
            this.igstRate = BigDecimal.ZERO;
            this.cgstAmount = BigDecimal.ZERO;
            this.sgstAmount = BigDecimal.ZERO;
            this.igstAmount = BigDecimal.ZERO;
        }
        
        this.totalTax = cgstAmount.add(sgstAmount).add(igstAmount);
        
        // Add packaging and delivery charges
        if (packagingCharges == null) {
            this.packagingCharges = BigDecimal.ZERO;
        }
        if (deliveryCharges == null) {
            this.deliveryCharges = BigDecimal.ZERO;
        }
        
        // Calculate total before round-off
        this.totalBeforeRoundOff = amountAfterDiscount
            .add(totalTax)
            .add(packagingCharges)
            .add(deliveryCharges);
        
        // Calculate round-off (to nearest rupee)
        BigDecimal rounded = totalBeforeRoundOff.setScale(0, BigDecimal.ROUND_HALF_UP);
        this.roundOffAmount = rounded.subtract(totalBeforeRoundOff);
        
        // Grand total
        this.grandTotal = rounded;
        
        // Calculate change if paid amount is provided
        if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.changeAmount = paidAmount.subtract(grandTotal);
        }
    }
}

// Made with Bob
