package com.dts.restro.billing.dto;

import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.enums.TaxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for Daily Sales Summary
 * Contains aggregated sales data for a restaurant on a specific date
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesSummaryDTO {
    
    private Long id;
    private Long restaurantId;
    private LocalDate salesDate;
    
    // Bill Counts
    private Integer totalBills;
    private Integer paidBills;
    private Integer pendingBills;
    private Integer cancelledBills;
    
    // Revenue Breakdown
    private BigDecimal totalRevenue;           // Total of all paid bills
    private BigDecimal subtotalAmount;         // Sum of all item subtotals
    private BigDecimal discountAmount;         // Total discounts given
    private BigDecimal serviceChargeAmount;    // Total service charges
    private BigDecimal packagingChargeAmount;  // Total packaging charges
    private BigDecimal deliveryChargeAmount;   // Total delivery charges
    
    // Tax Breakdown
    private BigDecimal cgstAmount;             // Total CGST collected
    private BigDecimal sgstAmount;             // Total SGST collected
    private BigDecimal igstAmount;             // Total IGST collected
    private BigDecimal totalTaxAmount;         // Total tax collected
    
    // Payment Method Breakdown
    private BigDecimal cashAmount;
    private BigDecimal cardAmount;
    private BigDecimal upiAmount;
    private BigDecimal netBankingAmount;
    private BigDecimal creditAmount;           // Bills paid using customer credit
    
    // Customer Metrics
    private Integer uniqueCustomers;           // Number of unique customers served
    private Integer newCustomers;              // New customers registered today
    private BigDecimal averageBillValue;       // Average bill amount
    
    // Item Metrics
    private Integer totalItemsSold;            // Total quantity of items sold
    private Integer uniqueItemsSold;           // Number of different menu items sold
    
    // Additional Metrics
    private BigDecimal roundOffAmount;         // Total round-off adjustments
    private BigDecimal pendingAmount;          // Total amount in pending bills
    
    // Timestamps
    private String createdAt;
    private String updatedAt;
    
    /**
     * Calculate total tax amount from individual tax components
     */
    public void calculateTotalTax() {
        this.totalTaxAmount = BigDecimal.ZERO
            .add(cgstAmount != null ? cgstAmount : BigDecimal.ZERO)
            .add(sgstAmount != null ? sgstAmount : BigDecimal.ZERO)
            .add(igstAmount != null ? igstAmount : BigDecimal.ZERO);
    }
    
    /**
     * Calculate average bill value
     */
    public void calculateAverageBillValue() {
        if (paidBills != null && paidBills > 0 && totalRevenue != null) {
            this.averageBillValue = totalRevenue.divide(
                BigDecimal.valueOf(paidBills), 
                2, 
                BigDecimal.ROUND_HALF_UP
            );
        } else {
            this.averageBillValue = BigDecimal.ZERO;
        }
    }
    
    /**
     * Get payment method breakdown as a map
     */
    public Map<PaymentMethod, BigDecimal> getPaymentMethodBreakdown() {
        return Map.of(
            PaymentMethod.CASH, cashAmount != null ? cashAmount : BigDecimal.ZERO,
            PaymentMethod.CARD, cardAmount != null ? cardAmount : BigDecimal.ZERO,
            PaymentMethod.UPI, upiAmount != null ? upiAmount : BigDecimal.ZERO,
            PaymentMethod.NET_BANKING, netBankingAmount != null ? netBankingAmount : BigDecimal.ZERO,
            PaymentMethod.CREDIT, creditAmount != null ? creditAmount : BigDecimal.ZERO
        );
    }
    
    /**
     * Get tax breakdown as a map
     */
    public Map<String, BigDecimal> getTaxBreakdown() {
        return Map.of(
            "CGST", cgstAmount != null ? cgstAmount : BigDecimal.ZERO,
            "SGST", sgstAmount != null ? sgstAmount : BigDecimal.ZERO,
            "IGST", igstAmount != null ? igstAmount : BigDecimal.ZERO,
            "TOTAL", totalTaxAmount != null ? totalTaxAmount : BigDecimal.ZERO
        );
    }
    
    /**
     * Get revenue breakdown as a map
     */
    public Map<String, BigDecimal> getRevenueBreakdown() {
        return Map.of(
            "Subtotal", subtotalAmount != null ? subtotalAmount : BigDecimal.ZERO,
            "Discount", discountAmount != null ? discountAmount : BigDecimal.ZERO,
            "Service Charge", serviceChargeAmount != null ? serviceChargeAmount : BigDecimal.ZERO,
            "Packaging Charge", packagingChargeAmount != null ? packagingChargeAmount : BigDecimal.ZERO,
            "Delivery Charge", deliveryChargeAmount != null ? deliveryChargeAmount : BigDecimal.ZERO,
            "Tax", totalTaxAmount != null ? totalTaxAmount : BigDecimal.ZERO,
            "Round Off", roundOffAmount != null ? roundOffAmount : BigDecimal.ZERO,
            "Total", totalRevenue != null ? totalRevenue : BigDecimal.ZERO
        );
    }
    
    /**
     * Get bill status breakdown as a map
     */
    public Map<String, Integer> getBillStatusBreakdown() {
        return Map.of(
            "Total", totalBills != null ? totalBills : 0,
            "Paid", paidBills != null ? paidBills : 0,
            "Pending", pendingBills != null ? pendingBills : 0,
            "Cancelled", cancelledBills != null ? cancelledBills : 0
        );
    }
    
    /**
     * Check if this is a valid summary (has data)
     */
    public boolean hasData() {
        return totalBills != null && totalBills > 0;
    }
    
    /**
     * Get collection efficiency percentage
     * (Paid bills / Total bills) * 100
     */
    public Double getCollectionEfficiency() {
        if (totalBills != null && totalBills > 0 && paidBills != null) {
            return (paidBills.doubleValue() / totalBills.doubleValue()) * 100;
        }
        return 0.0;
    }
    
    /**
     * Get average items per bill
     */
    public Double getAverageItemsPerBill() {
        if (totalBills != null && totalBills > 0 && totalItemsSold != null) {
            return totalItemsSold.doubleValue() / totalBills.doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Initialize all BigDecimal fields to ZERO if null
     */
    public void initializeNullFields() {
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (subtotalAmount == null) subtotalAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (serviceChargeAmount == null) serviceChargeAmount = BigDecimal.ZERO;
        if (packagingChargeAmount == null) packagingChargeAmount = BigDecimal.ZERO;
        if (deliveryChargeAmount == null) deliveryChargeAmount = BigDecimal.ZERO;
        if (cgstAmount == null) cgstAmount = BigDecimal.ZERO;
        if (sgstAmount == null) sgstAmount = BigDecimal.ZERO;
        if (igstAmount == null) igstAmount = BigDecimal.ZERO;
        if (totalTaxAmount == null) totalTaxAmount = BigDecimal.ZERO;
        if (cashAmount == null) cashAmount = BigDecimal.ZERO;
        if (cardAmount == null) cardAmount = BigDecimal.ZERO;
        if (upiAmount == null) upiAmount = BigDecimal.ZERO;
        if (netBankingAmount == null) netBankingAmount = BigDecimal.ZERO;
        if (creditAmount == null) creditAmount = BigDecimal.ZERO;
        if (averageBillValue == null) averageBillValue = BigDecimal.ZERO;
        if (roundOffAmount == null) roundOffAmount = BigDecimal.ZERO;
        if (pendingAmount == null) pendingAmount = BigDecimal.ZERO;
        
        if (totalBills == null) totalBills = 0;
        if (paidBills == null) paidBills = 0;
        if (pendingBills == null) pendingBills = 0;
        if (cancelledBills == null) cancelledBills = 0;
        if (uniqueCustomers == null) uniqueCustomers = 0;
        if (newCustomers == null) newCustomers = 0;
        if (totalItemsSold == null) totalItemsSold = 0;
        if (uniqueItemsSold == null) uniqueItemsSold = 0;
    }
}

// Made with Bob
