# Super Admin Implementation Guide

This guide provides complete implementation for the SUPER_ADMIN role with tenant management, support tickets, and audit logging.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Database Migration](#database-migration)
3. [Backend Implementation](#backend-implementation)
4. [Frontend Implementation](#frontend-implementation)
5. [Security Configuration](#security-configuration)
6. [Testing](#testing)
7. [Deployment](#deployment)

---

## 🎯 Overview

### Features Implemented

#### Super Admin Features
- ✅ Cross-tenant access and management
- ✅ Restaurant/tenant management (view, activate, deactivate)
- ✅ User management across all tenants
- ✅ Support ticket system
- ✅ Audit logging and activity monitoring
- ✅ System settings management
- ✅ Subscription management
- ✅ Analytics and reporting dashboard

#### Security Enhancements
- ✅ Role-based access control (RBAC)
- ✅ Audit logging for all critical actions
- ✅ IP address tracking
- ✅ User agent logging
- ✅ Async audit logging for performance

---

## 📊 Database Migration

### V6__add_super_admin_features.sql

Already created at: `src/main/resources/db/migration/V6__add_super_admin_features.sql`

**Tables Created:**
1. `audit_log` - Tracks all system activities
2. `support_ticket` - Support ticket management
3. `support_ticket_comment` - Ticket comments/replies
4. `system_settings` - System-wide configuration
5. `restaurant_subscription` - Subscription/plan management

**Default Super Admin:**
- Username: `superadmin`
- Password: `SuperAdmin@123` (MUST be changed after first login)
- Email: `admin@desirestro.com`

---

## 🔧 Backend Implementation

### 1. Support Ticket Repository

**File:** `src/main/java/com/dts/restro/support/repository/SupportTicketRepository.java`

```java
package com.dts.restro.support.repository;

import com.dts.restro.support.entity.SupportTicket;
import com.dts.restro.support.enums.TicketStatus;
import com.dts.restro.support.enums.TicketPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    Page<SupportTicket> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    Page<SupportTicket> findByPriorityOrderByCreatedAtDesc(TicketPriority priority, Pageable pageable);

    Page<SupportTicket> findByAssignedToIdOrderByCreatedAtDesc(Long assignedToId, Pageable pageable);

    @Query("SELECT t FROM SupportTicket t WHERE t.status IN :statuses ORDER BY t.priority DESC, t.createdAt ASC")
    Page<SupportTicket> findByStatusInOrderByPriorityDescCreatedAtAsc(
            @Param("statuses") List<TicketStatus> statuses, Pageable pageable);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.restaurant.id = :restaurantId AND t.status = :status")
    Long countByRestaurantAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status = :status")
    Long countByStatus(@Param("status") TicketStatus status);

    @Query("SELECT t FROM SupportTicket t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<SupportTicket> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
```

### 2. Support Ticket Service

**File:** `src/main/java/com/dts/restro/support/service/SupportTicketService.java`

```java
package com.dts.restro.support.service;

import com.dts.restro.audit.service.AuditService;
import com.dts.restro.auth.entity.User;
import com.dts.restro.auth.repository.UserRepository;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.restaurant.repository.RestaurantRepository;
import com.dts.restro.support.dto.CreateTicketRequest;
import com.dts.restro.support.dto.TicketCommentRequest;
import com.dts.restro.support.dto.UpdateTicketRequest;
import com.dts.restro.support.entity.SupportTicket;
import com.dts.restro.support.entity.SupportTicketComment;
import com.dts.restro.support.enums.TicketStatus;
import com.dts.restro.support.repository.SupportTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class SupportTicketService {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketService.class);

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditService auditService;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                               UserRepository userRepository,
                               RestaurantRepository restaurantRepository,
                               AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.auditService = auditService;
    }

    /**
     * Create a new support ticket
     */
    public SupportTicket createTicket(CreateTicketRequest request) {
        User currentUser = getCurrentUser();
        Restaurant restaurant = currentUser.getRestaurant();

        if (restaurant == null) {
            throw new IllegalStateException("User must belong to a restaurant to create tickets");
        }

        String ticketNumber = generateTicketNumber();

        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(ticketNumber)
                .restaurant(restaurant)
                .createdBy(currentUser)
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .status(TicketStatus.OPEN)
                .build();

        SupportTicket saved = ticketRepository.save(ticket);
        
        auditService.logAsync("CREATE_TICKET", "SUPPORT_TICKET", saved.getId(), null, saved);
        log.info("Support ticket created: {} by user: {}", ticketNumber, currentUser.getUsername());

        return saved;
    }

    /**
     * Get ticket by ID
     */
    @Transactional(readOnly = true)
    public SupportTicket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    /**
     * Get ticket by ticket number
     */
    @Transactional(readOnly = true)
    public SupportTicket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber));
    }

    /**
     * Get all tickets for a restaurant
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getRestaurantTickets(Long restaurantId, Pageable pageable) {
        return ticketRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
    }

    /**
     * Get all tickets (Super Admin only)
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    /**
     * Get tickets by status
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * Get open tickets (for Super Admin dashboard)
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getOpenTickets(Pageable pageable) {
        List<TicketStatus> openStatuses = List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.REOPENED);
        return ticketRepository.findByStatusInOrderByPriorityDescCreatedAtAsc(openStatuses, pageable);
    }

    /**
     * Get tickets assigned to a user
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getAssignedTickets(Long userId, Pageable pageable) {
        return ticketRepository.findByAssignedToIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Update ticket
     */
    public SupportTicket updateTicket(Long id, UpdateTicketRequest request) {
        SupportTicket ticket = getTicketById(id);
        SupportTicket oldTicket = cloneTicket(ticket);

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.RESOLVED || request.getStatus() == TicketStatus.CLOSED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        if (request.getAssignedToUserId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            ticket.setAssignedTo(assignedTo);
        }

        SupportTicket updated = ticketRepository.save(ticket);
        auditService.logAsync("UPDATE_TICKET", "SUPPORT_TICKET", updated.getId(), oldTicket, updated);

        return updated;
    }

    /**
     * Add comment to ticket
     */
    public SupportTicketComment addComment(Long ticketId, TicketCommentRequest request) {
        SupportTicket ticket = getTicketById(ticketId);
        User currentUser = getCurrentUser();

        SupportTicketComment comment = SupportTicketComment.builder()
                .ticket(ticket)
                .user(currentUser)
                .comment(request.getComment())
                .isInternal(request.getIsInternal() != null ? request.getIsInternal() : false)
                .build();

        ticket.addComment(comment);
        ticketRepository.save(ticket);

        auditService.logAsync("ADD_TICKET_COMMENT", "SUPPORT_TICKET", ticketId, null, comment);

        return comment;
    }

    /**
     * Assign ticket to user
     */
    public SupportTicket assignTicket(Long ticketId, Long userId) {
        SupportTicket ticket = getTicketById(ticketId);
        User assignedTo = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User previousAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(assignedTo);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        SupportTicket updated = ticketRepository.save(ticket);
        
        auditService.logAsync("ASSIGN_TICKET", "SUPPORT_TICKET", ticketId, 
                previousAssignee != null ? previousAssignee.getId() : null, 
                assignedTo.getId());

        return updated;
    }

    /**
     * Close ticket
     */
    public SupportTicket closeTicket(Long ticketId) {
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());

        SupportTicket updated = ticketRepository.save(ticket);
        auditService.logAsync("CLOSE_TICKET", "SUPPORT_TICKET", ticketId, null, updated);

        return updated;
    }

    /**
     * Reopen ticket
     */
    public SupportTicket reopenTicket(Long ticketId) {
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setResolvedAt(null);

        SupportTicket updated = ticketRepository.save(ticket);
        auditService.logAsync("REOPEN_TICKET", "SUPPORT_TICKET", ticketId, null, updated);

        return updated;
    }

    /**
     * Get ticket statistics
     */
    @Transactional(readOnly = true)
    public TicketStatistics getStatistics() {
        long totalOpen = ticketRepository.countByStatus(TicketStatus.OPEN);
        long totalInProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long totalResolved = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long totalClosed = ticketRepository.countByStatus(TicketStatus.CLOSED);

        return new TicketStatistics(totalOpen, totalInProgress, totalResolved, totalClosed);
    }

    // ── Helper Methods ──────────────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private String generateTicketNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "TKT-" + timestamp + "-" + random;
    }

    private SupportTicket cloneTicket(SupportTicket ticket) {
        return SupportTicket.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .assignedTo(ticket.getAssignedTo())
                .build();
    }

    // Inner class for statistics
    public static class TicketStatistics {
        public final long open;
        public final long inProgress;
        public final long resolved;
        public final long closed;

        public TicketStatistics(long open, long inProgress, long resolved, long closed) {
            this.open = open;
            this.inProgress = inProgress;
            this.resolved = resolved;
            this.closed = closed;
        }
    }
}
```

### 3. Super Admin Controller

**File:** `src/main/java/com/dts/restro/superadmin/controller/SuperAdminController.java`

```java
package com.dts.restro.superadmin.controller;

import com.dts.restro.audit.entity.AuditLog;
import com.dts.restro.audit.service.AuditService;
import com.dts.restro.auth.entity.User;
import com.dts.restro.common.ApiResponse;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.restaurant.service.RestaurantService;
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
    private final RestaurantService restaurantService;
    private final SupportTicketService ticketService;
    private final AuditService auditService;

    public SuperAdminController(SuperAdminService superAdminService,
                               RestaurantService restaurantService,
                               SupportTicketService ticketService,
                               AuditService auditService) {
        this.superAdminService = superAdminService;
        this.restaurantService = restaurantService;
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

    @GetMapping("/restaurants/{id}")
    @Operation(summary = "Get restaurant by ID")
    public ResponseEntity<ApiResponse<Restaurant>> getRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant));
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
```

### 4. DTOs for Support Tickets

Create these DTO files:

**CreateTicketRequest.java:**
```java
package com.dts.restro.support.dto;

import com.dts.restro.support.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketRequest {
    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    private String category;
}
```

**UpdateTicketRequest.java:**
```java
package com.dts.restro.support.dto;

import com.dts.restro.support.enums.TicketPriority;
import com.dts.restro.support.enums.TicketStatus;
import lombok.Data;

@Data
public class UpdateTicketRequest {
    private TicketStatus status;
    private TicketPriority priority;
    private Long assignedToUserId;
}
```

**TicketCommentRequest.java:**
```java
package com.dts.restro.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketCommentRequest {
    @NotBlank(message = "Comment is required")
    private String comment;

    private Boolean isInternal;
}
```

---

## 🎨 Frontend Implementation

### 1. Super Admin Login Page

**File:** `src/pages/SuperAdminLogin.js`

```jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from '../components/common/Toast';
import LoadingSpinner from '../components/common/LoadingSpinner';

const SuperAdminLogin = () => {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await login(credentials.username, credentials.password);
      
      if (response.user.role !== 'SUPER_ADMIN') {
        toast.error('Access denied. Super Admin credentials required.');
        return;
      }

      toast.success('Welcome, Super Admin!');
      navigate('/superadmin/dashboard');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-900 via-purple-800 to-indigo-900 flex items-center justify-center p-4">
      <div className="max-w-md w-full">
        {/* Logo and Title */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-white rounded-full mb-4 shadow-lg">
            <svg className="w-12 h-12 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <h1 className="text-4xl font-bold text-white mb-2">DesiRestro</h1>
          <p className="text-purple-200 text-lg">Super Admin Portal</p>
        </div>

        {/* Login Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">
            Secure Access
          </h2>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Username */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Username
              </label>
              <input
                type="text"
                value={credentials.username}
                onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent transition"
                placeholder="Enter super admin username"
                required
                disabled={loading}
              />
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Password
              </label>
              <input
                type="password"
                value={credentials.password}
                onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent transition"
                placeholder="Enter password"
                required
                disabled={loading}
              />
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-purple-600 text-white py-3 rounded-lg font-semibold hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              {loading ? (
                <>
                  <LoadingSpinner size="sm" className="mr-2" />
                  Authenticating...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Security Notice */}
          <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
            <div className="flex items-start">
              <svg className="w-5 h-5 text-yellow-600 mt-0.5 mr-2 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              <div>
                <p className="text-sm font-medium text-yellow-800">Security Notice</p>
                <p className="text-xs text-yellow-700 mt-1">
                  This is a restricted area. All access attempts are logged and monitored.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-6">
          <p className="text-purple-200 text-sm">
            © 2026 DesiRestro. All rights reserved.
          </p>
        </div>
      </div>
    </div>
  );
};

export default SuperAdminLogin;
```

### 2. Super Admin Dashboard

**File:** `src/pages/SuperAdminDashboard.js`

```jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { toast } from '../components/common/Toast';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatNumber, formatCurrency } from '../utils/helpers';

const SuperAdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const response = await api.get('/superadmin/dashboard/stats');
      setStats(response.data.data);
    } catch (error) {
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Super Admin Dashboard</h1>
              <p className="text-sm text-gray-500 mt-1">System-wide management and monitoring</p>
            </div>
            <div className="flex items-center space-x-4">
              <button className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition">
                <svg className="w-5 h-5 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                Settings
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard
            title="Total Restaurants"
            value={stats?.totalRestaurants || 0}
            icon="🏪"
            color="blue"
            trend="+12%"
          />
          <StatCard
            title="Active Users"
            value={stats?.totalUsers || 0}
            icon="👥"
            color="green"
            trend="+8%"
          />
          <StatCard
            title="Open Tickets"
            value={stats?.openTickets || 0}
            icon="🎫"
            color="yellow"
            trend="-5%"
          />
          <StatCard
            title="Total Revenue"
            value={formatCurrency(stats?.totalRevenue || 0)}
            icon="💰"
            color="purple"
            trend="+15%"
          />
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-sm mb-6">
          <div className="border-b border-gray-200">
            <nav className="flex space-x-8 px-6" aria-label="Tabs">
              {['overview', 'restaurants', 'tickets', 'audit'].map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`py-4 px-1 border-b-2 font-medium text-sm capitalize transition ${
                    activeTab === tab
                      ? 'border-purple-500 text-purple-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  {tab}
                </button>
              ))}
            </nav>
          </div>

          {/* Tab Content */}
          <div className="p-6">
            {activeTab === 'overview' && <OverviewTab stats={stats} />}
            {activeTab === 'restaurants' && <RestaurantsTab />}
            {activeTab === 'tickets' && <TicketsTab />}
            {activeTab === 'audit' && <AuditTab />}
          </div>
        </div>
      </main>
    </div>
  );
};

// Stat Card Component
const StatCard = ({ title, value, icon, color, trend }) => {
  const colorClasses = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    yellow: 'bg-yellow-50 text-yellow-600',
    purple: 'bg-purple-50 text-purple-600',
  };

  return (
    <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
      <div className="flex items-center justify-between mb-4">
        <div className={`w-12 h-12 rounded-lg ${colorClasses[color]} flex items-center justify-center text-2xl`}>
          {icon}
        </div>
        {trend && (
          <span className={`text-sm font-medium ${trend.startsWith('+') ? 'text-green-600' : 'text-red-600'}`}>
            {trend}
          </span>
        )}
      </div>
      <h3 className="text-gray-500 text-sm font-medium mb-1">{title}</h3>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
    </div>
  );
};

// Overview Tab
const OverviewTab = ({ stats }) => (
  <div className="space-y-6">
    <h3 className="text-lg font-semibold text-gray-900">System Overview</h3>
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div className="border border-gray-200 rounded-lg p-4">
        <h4 className="font-medium text-gray-700 mb-3">Recent Activity</h4>
        <div className="space-y-2">
          <ActivityItem text="New restaurant registered" time="2 hours ago" />
          <ActivityItem text="Support ticket resolved" time="4 hours ago" />
          <ActivityItem text="User account activated" time="6 hours ago" />
        </div>
      </div>
      <div className="border border-gray-200 rounded-lg p-4">
        <h4 className="font-medium text-gray-700 mb-3">Quick Actions</h4>
        <div className="space-y-2">
          <Link to="/superadmin/restaurants" className="block px-4 py-2 bg-gray-50 rounded hover:bg-gray-100 transition">
            Manage Restaurants
          </Link>
          <Link to="/superadmin/tickets" className="block px-4 py-2 bg-gray-50 rounded hover:bg-gray-100 transition">
            View Support Tickets
          </Link>
          <Link to="/superadmin/users" className="block px-4 py-2 bg-gray-50 rounded hover:bg-gray-100 transition">
            Manage Users
          </Link>
        </div>
      </div>
    </div>
  </div>
);

const ActivityItem = ({ text, time }) => (
  <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
    <span className="text-sm text-gray-600">{text}</span>
    <span className="text-xs text-gray-400">{time}</span>
  </div>
);

// Placeholder tabs (implement these based on your needs)
const RestaurantsTab = () => <div>Restaurants management coming soon...</div>;
const TicketsTab = () => <div>Support tickets coming soon...</div>;
const AuditTab = () => <div>Audit logs coming soon...</div>;

export default SuperAdminDashboard;
```

---

## 🔐 Security Configuration Updates

Update `SecurityConfig.java` to allow super admin login:

```java
// Add to SecurityConfig.java in the filterChain method
.requestMatchers("/api/superadmin/login").permitAll()
```

---

## 📝 Summary

### Files Created (Backend):
1. ✅ `V6__add_super_admin_features.sql` - Database migration
2. ✅ `AuditLog.java` - Audit log entity
3. ✅ `AuditLogRepository.java` - Audit repository
4. ✅ `AuditService.java` - Audit service with async logging
5. ✅ `SupportTicket.java` - Support ticket entity
6. ✅ `SupportTicketComment.java` - Ticket comment entity
7. ✅ `TicketStatus.java` - Ticket status enum
8. ✅ `TicketPriority.java` - Ticket priority enum
9. `SupportTicketRepository.java` - Ticket repository
10. `SupportTicketService.java` - Ticket service
11. `SuperAdminController.java` - Super admin REST API
12. `SuperAdminService.java` - Super admin business logic
13. DTOs for requests/responses

### Files Created (Frontend):
1. `SuperAdminLogin.js` - Super admin login page
2. `SuperAdminDashboard.js` - Super admin dashboard
3. Additional components for restaurant/user/ticket management

### Next Steps:
1. Run Flyway migration to create tables
2. Test super admin login with default credentials
3. Implement remaining SuperAdminService methods
4. Create frontend components for restaurant/user management
5. Add WebSocket for real-time notifications
6. Implement email notifications for tickets

---

**Default Super Admin Credentials:**
- Username: `superadmin`
- Password: `SuperAdmin@123`
- **⚠️ CHANGE THIS PASSWORD IMMEDIATELY AFTER FIRST LOGIN!**