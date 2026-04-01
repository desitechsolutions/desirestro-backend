package com.dts.restro.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base DTO for all reports
 * Contains common fields shared across different report types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    
    // Report Metadata
    private String reportType;
    private String reportTitle;
    private LocalDateTime generatedAt;
    private String generatedBy;
    
    // Restaurant Information
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantGSTIN;
    private String restaurantPhone;
    
    // Date Range
    private LocalDate startDate;
    private LocalDate endDate;
    private String period; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    
    // Filters Applied
    private String filters;
    
    // Export Information
    private String exportFormat; // PDF, EXCEL, CSV
    private String fileName;
    
    /**
     * Get formatted date range string
     */
    public String getDateRangeString() {
        if (startDate != null && endDate != null) {
            if (startDate.equals(endDate)) {
                return startDate.toString();
            }
            return startDate + " to " + endDate;
        }
        return "N/A";
    }
    
    /**
     * Get formatted generation timestamp
     */
    public String getFormattedGeneratedAt() {
        if (generatedAt != null) {
            return generatedAt.toString();
        }
        return LocalDateTime.now().toString();
    }
    
    /**
     * Check if report is for a single day
     */
    public boolean isSingleDayReport() {
        return startDate != null && endDate != null && startDate.equals(endDate);
    }
    
    /**
     * Get number of days in report period
     */
    public long getDaysInPeriod() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }
}

// Made with Bob
