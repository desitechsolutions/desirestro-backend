package com.dts.restro.reports.dto;

import com.dts.restro.billing.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for Sales Reports (Daily/Monthly)
 * Contains comprehensive sales data and breakdowns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {
    
    // Report Metadata (from base)
    private ReportDTO metadata;
    
    // Summary Metrics
    private Integer totalBills;
    private Integer paidBills;
    private Integer pendingBills;
    private Integer cancelledBills;
    
    // Revenue Breakdown
    private BigDecimal totalRevenue;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal serviceChargeAmount;
    private BigDecimal packagingChargeAmount;
    private BigDecimal deliveryChargeAmount;
    
    // Tax Breakdown
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalTaxAmount;
    
    // Payment Method Breakdown
    private Map<PaymentMethod, BigDecimal> paymentMethodBreakdown;
    private BigDecimal cashAmount;
    private BigDecimal cardAmount;
    private BigDecimal upiAmount;
    private BigDecimal netBankingAmount;
    private BigDecimal creditAmount;
    
    // Customer Metrics
    private Integer uniqueCustomers;
    private Integer newCustomers;
    private BigDecimal averageBillValue;
    private Integer repeatCustomers;
    
    // Item Metrics
    private Integer totalItemsSold;
    private Integer uniqueItemsSold;
    private BigDecimal averageItemsPerBill;
    
    // Top Performers
    private List<TopItemDTO> topSellingItems;
    private List<TopCategoryDTO> topCategories;
    private List<TopCustomerDTO> topCustomers;
    
    // Time-based Analysis (for hourly reports)
    private List<HourlySalesDTO> hourlySales;
    
    // Comparative Data
    private ComparisonDTO comparison;
    
    // Additional Metrics
    private BigDecimal roundOffAmount;
    private BigDecimal pendingAmount;
    private Double collectionEfficiency; // (Paid / Total) * 100
    private Double growthRate; // Compared to previous period
    
    /**
     * Calculate collection efficiency
     */
    public void calculateCollectionEfficiency() {
        if (totalBills != null && totalBills > 0 && paidBills != null) {
            this.collectionEfficiency = (paidBills.doubleValue() / totalBills.doubleValue()) * 100;
        } else {
            this.collectionEfficiency = 0.0;
        }
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
     * Calculate average items per bill
     */
    public void calculateAverageItemsPerBill() {
        if (totalBills != null && totalBills > 0 && totalItemsSold != null) {
            this.averageItemsPerBill = BigDecimal.valueOf(
                totalItemsSold.doubleValue() / totalBills.doubleValue()
            ).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageItemsPerBill = BigDecimal.ZERO;
        }
    }
    
    // Inner DTOs for nested data
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopItemDTO {
        private Long menuItemId;
        private String itemName;
        private String category;
        private Integer quantitySold;
        private BigDecimal revenue;
        private Double percentageOfTotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCategoryDTO {
        private String categoryName;
        private Integer itemsSold;
        private BigDecimal revenue;
        private Double percentageOfTotal;
        private Integer uniqueItems;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerDTO {
        private Long customerId;
        private String customerName;
        private String phone;
        private Integer orderCount;
        private BigDecimal totalSpent;
        private BigDecimal averageOrderValue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlySalesDTO {
        private Integer hour; // 0-23
        private String timeSlot; // "10:00 AM - 11:00 AM"
        private Integer billCount;
        private BigDecimal revenue;
        private Integer itemsSold;
        private BigDecimal averageBillValue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonDTO {
        private String comparisonPeriod; // "Previous Day", "Previous Month", etc.
        private BigDecimal previousRevenue;
        private BigDecimal currentRevenue;
        private BigDecimal difference;
        private Double growthPercentage;
        private String trend; // "UP", "DOWN", "STABLE"
        private Double revenueGrowth;
        private Integer previousBills;
        private Integer currentBills;
        private Double billGrowth;
    }
}

// Made with Bob
