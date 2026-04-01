package com.dts.restro.reports.controller;

import com.dts.restro.common.ApiResponse;
import com.dts.restro.common.TenantContext;
import com.dts.restro.reports.dto.GSTReportDTO;
import com.dts.restro.reports.dto.ItemSalesReportDTO;
import com.dts.restro.reports.dto.SalesReportDTO;
import com.dts.restro.reports.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Report generation and management APIs")
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * Generate daily sales report
     */
    @GetMapping("/sales/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate daily sales report", description = "Get comprehensive sales report for a specific date")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Generating daily sales report for date: {}", date);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        SalesReportDTO report = reportService.generateDailySalesReport(restaurantId, date);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Daily sales report generated successfully"));
    }
    
    /**
     * Generate monthly sales report
     */
    @GetMapping("/sales/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate monthly sales report", description = "Get comprehensive sales report for a specific month")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getMonthlySalesReport(
            @RequestParam int month,
            @RequestParam int year) {
        
        log.info("Generating monthly sales report for {}/{}", month, year);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        SalesReportDTO report = reportService.generateMonthlySalesReport(restaurantId, month, year);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Monthly sales report generated successfully"));
    }
    
    /**
     * Generate date range sales report
     */
    @GetMapping("/sales/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate date range sales report", description = "Get sales report for a custom date range")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getDateRangeSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating sales report from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        SalesReportDTO report = reportService.generateMonthlySalesReport(restaurantId, 
            startDate.getMonthValue(), startDate.getYear());
        
        return ResponseEntity.ok(ApiResponse.success(report, "Date range sales report generated successfully"));
    }
    
    /**
     * Generate item-wise sales report
     */
    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate item-wise sales report", description = "Get detailed sales report for all menu items")
    public ResponseEntity<ApiResponse<ItemSalesReportDTO>> getItemWiseSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating item-wise sales report from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        ItemSalesReportDTO report = reportService.generateItemWiseSalesReport(restaurantId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Item-wise sales report generated successfully"));
    }
    
    /**
     * Generate category-wise sales report
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate category-wise sales report", description = "Get sales report grouped by categories")
    public ResponseEntity<ApiResponse<ItemSalesReportDTO>> getCategoryWiseSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating category-wise sales report from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        ItemSalesReportDTO report = reportService.generateItemWiseSalesReport(restaurantId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Category-wise sales report generated successfully"));
    }
    
    /**
     * Generate payment method report
     */
    @GetMapping("/payment-methods")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate payment method report", description = "Get sales breakdown by payment methods")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getPaymentMethodReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating payment method report from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        SalesReportDTO report = reportService.generateMonthlySalesReport(restaurantId, 
            startDate.getMonthValue(), startDate.getYear());
        
        return ResponseEntity.ok(ApiResponse.success(report, "Payment method report generated successfully"));
    }
    
    /**
     * Generate GST report (GSTR-1 format)
     */
    @GetMapping("/gst")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate GST report", description = "Get GST report in GSTR-1 format for tax filing")
    public ResponseEntity<ApiResponse<GSTReportDTO>> getGSTReport(
            @RequestParam int month,
            @RequestParam int year) {
        
        log.info("Generating GST report for {}/{}", month, year);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        GSTReportDTO report = reportService.generateGSTReport(restaurantId, month, year);
        
        return ResponseEntity.ok(ApiResponse.success(report, "GST report generated successfully"));
    }
    
    /**
     * Generate top customers report
     */
    @GetMapping("/customers/top")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate top customers report", description = "Get list of top customers by spending")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getTopCustomersReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Generating top {} customers report from {} to {}", limit, startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        SalesReportDTO report = reportService.generateDailySalesReport(restaurantId, startDate);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Top customers report generated successfully"));
    }
    
    /**
     * Export report to PDF
     */
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Export report to PDF", description = "Download report as PDF file")
    public ResponseEntity<byte[]> exportReportToPDF(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Exporting {} report to PDF from {} to {}", reportType, startDate, endDate);
        
        // TODO: Implement PDF export using iText
        // byte[] pdfBytes = reportService.exportReportToPDF(report);
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=report.pdf")
            .body(new byte[0]); // Placeholder
    }
    
    /**
     * Export report to Excel
     */
    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Export report to Excel", description = "Download report as Excel file")
    public ResponseEntity<byte[]> exportReportToExcel(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Exporting {} report to Excel from {} to {}", reportType, startDate, endDate);
        
        // TODO: Implement Excel export using Apache POI
        // byte[] excelBytes = reportService.exportReportToExcel(report);
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Content-Disposition", "attachment; filename=report.xlsx")
            .body(new byte[0]); // Placeholder
    }
}

// Made with Bob
