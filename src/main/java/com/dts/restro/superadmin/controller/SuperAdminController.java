package com.dts.restro.superadmin.controller;

import com.dts.restro.audit.entity.AuditLog;
import com.dts.restro.audit.service.AuditService;
import com.dts.restro.auth.entity.User;
import com.dts.restro.common.ApiResponse;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.superadmin.dto.RestaurantStatsDTO;
import com.dts.restro.superadmin.dto.SystemStatsDTO;
import com.dts.restro.superadmin.service.SuperAdminService;
import com.dts.restro.support.entity.SupportTicket;
import com.dts.restro.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@Tag(name = "Super Admin", description = "Super Admin management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final SupportTicketService ticketService;
    private final AuditService auditService;

    public SuperAdminController(SuperAdminService superAdminService,
                               SupportTicketService ticketService,
                               AuditService auditService) {
        this.superAdminService = superAdminService;
        this.ticketService = ticketService;
        this.auditService = auditService;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get system-wide statistics")
    public ResponseEntity<ApiResponse<SystemStatsDTO>> getSystemStats() {
        SystemStatsDTO stats = superAdminService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ── Restaurant Management ────────────────────────────────────────────────

    @GetMapping("/restaurants")
    @Operation(summary = "Get all restaurants")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> getAllRestaurants(Pageable pageable) {
        Page<Restaurant> restaurants = superAdminService.getAllRestaurants(pageable);
        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    @GetMapping("/restaurants/{id}/stats")
    @Operation(summary = "Get restaurant statistics")
    public ResponseEntity<ApiResponse<RestaurantStatsDTO>> getRestaurantStats(@PathVariable Long id) {
        RestaurantStatsDTO stats = superAdminService.getRestaurantStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PutMapping("/restaurants/{id}/activate")
    @Operation(summary = "Activate a restaurant")
    public ResponseEntity<ApiResponse<Restaurant>> activateRestaurant(@PathVariable Long id) {
        Restaurant restaurant = superAdminService.activateRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant activated successfully"));
    }

    @PutMapping("/restaurants/{id}/deactivate")
    @Operation(summary = "Deactivate a restaurant")
    public ResponseEntity<ApiResponse<Restaurant>> deactivateRestaurant(@PathVariable Long id) {
        Restaurant restaurant = superAdminService.deactivateRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant deactivated successfully"));
    }

    // ── User Management ──────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "Get all users across all restaurants")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(Pageable pageable) {
        Page<User> users = superAdminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/restaurants/{restaurantId}/users")
    @Operation(summary = "Get users for a specific restaurant")
    public ResponseEntity<ApiResponse<List<User>>> getRestaurantUsers(@PathVariable Long restaurantId) {
        List<User> users = superAdminService.getRestaurantUsers(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{userId}/activate")
    @Operation(summary = "Activate a user")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable Long userId) {
        User user = superAdminService.activateUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User activated successfully"));
    }

    @PutMapping("/users/{userId}/deactivate")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<ApiResponse<User>> deactivateUser(@PathVariable Long userId) {
        User user = superAdminService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User deactivated successfully"));
    }

    // ── Support Tickets ──────────────────────────────────────────────────────

    @GetMapping("/tickets")
    @Operation(summary = "Get all support tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicket>>> getAllTickets(Pageable pageable) {
        Page<SupportTicket> tickets = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/tickets/open")
    @Operation(summary = "Get open support tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicket>>> getOpenTickets(Pageable pageable) {
        Page<SupportTicket> tickets = ticketService.getOpenTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/tickets/stats")
    @Operation(summary = "Get ticket statistics")
    public ResponseEntity<ApiResponse<SupportTicketService.TicketStatistics>> getTicketStats() {
        SupportTicketService.TicketStatistics stats = ticketService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ── Audit Logs ───────────────────────────────────────────────────────────

    @GetMapping("/audit-logs")
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> logs = auditService.getAuditLogsByDateRange(
                LocalDateTime.now().minusDays(30), LocalDateTime.now(), pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/restaurants/{restaurantId}/audit-logs")
    @Operation(summary = "Get audit logs for a restaurant")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getRestaurantAuditLogs(
            @PathVariable Long restaurantId, Pageable pageable) {
        Page<AuditLog> logs = auditService.getRestaurantAuditLogs(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/users/{userId}/audit-logs")
    @Operation(summary = "Get audit logs for a user")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getUserAuditLogs(
            @PathVariable Long userId, Pageable pageable) {
        Page<AuditLog> logs = auditService.getUserAuditLogs(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}

// Made with Bob
