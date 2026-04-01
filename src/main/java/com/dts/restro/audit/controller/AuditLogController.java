package com.dts.restro.audit.controller;

import com.dts.restro.audit.entity.AuditLog;
import com.dts.restro.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Audit Log operations
 * Provides endpoints for viewing audit trails and activity logs
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "APIs for viewing audit trails and activity logs")
public class AuditLogController {
    
    private final AuditService auditService;
    
    /**
     * Get all audit logs for a restaurant
     * 
     * @param restaurantId Restaurant ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get restaurant audit logs", 
               description = "Retrieve all audit logs for a specific restaurant")
    public ResponseEntity<Page<AuditLog>> getRestaurantAuditLogs(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs", restaurantId);
        
        Page<AuditLog> auditLogs = auditService.getRestaurantAuditLogs(restaurantId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs by action type
     * 
     * @param restaurantId Restaurant ID
     * @param action Action type (e.g., CUSTOMER_CREATE, BILL_PAYMENT)
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get audit logs by action", 
               description = "Retrieve audit logs filtered by action type")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Action type", required = true)
        @PathVariable String action,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/action/{}", restaurantId, action);
        
        Page<AuditLog> auditLogs = auditService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs for a specific entity
     * 
     * @param restaurantId Restaurant ID
     * @param entityType Entity type (e.g., Customer, Bill)
     * @param entityId Entity ID
     * @return List of audit logs for the entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get entity audit history", 
               description = "Retrieve complete audit history for a specific entity")
    public ResponseEntity<List<AuditLog>> getEntityHistory(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Entity type (e.g., Customer, Bill)", required = true)
        @PathVariable String entityType,
        
        @Parameter(description = "Entity ID", required = true)
        @PathVariable Long entityId
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/entity/{}/{}", 
            restaurantId, entityType, entityId);
        
        List<AuditLog> auditLogs = auditService.getEntityHistory(entityType, entityId);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs by date range
     * 
     * @param restaurantId Restaurant ID
     * @param startDate Start date and time
     * @param endDate End date and time
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get audit logs by date range", 
               description = "Retrieve audit logs within a specific date range")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Start date (ISO format)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        
        @Parameter(description = "End date (ISO format)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/date-range?startDate={}&endDate={}", 
            restaurantId, startDate, endDate);
        
        Page<AuditLog> auditLogs = auditService.getRestaurantAuditLogsByDateRange(
            restaurantId, startDate, endDate, pageable
        );
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs for a specific user
     * 
     * @param restaurantId Restaurant ID
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user audit logs", 
               description = "Retrieve all audit logs for a specific user")
    public ResponseEntity<Page<AuditLog>> getUserAuditLogs(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "User ID", required = true)
        @PathVariable Long userId,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/user/{}", restaurantId, userId);
        
        Page<AuditLog> auditLogs = auditService.getUserAuditLogs(userId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get activity count for a restaurant
     * 
     * @param restaurantId Restaurant ID
     * @param since Date from which to count activities
     * @return Activity count
     */
    @GetMapping("/activity-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get activity count", 
               description = "Get count of activities since a specific date")
    public ResponseEntity<Long> getActivityCount(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        @Parameter(description = "Since date (ISO format)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/activity-count?since={}", 
            restaurantId, since);
        
        Long count = auditService.getRestaurantActivityCount(restaurantId, since);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get today's audit logs
     * Convenience endpoint for current day activities
     * 
     * @param restaurantId Restaurant ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get today's audit logs", 
               description = "Retrieve audit logs for the current date")
    public ResponseEntity<Page<AuditLog>> getTodaysAuditLogs(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/today", restaurantId);
        
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        Page<AuditLog> auditLogs = auditService.getRestaurantAuditLogsByDateRange(
            restaurantId, startOfDay, endOfDay, pageable
        );
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get last 24 hours audit logs
     * 
     * @param restaurantId Restaurant ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/last-24-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get last 24 hours audit logs", 
               description = "Retrieve audit logs from the last 24 hours")
    public ResponseEntity<Page<AuditLog>> getLast24HoursAuditLogs(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/last-24-hours", restaurantId);
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(24);
        
        Page<AuditLog> auditLogs = auditService.getRestaurantAuditLogsByDateRange(
            restaurantId, startDate, endDate, pageable
        );
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get customer-related audit logs
     * 
     * @param restaurantId Restaurant ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get customer audit logs", 
               description = "Retrieve all customer-related audit logs")
    public ResponseEntity<Page<AuditLog>> getCustomerAuditLogs(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/customers", restaurantId);
        
        // This would need a custom repository method to filter by entity type
        // For now, we'll return all logs and let the frontend filter
        Page<AuditLog> auditLogs = auditService.getRestaurantAuditLogs(restaurantId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get billing-related audit logs
     * 
     * @param restaurantId Restaurant ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @GetMapping("/bills")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get billing audit logs", 
               description = "Retrieve all billing-related audit logs")
    public ResponseEntity<Page<AuditLog>> getBillingAuditLogs(
        @Parameter(description = "Restaurant ID", required = true)
        @PathVariable Long restaurantId,
        
        Pageable pageable
    ) {
        log.info("GET /api/restaurants/{}/audit-logs/bills", restaurantId);
        
        // This would need a custom repository method to filter by entity type
        // For now, we'll return all logs and let the frontend filter
        Page<AuditLog> auditLogs = auditService.getRestaurantAuditLogs(restaurantId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}

// Made with Bob
