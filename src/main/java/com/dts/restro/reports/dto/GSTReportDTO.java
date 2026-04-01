package com.dts.restro.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for GST Report (GSTR-1 Format)
 * Contains GST compliance data for filing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GSTReportDTO {
    
    // Report Metadata
    private ReportDTO metadata;
    
    // GST Period
    private String gstPeriod; // MM-YYYY format
    private Integer month;
    private Integer year;
    
    // Restaurant GST Details
    private String gstin;
    private String legalName;
    private String tradeName;
    private String address;
    private String stateCode;
    
    // GSTR-1 Sections
    private List<B2BInvoice> b2bInvoices;      // Business to Business
    private List<B2CInvoice> b2cInvoices;      // Business to Consumer
    private HSNSummary hsnSummary;              // HSN-wise summary
    
    // Tax Summary
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCGST;
    private BigDecimal totalSGST;
    private BigDecimal totalIGST;
    private BigDecimal totalTax;
    private BigDecimal totalInvoiceValue;
    
    // Invoice Counts
    private Integer totalB2BInvoices;
    private Integer totalB2CInvoices;
    private Integer totalInvoices;
    
    // Tax Liability
    private BigDecimal taxLiability;
    private BigDecimal inputTaxCredit; // If applicable
    private BigDecimal netTaxPayable;
    
    /**
     * B2B Invoice (Business to Business)
     * For customers with GSTIN
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class B2BInvoice {
        private String customerGSTIN;
        private String customerName;
        private String customerState;
        private String invoiceNumber;
        private LocalDate invoiceDate;
        private String invoiceType; // Regular, Debit Note, Credit Note
        
        // Amounts
        private BigDecimal taxableValue;
        private BigDecimal cgstRate;
        private BigDecimal cgstAmount;
        private BigDecimal sgstRate;
        private BigDecimal sgstAmount;
        private BigDecimal igstRate;
        private BigDecimal igstAmount;
        private BigDecimal totalTax;
        private BigDecimal invoiceValue;
        
        // Additional Info
        private String placeOfSupply;
        private Boolean isReverseCharge;
        private String hsnCode;
    }
    
    /**
     * B2C Invoice (Business to Consumer)
     * For customers without GSTIN
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class B2CInvoice {
        private String invoiceNumber;
        private LocalDate invoiceDate;
        private String invoiceType;
        
        // Amounts
        private BigDecimal taxableValue;
        private BigDecimal cgstRate;
        private BigDecimal cgstAmount;
        private BigDecimal sgstRate;
        private BigDecimal sgstAmount;
        private BigDecimal totalTax;
        private BigDecimal invoiceValue;
        
        // Additional Info
        private String placeOfSupply;
        private String eCommerceGSTIN; // If applicable
    }
    
    /**
     * HSN Summary
     * HSN-wise summary of outward supplies
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HSNSummary {
        private List<HSNDetail> hsnDetails;
        private BigDecimal totalTaxableValue;
        private BigDecimal totalTax;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HSNDetail {
            private String hsnCode;
            private String description;
            private String uqc; // Unit of Quantity Code
            private Integer totalQuantity;
            private BigDecimal totalValue;
            
            // Tax Rates and Amounts
            private BigDecimal taxableValue;
            private BigDecimal cgstRate;
            private BigDecimal cgstAmount;
            private BigDecimal sgstRate;
            private BigDecimal sgstAmount;
            private BigDecimal igstRate;
            private BigDecimal igstAmount;
            private BigDecimal totalTax;
        }
    }
    
    /**
     * Calculate total tax liability
     */
    public void calculateTaxLiability() {
        this.taxLiability = BigDecimal.ZERO
            .add(totalCGST != null ? totalCGST : BigDecimal.ZERO)
            .add(totalSGST != null ? totalSGST : BigDecimal.ZERO)
            .add(totalIGST != null ? totalIGST : BigDecimal.ZERO);
        
        // Net tax payable = Tax Liability - Input Tax Credit
        this.netTaxPayable = taxLiability.subtract(
            inputTaxCredit != null ? inputTaxCredit : BigDecimal.ZERO
        );
    }
    
    /**
     * Get formatted GST period
     */
    public String getFormattedGstPeriod() {
        if (month != null && year != null) {
            return String.format("%02d-%d", month, year);
        }
        return gstPeriod;
    }
}

// Made with Bob
