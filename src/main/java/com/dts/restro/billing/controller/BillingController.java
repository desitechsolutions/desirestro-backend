package com.dts.restro.billing.controller;

import com.dts.restro.billing.dto.CreateBillRequest;
import com.dts.restro.billing.dto.EnhancedBillDTO;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST Controller for Billing operations
 * All endpoints enforce multi-tenancy through restaurantId path variable
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/bills")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    
    /**
     * Generate a new bill from an order
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<EnhancedBillDTO> generateBill(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateBillRequest request) {
        EnhancedBillDTO bill = billingService.generateBill(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
    
    /**
     * Get bill by ID
     */
    @GetMapping("/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'CAPTAIN')")
    public ResponseEntity<EnhancedBillDTO> getBill(
            @PathVariable Long restaurantId,
            @PathVariable Long billId) {
        EnhancedBillDTO bill = billingService.getBill(restaurantId, billId);
        return ResponseEntity.ok(bill);
    }
    
    /**
     * Get bill by bill number
     */
    @GetMapping("/number/{billNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'CAPTAIN')")
    public ResponseEntity<EnhancedBillDTO> getBillByNumber(
            @PathVariable Long restaurantId,
            @PathVariable String billNumber) {
        EnhancedBillDTO bill = billingService.getBillByNumber(restaurantId, billNumber);
        return ResponseEntity.ok(bill);
    }
    
    /**
     * Get all bills for restaurant
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<EnhancedBillDTO>> getAllBills(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        Page<EnhancedBillDTO> bills = billingService.getAllBills(restaurantId, pageable);
        return ResponseEntity.ok(bills);
    }
    
    /**
     * Get bills by date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<EnhancedBillDTO>> getBillsByDateRange(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<EnhancedBillDTO> bills = billingService.getBillsByDateRange(
                restaurantId, startDate, endDate, pageable);
        return ResponseEntity.ok(bills);
    }
    
    /**
     * Get bills by customer
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<EnhancedBillDTO>> getBillsByCustomer(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId,
            Pageable pageable) {
        Page<EnhancedBillDTO> bills = billingService.getBillsByCustomer(
                restaurantId, customerId, pageable);
        return ResponseEntity.ok(bills);
    }
    
    /**
     * Get unpaid bills
     */
    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<EnhancedBillDTO>> getUnpaidBills(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        Page<EnhancedBillDTO> bills = billingService.getUnpaidBills(restaurantId, pageable);
        return ResponseEntity.ok(bills);
    }
    
    /**
     * Process payment for a bill
     */
    @PostMapping("/{billId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<EnhancedBillDTO> processPayment(
            @PathVariable Long restaurantId,
            @PathVariable Long billId,
            @RequestBody Map<String, Object> paymentRequest) {
        
        PaymentMethod method = PaymentMethod.valueOf((String) paymentRequest.get("paymentMethod"));
        BigDecimal amount = new BigDecimal(paymentRequest.get("amount").toString());
        String reference = (String) paymentRequest.get("reference");
        
        EnhancedBillDTO bill = billingService.processPayment(
                restaurantId, billId, method, amount, reference);
        return ResponseEntity.ok(bill);
    }
    
    /**
     * Cancel a bill
     */
    @PostMapping("/{billId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Void> cancelBill(
            @PathVariable Long restaurantId,
            @PathVariable Long billId,
            @RequestBody Map<String, String> request) {
        
        String reason = request.get("reason");
        billingService.cancelBill(restaurantId, billId, reason);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get sales summary for date range
     */
    @GetMapping("/sales-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSalesSummary(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> summary = billingService.getSalesSummary(
                restaurantId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get sales by payment method
     */
    @GetMapping("/sales-by-payment-method")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<PaymentMethod, BigDecimal>> getSalesByPaymentMethod(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<PaymentMethod, BigDecimal> sales = billingService.getSalesByPaymentMethod(
                restaurantId, startDate, endDate);
        return ResponseEntity.ok(sales);
    }
    
    /**
     * Get top selling items
     */
    @GetMapping("/top-items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingItems(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Map<String, Object>> items = billingService.getTopSellingItems(
                restaurantId, startDate, endDate, limit);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Get sales by category
     */
    @GetMapping("/sales-by-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getSalesByCategory(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, BigDecimal> sales = billingService.getSalesByCategory(
                restaurantId, startDate, endDate);
        return ResponseEntity.ok(sales);
    }
}

// Made with Bob
