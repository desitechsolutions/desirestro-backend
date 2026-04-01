package com.dts.restro.audit.service;

import com.dts.restro.audit.entity.AuditLog;
import com.dts.restro.audit.repository.AuditLogRepository;
import com.dts.restro.auth.entity.User;
import com.dts.restro.auth.repository.UserRepository;
import com.dts.restro.restaurant.entity.Restaurant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository,
                       UserRepository userRepository,
                       ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Log an audit event asynchronously
     */
    @Async
    public void logAsync(String action, String entityType, Long entityId, Object oldValue, Object newValue) {
        try {
            log(action, entityType, entityId, oldValue, newValue);
        } catch (Exception e) {
            log.error("Failed to log audit event asynchronously", e);
        }
    }

    /**
     * Log an audit event synchronously
     */
    public void log(String action, String entityType, Long entityId, Object oldValue, Object newValue) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "system";

            User user = null;
            Restaurant restaurant = null;

            if (authentication != null && !"anonymousUser".equals(username)) {
                user = userRepository.findByUsername(username).orElse(null);
                if (user != null && user.getRestaurant() != null) {
                    restaurant = user.getRestaurant();
                }
            }

            HttpServletRequest request = getCurrentRequest();
            String ipAddress = request != null ? getClientIpAddress(request) : null;
            String userAgent = request != null ? request.getHeader("User-Agent") : null;

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .username(username)
                    .restaurant(restaurant)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(toJson(oldValue))
                    .newValue(toJson(newValue))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: action={} entityType={} entityId={}", action, entityType, entityId);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log a simple action without entity details
     */
    public void logAction(String action) {
        log(action, null, null, null, null);
    }

    /**
     * Get audit logs for a specific restaurant
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getRestaurantAuditLogs(Long restaurantId, Pageable pageable) {
        return auditLogRepository.findByRestaurantIdOrderByTimestampDesc(restaurantId, pageable);
    }

    /**
     * Get audit logs for a specific user
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogs(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Get audit logs by action type
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    /**
     * Get audit logs within a date range
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    /**
     * Get audit logs for a specific restaurant within a date range
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getRestaurantAuditLogsByDateRange(
            Long restaurantId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByRestaurantAndTimestampBetween(restaurantId, startDate, endDate, pageable);
    }

    /**
     * Get audit history for a specific entity
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getEntityHistory(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get activity count for a restaurant since a specific date
     */
    @Transactional(readOnly = true)
    public Long getRestaurantActivityCount(Long restaurantId, LocalDateTime since) {
        return auditLogRepository.countByRestaurantSince(restaurantId, since);
    }

    // ── Customer Operations Audit Methods ───────────────────────────────────

    /**
     * Log customer creation
     */
    public void logCustomerCreate(Long restaurantId, Long customerId, String customerName) {
        log("CUSTOMER_CREATE", "Customer", customerId, null,
            "Customer created: " + customerName + " (Restaurant: " + restaurantId + ")");
    }

    /**
     * Log customer update
     */
    public void logCustomerUpdate(Long restaurantId, Long customerId, Object oldValue, Object newValue) {
        log("CUSTOMER_UPDATE", "Customer", customerId, oldValue, newValue);
    }

    /**
     * Log customer deletion
     */
    public void logCustomerDelete(Long restaurantId, Long customerId, String customerName) {
        log("CUSTOMER_DELETE", "Customer", customerId,
            "Customer: " + customerName + " (Restaurant: " + restaurantId + ")", null);
    }

    /**
     * Log credit balance operation
     */
    public void logCreditOperation(Long restaurantId, Long customerId, String operation,
                                   Object amount, String reason) {
        log("CREDIT_" + operation, "Customer", customerId, null,
            "Amount: " + amount + ", Reason: " + reason + " (Restaurant: " + restaurantId + ")");
    }

    /**
     * Log loyalty points operation
     */
    public void logLoyaltyOperation(Long restaurantId, Long customerId, String operation,
                                    Integer points, String reason) {
        log("LOYALTY_" + operation, "Customer", customerId, null,
            "Points: " + points + ", Reason: " + reason + " (Restaurant: " + restaurantId + ")");
    }

    // ── Billing Operations Audit Methods ────────────────────────────────────

    /**
     * Log bill generation
     */
    public void logBillGenerate(Long restaurantId, Long billId, String billNumber, Object billData) {
        log("BILL_GENERATE", "Bill", billId, null,
            "Bill Number: " + billNumber + " (Restaurant: " + restaurantId + ")");
    }

    /**
     * Log bill payment
     */
    public void logBillPayment(Long restaurantId, Long billId, String billNumber,
                               String paymentMethod, Object amount) {
        log("BILL_PAYMENT", "Bill", billId, null,
            "Bill: " + billNumber + ", Method: " + paymentMethod +
            ", Amount: " + amount + " (Restaurant: " + restaurantId + ")");
    }

    /**
     * Log bill cancellation
     */
    public void logBillCancel(Long restaurantId, Long billId, String billNumber, String reason) {
        log("BILL_CANCEL", "Bill", billId,
            "Bill: " + billNumber + " (Restaurant: " + restaurantId + ")",
            "Reason: " + reason);
    }

    // ── Staff Operations Audit Methods ──────────────────────────────────────

    /**
     * Log staff creation
     */
    public void logStaffCreate(Long restaurantId, Long staffId, String staffName, String role) {
        log("STAFF_CREATE", "Staff", staffId, null,
            "Staff created: " + staffName + " (Role: " + role + ", Restaurant: " + restaurantId + ")");
    }

    /**
     * Log staff update
     */
    public void logStaffUpdate(Long restaurantId, Long staffId, String staffName, Object oldValue, Object newValue) {
        log("STAFF_UPDATE", "Staff", staffId, oldValue, newValue);
    }

    /**
     * Log staff deletion
     */
    public void logStaffDelete(Long restaurantId, Long staffId, String staffName) {
        log("STAFF_DELETE", "Staff", staffId,
            "Staff: " + staffName + " (Restaurant: " + restaurantId + ")", null);
    }

    /**
     * Log leave approval
     */
    public void logLeaveApprove(Long restaurantId, Long leaveId, String staffName, String period) {
        log("LEAVE_APPROVE", "Leave", leaveId, null,
            "Leave approved for: " + staffName + " (" + period + ", Restaurant: " + restaurantId + ")");
    }

    /**
     * Log leave rejection
     */
    public void logLeaveReject(Long restaurantId, Long leaveId, String staffName, String period) {
        log("LEAVE_REJECT", "Leave", leaveId, null,
            "Leave rejected for: " + staffName + " (" + period + ", Restaurant: " + restaurantId + ")");
    }

    /**
     * Log attendance clock-in
     */
    public void logAttendanceClockIn(Long restaurantId, Long attendanceId, String staffName) {
        log("ATTENDANCE_CLOCK_IN", "Attendance", attendanceId, null,
            "Clock-in: " + staffName + " (Restaurant: " + restaurantId + ")");
    }

    /**
     * Log attendance clock-out
     */
    public void logAttendanceClockOut(Long restaurantId, Long attendanceId, String staffName, Double hoursWorked) {
        log("ATTENDANCE_CLOCK_OUT", "Attendance", attendanceId, null,
            "Clock-out: " + staffName + " (Hours: " + hoursWorked + ", Restaurant: " + restaurantId + ")");
    }

    // ── Helper Methods ──────────────────────────────────────────────────────

    private String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON", e);
            return object.toString();
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}

// Made with Bob
