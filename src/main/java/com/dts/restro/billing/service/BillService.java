package com.dts.restro.billing.service;

import com.dts.restro.billing.dto.CreateBillRequest;
import com.dts.restro.billing.dto.EnhancedBillDTO;
import com.dts.restro.billing.entity.Bill;
import com.dts.restro.billing.entity.BillItem;
import com.dts.restro.billing.enums.OrderType;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.enums.TaxType;
import com.dts.restro.billing.repository.BillItemRepository;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.customer.entity.Customer;
import com.dts.restro.customer.repository.CustomerRepository;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.order.entity.Order;
import com.dts.restro.order.entity.OrderItem;
import com.dts.restro.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for billing operations with Indian GST support
 * Ensures multi-tenancy by always validating restaurantId
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * Generate bill from order with GST calculations
     * Multi-tenant: Validates restaurantId matches order's restaurant
     */
    @Transactional
    public EnhancedBillDTO generateBill(Long restaurantId, CreateBillRequest request) {
        log.info("Generating bill for restaurant: {}, order: {}", restaurantId, request.getOrderId());
        
        // Fetch and validate order belongs to restaurant (multi-tenancy check)
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Order not found for this restaurant");
        }
        
        // Check if bill already exists for this order
        if (billRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new IllegalStateException("Bill already exists for this order");
        }
        
        // Generate unique bill number
        String billNumber = generateBillNumber(restaurantId);
        
        // Create bill entity
        Bill bill = Bill.builder()
                .restaurantId(restaurantId)
                .billNumber(billNumber)
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .tableNumber(request.getTableNumber())
                .orderType(request.getOrderType() != null ? request.getOrderType() : OrderType.DINE_IN)
                .taxType(request.getTaxType() != null ? request.getTaxType() : TaxType.CGST_SGST)
                .serviceChargeRate(request.getServiceChargeRate() != null ? request.getServiceChargeRate() : new BigDecimal("10.00"))
                .packagingCharges(request.getPackagingCharges() != null ? request.getPackagingCharges() : BigDecimal.ZERO)
                .deliveryCharges(request.getDeliveryCharges() != null ? request.getDeliveryCharges() : BigDecimal.ZERO)
                .discountRate(request.getDiscountRate() != null ? request.getDiscountRate() : BigDecimal.ZERO)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .discountReason(request.getDiscountReason())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH)
                .paymentReference(request.getPaymentReference())
                .captainId(request.getCaptainId())
                .cashierId(request.getCashierId())
                .build();
        
        // Calculate amounts
        calculateBillAmounts(bill, order);
        
        // Save bill
        bill = billRepository.save(bill);
        
        // Create bill items from order items
        List<BillItem> billItems = createBillItems(bill.getId(), order);
        billItemRepository.saveAll(billItems);
        
        // Process payment if paid amount provided
        if (request.getPaidAmount() != null && request.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            bill.markAsPaid(request.getPaymentMethod(), request.getPaidAmount(), request.getPaymentReference());
            billRepository.save(bill);
            
            // Update customer statistics if customer provided
            if (request.getCustomerId() != null) {
                updateCustomerStats(restaurantId, request.getCustomerId(), bill.getGrandTotal());
            }
        }
        
        log.info("Bill generated successfully: {}", billNumber);
        return toBillDTO(bill, billItems);
    }
    
    /**
     * Calculate all bill amounts including GST
     */
    private void calculateBillAmounts(Bill bill, Order order) {
        // Calculate subtotal from order items
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        bill.setSubtotal(subtotal);
        
        // Calculate service charge
        BigDecimal serviceChargeAmount = subtotal
                .multiply(bill.getServiceChargeRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        bill.setServiceChargeAmount(serviceChargeAmount);
        
        // Taxable amount = subtotal + service charge
        BigDecimal taxableAmount = subtotal.add(serviceChargeAmount);
        bill.setTaxableAmount(taxableAmount);
        
        // Apply discount
        BigDecimal discountAmount;
        if (bill.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = taxableAmount
                    .multiply(bill.getDiscountRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = bill.getDiscountAmount();
        }
        bill.setDiscountAmount(discountAmount);
        
        // Amount after discount
        BigDecimal amountAfterDiscount = taxableAmount.subtract(discountAmount);
        
        // Calculate GST based on tax type
        if (bill.getTaxType() == TaxType.CGST_SGST) {
            // Intra-state: 9% CGST + 9% SGST
            bill.setCgstRate(new BigDecimal("9.00"));
            bill.setSgstRate(new BigDecimal("9.00"));
            bill.setIgstRate(BigDecimal.ZERO);
            
            BigDecimal cgstAmount = amountAfterDiscount
                    .multiply(bill.getCgstRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal sgstAmount = amountAfterDiscount
                    .multiply(bill.getSgstRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            bill.setCgstAmount(cgstAmount);
            bill.setSgstAmount(sgstAmount);
            bill.setIgstAmount(BigDecimal.ZERO);
            bill.setTotalTax(cgstAmount.add(sgstAmount));
        } else if (bill.getTaxType() == TaxType.IGST) {
            // Inter-state: 18% IGST
            bill.setCgstRate(BigDecimal.ZERO);
            bill.setSgstRate(BigDecimal.ZERO);
            bill.setIgstRate(new BigDecimal("18.00"));
            
            BigDecimal igstAmount = amountAfterDiscount
                    .multiply(bill.getIgstRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            bill.setCgstAmount(BigDecimal.ZERO);
            bill.setSgstAmount(BigDecimal.ZERO);
            bill.setIgstAmount(igstAmount);
            bill.setTotalTax(igstAmount);
        } else {
            // NO_TAX
            bill.setCgstRate(BigDecimal.ZERO);
            bill.setSgstRate(BigDecimal.ZERO);
            bill.setIgstRate(BigDecimal.ZERO);
            bill.setCgstAmount(BigDecimal.ZERO);
            bill.setSgstAmount(BigDecimal.ZERO);
            bill.setIgstAmount(BigDecimal.ZERO);
            bill.setTotalTax(BigDecimal.ZERO);
        }
        
        // Calculate total before round-off
        BigDecimal totalBeforeRoundOff = amountAfterDiscount
                .add(bill.getTotalTax())
                .add(bill.getPackagingCharges())
                .add(bill.getDeliveryCharges());
        bill.setTotalBeforeRoundOff(totalBeforeRoundOff);
        
        // Calculate round-off (to nearest rupee)
        BigDecimal rounded = totalBeforeRoundOff.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOffAmount = rounded.subtract(totalBeforeRoundOff);
        bill.setRoundOffAmount(roundOffAmount);
        
        // Grand total
        bill.setGrandTotal(rounded);
    }
    
    /**
     * Create bill items from order items
     */
    private List<BillItem> createBillItems(Long billId, Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> BillItem.builder()
                        .billId(billId)
                        .menuItemId(orderItem.getMenuItemId())
                        .itemName(orderItem.getItemName())
                        .itemCode(orderItem.getItemCode())
                        .category(orderItem.getCategory())
                        .quantity(orderItem.getQuantity())
                        .unitPrice(orderItem.getPrice())
                        .itemTotal(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())))
                        .spiceLevel(orderItem.getSpiceLevel())
                        .specialInstructions(orderItem.getSpecialInstructions())
                        .isVeg(orderItem.getIsVeg())
                        .isJain(orderItem.getIsJain())
                        .hsnCode(orderItem.getHsnCode())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Generate unique bill number
     * Format: BILL-YYYYMMDD-XXXX
     */
    private String generateBillNumber(Long restaurantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BILL-" + dateStr + "-";
        
        // Find last bill number for today
        int sequence = 1;
        String billNumber;
        do {
            billNumber = prefix + String.format("%04d", sequence);
            sequence++;
        } while (billRepository.existsByRestaurantIdAndBillNumber(restaurantId, billNumber));
        
        return billNumber;
    }
    
    /**
     * Update customer statistics after payment
     * Multi-tenant: Validates customer belongs to restaurant
     */
    private void updateCustomerStats(Long restaurantId, Long customerId, BigDecimal amount) {
        customerRepository.findById(customerId).ifPresent(customer -> {
            if (customer.getRestaurantId().equals(restaurantId)) {
                customer.updateOrderStats(amount);
                
                // Add loyalty points (1 point per 100 rupees)
                int points = amount.divide(new BigDecimal("100"), 0, RoundingMode.DOWN).intValue();
                if (points > 0) {
                    customer.addLoyaltyPoints(points);
                }
                
                customerRepository.save(customer);
            }
        });
    }
    
    /**
     * Get bill by ID
     * Multi-tenant: Validates bill belongs to restaurant
     */
    @Transactional(readOnly = true)
    public EnhancedBillDTO getBill(Long restaurantId, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        if (!bill.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Bill not found for this restaurant");
        }
        
        List<BillItem> items = billItemRepository.findByBillId(billId);
        return toBillDTO(bill, items);
    }
    
    /**
     * Get bill by bill number
     * Multi-tenant: Uses restaurantId in query
     */
    @Transactional(readOnly = true)
    public EnhancedBillDTO getBillByNumber(Long restaurantId, String billNumber) {
        Bill bill = billRepository.findByRestaurantIdAndBillNumber(restaurantId, billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        List<BillItem> items = billItemRepository.findByBillId(bill.getId());
        return toBillDTO(bill, items);
    }
    
    /**
     * Get all bills for restaurant
     */
    @Transactional(readOnly = true)
    public Page<EnhancedBillDTO> getAllBills(Long restaurantId, Pageable pageable) {
        return billRepository.findByRestaurantId(restaurantId, pageable)
                .map(bill -> toBillDTO(bill, billItemRepository.findByBillId(bill.getId())));
    }
    
    /**
     * Process payment for a bill
     * Multi-tenant: Validates bill belongs to restaurant
     */
    @Transactional
    public EnhancedBillDTO processPayment(Long restaurantId, Long billId, PaymentMethod method, 
                                         BigDecimal amount, String reference) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        if (!bill.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Bill not found for this restaurant");
        }
        
        if (bill.getIsPaid()) {
            throw new IllegalStateException("Bill is already paid");
        }
        
        bill.markAsPaid(method, amount, reference);
        bill = billRepository.save(bill);
        
        // Update customer statistics
        if (bill.getCustomerId() != null) {
            updateCustomerStats(restaurantId, bill.getCustomerId(), bill.getGrandTotal());
        }
        
        List<BillItem> items = billItemRepository.findByBillId(billId);
        return toBillDTO(bill, items);
    }
    
    /**
     * Cancel a bill
     * Multi-tenant: Validates bill belongs to restaurant
     */
    @Transactional
    public void cancelBill(Long restaurantId, Long billId, String reason) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        if (!bill.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Bill not found for this restaurant");
        }
        
        if (bill.getIsPaid()) {
            throw new IllegalStateException("Cannot cancel a paid bill");
        }
        
        bill.cancel(reason);
        billRepository.save(bill);
    }
    
    /**
     * Convert Bill entity to DTO
     */
    private EnhancedBillDTO toBillDTO(Bill bill, List<BillItem> items) {
        List<EnhancedBillDTO.BillItemDTO> itemDTOs = items.stream()
                .map(item -> EnhancedBillDTO.BillItemDTO.builder()
                        .itemId(item.getId())
                        .itemName(item.getItemName())
                        .itemCode(item.getItemCode())
                        .category(item.getCategory())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .itemTotal(item.getItemTotal())
                        .spiceLevel(item.getSpiceLevel())
                        .specialInstructions(item.getSpecialInstructions())
                        .isVeg(item.getIsVeg())
                        .isJain(item.getIsJain())
                        .hsnCode(item.getHsnCode())
                        .build())
                .collect(Collectors.toList());
        
        return EnhancedBillDTO.builder()
                .billId(bill.getId())
                .billNumber(bill.getBillNumber())
                .restaurantId(bill.getRestaurantId())
                .orderId(bill.getOrderId())
                .customerId(bill.getCustomerId())
                .tableNumber(bill.getTableNumber())
                .orderType(bill.getOrderType())
                .items(itemDTOs)
                .subtotal(bill.getSubtotal())
                .taxableAmount(bill.getTaxableAmount())
                .taxType(bill.getTaxType())
                .cgstRate(bill.getCgstRate())
                .sgstRate(bill.getSgstRate())
                .igstRate(bill.getIgstRate())
                .cgstAmount(bill.getCgstAmount())
                .sgstAmount(bill.getSgstAmount())
                .igstAmount(bill.getIgstAmount())
                .totalTax(bill.getTotalTax())
                .serviceChargeRate(bill.getServiceChargeRate())
                .serviceChargeAmount(bill.getServiceChargeAmount())
                .packagingCharges(bill.getPackagingCharges())
                .deliveryCharges(bill.getDeliveryCharges())
                .discountRate(bill.getDiscountRate())
                .discountAmount(bill.getDiscountAmount())
                .discountReason(bill.getDiscountReason())
                .totalBeforeRoundOff(bill.getTotalBeforeRoundOff())
                .roundOffAmount(bill.getRoundOffAmount())
                .grandTotal(bill.getGrandTotal())
                .paymentMethod(bill.getPaymentMethod())
                .paymentReference(bill.getPaymentReference())
                .paidAmount(bill.getPaidAmount())
                .changeAmount(bill.getChangeAmount())
                .isPaid(bill.getIsPaid())
                .isCancelled(bill.getIsCancelled())
                .cancellationReason(bill.getCancellationReason())
                .build();
    }
}