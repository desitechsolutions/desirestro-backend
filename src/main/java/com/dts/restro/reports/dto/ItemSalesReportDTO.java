package com.dts.restro.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for Item-wise Sales Report
 * Contains detailed sales analysis for menu items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSalesReportDTO {
    
    // Report Metadata
    private ReportDTO metadata;
    
    // Summary
    private Integer totalItemsSold;
    private Integer uniqueItemsCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageItemPrice;
    
    // Detailed Item Sales
    private List<ItemSalesDetail> items;
    
    // Category Summary
    private Map<String, CategorySummary> categorySummary;
    
    // Top and Bottom Performers
    private List<ItemSalesDetail> topSellingItems;
    private List<ItemSalesDetail> slowMovingItems;
    
    /**
     * Item Sales Detail
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemSalesDetail {
        private Long menuItemId;
        private String itemName;
        private String category;
        private String hsnCode;
        private Boolean isVeg;
        
        // Sales Metrics
        private Integer quantitySold;
        private BigDecimal unitPrice;
        private BigDecimal totalRevenue;
        private BigDecimal averageSellingPrice;
        
        // Analysis
        private Double percentageOfTotalRevenue;
        private Double percentageOfTotalQuantity;
        private String trend; // UP, DOWN, STABLE
        private Integer rank; // Ranking by revenue
        
        // Comparison
        private Integer previousPeriodQuantity;
        private BigDecimal previousPeriodRevenue;
        private Double growthPercentage;
    }
    
    /**
     * Category Summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String categoryName;
        private Integer itemCount;
        private Integer totalQuantitySold;
        private BigDecimal totalRevenue;
        private BigDecimal averageItemPrice;
        private Double percentageOfTotal;
        private String topSellingItem;
        private Integer topSellingItemQuantity;
    }
}

// Made with Bob
