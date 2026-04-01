package com.dts.restro.customer.service;

import com.dts.restro.audit.service.AuditService;
import com.dts.restro.customer.dto.CreateCustomerRequest;
import com.dts.restro.customer.dto.CustomerDTO;
import com.dts.restro.customer.dto.UpdateCustomerRequest;
import com.dts.restro.customer.entity.Customer;
import com.dts.restro.customer.repository.CustomerRepository;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    
    @Transactional
    public CustomerDTO createCustomer(Long restaurantId, CreateCustomerRequest request) {
        log.info("Creating customer for restaurant: {}", restaurantId);
        
        // Check for duplicate phone
        if (customerRepository.existsByRestaurantIdAndPhone(restaurantId, request.getPhone())) {
            throw new DuplicateResourceException("Customer with phone " + request.getPhone() + " already exists");
        }
        
        // Check for duplicate email if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (customerRepository.existsByRestaurantIdAndEmail(restaurantId, request.getEmail())) {
                throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
            }
        }
        
        // Check for duplicate GSTIN if provided
        if (request.getGstin() != null && !request.getGstin().isEmpty()) {
            if (customerRepository.existsByRestaurantIdAndGstin(restaurantId, request.getGstin())) {
                throw new DuplicateResourceException("Customer with GSTIN " + request.getGstin() + " already exists");
            }
        }
        
        Customer customer = Customer.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gstin(request.getGstin())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.ZERO)
                .creditBalance(BigDecimal.ZERO)
                .loyaltyPoints(0)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .isActive(true)
                .notes(request.getNotes())
                .build();
        
        customer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", customer.getId());
        
        // Audit log
        try {
            auditService.logCustomerCreate(restaurantId, customer.getId(), customer.getName());
        } catch (Exception e) {
            log.error("Failed to log customer creation audit", e);
        }
        
        return toDTO(customer);
    }
    
    @Transactional
    public CustomerDTO updateCustomer(Long restaurantId, Long customerId, UpdateCustomerRequest request) {
        log.info("Updating customer: {} for restaurant: {}", customerId, restaurantId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        
        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByRestaurantIdAndPhone(restaurantId, request.getPhone())) {
                throw new DuplicateResourceException("Customer with phone " + request.getPhone() + " already exists");
            }
            customer.setPhone(request.getPhone());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByRestaurantIdAndEmail(restaurantId, request.getEmail())) {
                throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
            }
            customer.setEmail(request.getEmail());
        }
        
        if (request.getGstin() != null && !request.getGstin().equals(customer.getGstin())) {
            if (customerRepository.existsByRestaurantIdAndGstin(restaurantId, request.getGstin())) {
                throw new DuplicateResourceException("Customer with GSTIN " + request.getGstin() + " already exists");
            }
            customer.setGstin(request.getGstin());
        }
        
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getCity() != null) customer.setCity(request.getCity());
        if (request.getState() != null) customer.setState(request.getState());
        if (request.getPincode() != null) customer.setPincode(request.getPincode());
        if (request.getCreditLimit() != null) customer.setCreditLimit(request.getCreditLimit());
        if (request.getIsActive() != null) customer.setIsActive(request.getIsActive());
        if (request.getNotes() != null) customer.setNotes(request.getNotes());
        
        customer = customerRepository.save(customer);
        log.info("Customer updated successfully: {}", customerId);
        
        // Audit log
        try {
            auditService.logCustomerUpdate(restaurantId, customerId, null, customer);
        } catch (Exception e) {
            log.error("Failed to log customer update audit", e);
        }
        
        return toDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(Long restaurantId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        return toDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByPhone(Long restaurantId, String phone) {
        Customer customer = customerRepository.findByRestaurantIdAndPhone(restaurantId, phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));
        
        return toDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Long restaurantId, Pageable pageable) {
        return customerRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getActiveCustomers(Long restaurantId, Pageable pageable) {
        return customerRepository.findByRestaurantIdAndIsActive(restaurantId, true, pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerDTO> searchCustomers(Long restaurantId, String search, Pageable pageable) {
        return customerRepository.searchCustomers(restaurantId, search, pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerDTO> getTopCustomers(Long restaurantId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return customerRepository.findTopCustomersBySpent(restaurantId, pageable)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteCustomer(Long restaurantId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        String customerName = customer.getName();
        customerRepository.delete(customer);
        log.info("Customer deleted: {}", customerId);
        
        // Audit log
        try {
            auditService.logCustomerDelete(restaurantId, customerId, customerName);
        } catch (Exception e) {
            log.error("Failed to log customer deletion audit", e);
        }
    }
    
    @Transactional
    public CustomerDTO addCreditBalance(Long restaurantId, Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        customer.addCreditBalance(amount);
        customer = customerRepository.save(customer);
        
        // Audit log
        try {
            auditService.logCreditOperation(restaurantId, customerId, "ADD", amount, "Credit added");
        } catch (Exception e) {
            log.error("Failed to log credit operation audit", e);
        }
        
        return toDTO(customer);
    }
    
    @Transactional
    public CustomerDTO reduceCreditBalance(Long restaurantId, Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        customer.reduceCreditBalance(amount);
        customer = customerRepository.save(customer);
        
        // Audit log
        try {
            auditService.logCreditOperation(restaurantId, customerId, "REDUCE", amount, "Credit reduced");
        } catch (Exception e) {
            log.error("Failed to log credit operation audit", e);
        }
        
        return toDTO(customer);
    }
    
    @Transactional
    public CustomerDTO addLoyaltyPoints(Long restaurantId, Long customerId, Integer points) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        customer.addLoyaltyPoints(points);
        customer = customerRepository.save(customer);
        
        // Audit log
        try {
            auditService.logLoyaltyOperation(restaurantId, customerId, "ADD", points, "Loyalty points added");
        } catch (Exception e) {
            log.error("Failed to log loyalty operation audit", e);
        }
        
        return toDTO(customer);
    }
    
    @Transactional
    public CustomerDTO redeemLoyaltyPoints(Long restaurantId, Long customerId, Integer points) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        
        if (!customer.redeemLoyaltyPoints(points)) {
            throw new IllegalStateException("Insufficient loyalty points");
        }
        
        customer = customerRepository.save(customer);
        
        // Audit log
        try {
            auditService.logLoyaltyOperation(restaurantId, customerId, "REDEEM", points, "Loyalty points redeemed");
        } catch (Exception e) {
            log.error("Failed to log loyalty operation audit", e);
        }
        
        return toDTO(customer);
    }
    
    private CustomerDTO toDTO(Customer customer) {
        BigDecimal availableCredit = BigDecimal.ZERO;
        if (customer.getCreditLimit() != null && customer.getCreditBalance() != null) {
            availableCredit = customer.getCreditLimit().subtract(customer.getCreditBalance());
        }
        
        return CustomerDTO.builder()
                .id(customer.getId())
                .restaurantId(customer.getRestaurantId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .gstin(customer.getGstin())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .pincode(customer.getPincode())
                .creditLimit(customer.getCreditLimit())
                .creditBalance(customer.getCreditBalance())
                .availableCredit(availableCredit)
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalOrders(customer.getTotalOrders())
                .totalSpent(customer.getTotalSpent())
                .isActive(customer.getIsActive())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}

// Made with Bob
