package com.dts.restro.billing.controller;

import com.dts.restro.billing.dto.DailySalesSummaryDTO;
import com.dts.restro.billing.service.DailySalesSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Daily Sales Summary operations
 * Provides endpoints for viewing and managing daily sales summaries
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/daily-summary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Daily Sales Summary", description = "APIs for managing daily sales summaries")
public class DailySalesSummaryController {
    
    private final DailySalesSummaryService summaryService;
    
    /**
     * Get daily sales summary for a specific date
     * 
     * @param restaurantId Restaurant ID
     * @param date Date in YYYY-MM-DD format
     * @return Daily sales summary
     */
    @GetMapping("/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get daily sales summary", 
               description = "Retrieve sales summary for a specific date")
    public ResponseEntity<DailySalesSummaryDTO> getDailySummary(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Date (YYYY-MM-DD)", required = true)
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/{}", restaurantId, date);
        
        DailySalesSummaryDTO summary = summaryService.getSummaryByDate(restaurantId, date);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get monthly sales summaries
     * 
     * @param restaurantId Restaurant ID
     * @param month Month (1-12)
     * @param year Year (e.g., 2024)
     * @return List of daily summaries for the month
     */
    @GetMapping("/month/{month}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get monthly sales summaries", 
               description = "Retrieve all daily summaries for a specific month")
    public ResponseEntity<List<DailySalesSummaryDTO>> getMonthlySummaries(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Month (1-12)", required = true)
        @PathVariable int month,
        
        @Parameter(description = "Year (e.g., 2024)", required = true)
        @PathVariable int year
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/month/{}/year/{}", 
            restaurantId, month, year);
        
        List<DailySalesSummaryDTO> summaries = summaryService.getMonthlySummaries(
            restaurantId, month, year
        );
        return ResponseEntity.ok(summaries);
    }
    
    /**
     * Get sales summaries for a date range
     * 
     * @param restaurantId Restaurant ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of daily summaries for the date range
     */
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get sales summaries by date range", 
               description = "Retrieve daily summaries for a specific date range")
    public ResponseEntity<List<DailySalesSummaryDTO>> getSummariesByDateRange(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        
        @Parameter(description = "End date (YYYY-MM-DD)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/range?startDate={}&endDate={}", 
            restaurantId, startDate, endDate);
        
        List<DailySalesSummaryDTO> summaries = summaryService.getSummariesByDateRange(
            restaurantId, startDate, endDate
        );
        return ResponseEntity.ok(summaries);
    }
    
    /**
     * Generate daily sales summary for a specific date
     * This endpoint manually triggers summary generation
     * 
     * @param restaurantId Restaurant ID
     * @param date Date in YYYY-MM-DD format
     * @return Generated daily sales summary
     */
    @PostMapping("/generate/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate daily sales summary", 
               description = "Manually generate or update sales summary for a specific date")
    public ResponseEntity<DailySalesSummaryDTO> generateDailySummary(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Date (YYYY-MM-DD)", required = true)
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("POST /api/restaurants/{}/daily-summary/generate/{}", restaurantId, date);
        
        DailySalesSummaryDTO summary = summaryService.generateDailySummary(restaurantId, date);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }
    
    /**
     * Regenerate daily sales summary for a specific date
     * This deletes the existing summary and creates a fresh one
     * Useful for fixing data inconsistencies
     * 
     * @param restaurantId Restaurant ID
     * @param date Date in YYYY-MM-DD format
     * @return Regenerated daily sales summary
     */
    @PostMapping("/regenerate/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Regenerate daily sales summary", 
               description = "Delete and regenerate sales summary for a specific date (Admin only)")
    public ResponseEntity<DailySalesSummaryDTO> regenerateDailySummary(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Date (YYYY-MM-DD)", required = true)
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("POST /api/restaurants/{}/daily-summary/regenerate/{}", restaurantId, date);
        
        DailySalesSummaryDTO summary = summaryService.regenerateSummary(restaurantId, date);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get today's sales summary
     * Convenience endpoint for current day summary
     * 
     * @param restaurantId Restaurant ID
     * @return Today's sales summary
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get today's sales summary", 
               description = "Retrieve sales summary for the current date")
    public ResponseEntity<DailySalesSummaryDTO> getTodaysSummary(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/today", restaurantId);
        
        LocalDate today = LocalDate.now();
        DailySalesSummaryDTO summary = summaryService.getSummaryByDate(restaurantId, today);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get yesterday's sales summary
     * Convenience endpoint for previous day summary
     * 
     * @param restaurantId Restaurant ID
     * @return Yesterday's sales summary
     */
    @GetMapping("/yesterday")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get yesterday's sales summary", 
               description = "Retrieve sales summary for the previous date")
    public ResponseEntity<DailySalesSummaryDTO> getYesterdaysSummary(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/yesterday", restaurantId);
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DailySalesSummaryDTO summary = summaryService.getSummaryByDate(restaurantId, yesterday);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get current month's summaries
     * Convenience endpoint for current month
     * 
     * @param restaurantId Restaurant ID
     * @return List of daily summaries for current month
     */
    @GetMapping("/current-month")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get current month's summaries", 
               description = "Retrieve all daily summaries for the current month")
    public ResponseEntity<List<DailySalesSummaryDTO>> getCurrentMonthSummaries(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/current-month", restaurantId);
        
        LocalDate now = LocalDate.now();
        List<DailySalesSummaryDTO> summaries = summaryService.getMonthlySummaries(
            restaurantId, now.getMonthValue(), now.getYear()
        );
        return ResponseEntity.ok(summaries);
    }
    
    /**
     * Get last 7 days summaries
     * Convenience endpoint for weekly view
     * 
     * @param restaurantId Restaurant ID
     * @return List of daily summaries for last 7 days
     */
    @GetMapping("/last-7-days")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get last 7 days summaries", 
               description = "Retrieve daily summaries for the last 7 days")
    public ResponseEntity<List<DailySalesSummaryDTO>> getLast7DaysSummaries(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/last-7-days", restaurantId);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        
        List<DailySalesSummaryDTO> summaries = summaryService.getSummariesByDateRange(
            restaurantId, startDate, endDate
        );
        return ResponseEntity.ok(summaries);
    }
    
    /**
     * Get last 30 days summaries
     * Convenience endpoint for monthly view
     * 
     * @param restaurantId Restaurant ID
     * @return List of daily summaries for last 30 days
     */
    @GetMapping("/last-30-days")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get last 30 days summaries", 
               description = "Retrieve daily summaries for the last 30 days")
    public ResponseEntity<List<DailySalesSummaryDTO>> getLast30DaysSummaries(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId
    ) {
        log.info("GET /api/restaurants/{}/daily-summary/last-30-days", restaurantId);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        
        List<DailySalesSummaryDTO> summaries = summaryService.getSummariesByDateRange(
            restaurantId, startDate, endDate
        );
        return ResponseEntity.ok(summaries);
    }
}

// Made with Bob
