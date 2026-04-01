package com.dts.restro.reports.controller;

import com.dts.restro.common.ApiResponse;
import com.dts.restro.common.TenantContext;
import com.dts.restro.reports.services.SalesAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Sales analytics and insights APIs")
public class AnalyticsController {
    
    private final SalesAnalyticsService analyticsService;
    
    /**
     * Analyze sales trends
     */
    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Analyze sales trends", description = "Get sales trends analysis with moving averages and growth rates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeSalesTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Analyzing sales trends from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> trends = analyticsService.analyzeSalesTrends(restaurantId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(trends, "Sales trends analyzed successfully"));
    }
    
    /**
     * Analyze hourly sales pattern
     */
    @GetMapping("/hourly")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Analyze hourly sales", description = "Get hourly sales breakdown and identify peak hours")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeHourlySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Analyzing hourly sales for {}", date);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> hourlySales = analyticsService.analyzeHourlySales(restaurantId, date);
        
        return ResponseEntity.ok(ApiResponse.success(hourlySales, "Hourly sales analyzed successfully"));
    }
    
    /**
     * Calculate performance metrics
     */
    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Calculate performance metrics", description = "Get comprehensive performance metrics including KPIs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculatePerformanceMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Calculating performance metrics from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> metrics = analyticsService.calculatePerformanceMetrics(restaurantId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(metrics, "Performance metrics calculated successfully"));
    }
    
    /**
     * Compare performance between two periods
     */
    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Compare performance", description = "Compare performance metrics between two time periods")
    public ResponseEntity<ApiResponse<Map<String, Object>>> comparePerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start2,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end2) {
        
        log.info("Comparing performance: Period 1 ({} to {}) vs Period 2 ({} to {})", 
            start1, end1, start2, end2);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> comparison = analyticsService.comparePerformance(
            restaurantId, start1, end1, start2, end2);
        
        return ResponseEntity.ok(ApiResponse.success(comparison, "Performance comparison completed successfully"));
    }
    
    /**
     * Forecast sales
     */
    @GetMapping("/forecast")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Forecast sales", description = "Predict future sales using historical data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forecastSales(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("Forecasting sales for next {} days", days);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> forecast = analyticsService.forecastSales(restaurantId, days);
        
        return ResponseEntity.ok(ApiResponse.success(forecast, "Sales forecast generated successfully"));
    }
    
    /**
     * Analyze customer retention
     */
    @GetMapping("/customers/retention")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Analyze customer retention", description = "Get customer retention metrics and segments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeCustomerRetention(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Analyzing customer retention from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> retention = analyticsService.analyzeCustomerRetention(
            restaurantId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(retention, "Customer retention analyzed successfully"));
    }
    
    /**
     * Get customer segments
     */
    @GetMapping("/customers/segments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get customer segments", description = "Segment customers by behavior and spending")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerSegments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting customer segments from {} to {}", startDate, endDate);
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Map<String, Object> retention = analyticsService.analyzeCustomerRetention(
            restaurantId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(retention, "Customer segments retrieved successfully"));
    }
}

// Made with Bob
