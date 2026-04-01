package com.dts.restro.customer.controller;

import com.dts.restro.customer.dto.CreateCustomerRequest;
import com.dts.restro.customer.dto.CustomerDTO;
import com.dts.restro.customer.dto.UpdateCustomerRequest;
import com.dts.restro.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Customer management
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerDTO> createCustomer(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDTO customer = customerService.createCustomer(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
    
    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerDTO customer = customerService.updateCustomer(restaurantId, customerId, request);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'CAPTAIN')")
    public ResponseEntity<CustomerDTO> getCustomer(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId) {
        CustomerDTO customer = customerService.getCustomer(restaurantId, customerId);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'CAPTAIN')")
    public ResponseEntity<CustomerDTO> getCustomerByPhone(
            @PathVariable Long restaurantId,
            @PathVariable String phone) {
        CustomerDTO customer = customerService.getCustomerByPhone(restaurantId, phone);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        Page<CustomerDTO> customers = customerService.getAllCustomers(restaurantId, pageable);
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'CAPTAIN')")
    public ResponseEntity<Page<CustomerDTO>> getActiveCustomers(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        Page<CustomerDTO> customers = customerService.getActiveCustomers(restaurantId, pageable);
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'CAPTAIN')")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @PathVariable Long restaurantId,
            @RequestParam String query,
            Pageable pageable) {
        Page<CustomerDTO> customers = customerService.searchCustomers(restaurantId, query, pageable);
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/top")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerDTO>> getTopCustomers(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        List<CustomerDTO> customers = customerService.getTopCustomers(restaurantId, limit);
        return ResponseEntity.ok(customers);
    }
    
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId) {
        customerService.deleteCustomer(restaurantId, customerId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{customerId}/credit/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerDTO> addCreditBalance(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        CustomerDTO customer = customerService.addCreditBalance(restaurantId, customerId, amount);
        return ResponseEntity.ok(customer);
    }
    
    @PostMapping("/{customerId}/credit/reduce")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerDTO> reduceCreditBalance(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        CustomerDTO customer = customerService.reduceCreditBalance(restaurantId, customerId, amount);
        return ResponseEntity.ok(customer);
    }
    
    @PostMapping("/{customerId}/loyalty/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerDTO> addLoyaltyPoints(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId,
            @RequestBody Map<String, Integer> request) {
        Integer points = request.get("points");
        CustomerDTO customer = customerService.addLoyaltyPoints(restaurantId, customerId, points);
        return ResponseEntity.ok(customer);
    }
    
    @PostMapping("/{customerId}/loyalty/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerDTO> redeemLoyaltyPoints(
            @PathVariable Long restaurantId,
            @PathVariable Long customerId,
            @RequestBody Map<String, Integer> request) {
        Integer points = request.get("points");
        CustomerDTO customer = customerService.redeemLoyaltyPoints(restaurantId, customerId, points);
        return ResponseEntity.ok(customer);
    }
}

// Made with Bob
