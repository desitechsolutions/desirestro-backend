package com.dts.restro.billing.service;

import com.dts.restro.billing.dto.CreateBillRequest;
import com.dts.restro.billing.dto.EnhancedBillDTO;
import com.dts.restro.billing.entity.Bill;
import com.dts.restro.billing.entity.BillItem;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.enums.TaxType;
import com.dts.restro.billing.repository.BillItemRepository;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.customer.entity.Customer;
import com.dts.restro.customer.repository.CustomerRepository;
import com.dts.restro.exception.BusinessValidationException;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.order.entity.KOT;
import com.dts.restro.order.entity.KOTItem;
import com.dts.restro.order.repository.KOTRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final KOTRepository kotRepository;
    private final CustomerRepository customerRepository;
    private final DailySalesSummaryService dailySalesSummaryService;
    private final com.dts.restro.audit.service.AuditService auditService;

    /**
     * Generate a bill from a KOT
     */
    @Transactional
    public EnhancedBillDTO generateBill(Long restaurantId, CreateBillRequest request) {
        log.info("Generating bill for restaurant: {}, Order: {}", restaurantId, request.getOrderId());

        // Validate restaurant ownership of KOT
        KOT kot = kotRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("KOT not found"));

        if (!kot.getRestaurant().getId().equals(restaurantId)) {
            throw new BusinessValidationException("KOT does not belong to this restaurant");
        }

        // Create bill
        Bill bill = new Bill();
        bill.setRestaurantId(restaurantId);
        bill.setOrderId(kot.getId());
        bill.setBillNumber(generateBillNumber(restaurantId));
        bill.setBillTime(LocalDateTime.now());
        bill.setTableNumber(kot.getParty() != null && kot.getParty().getTable() != null
                ? parseTableNumber(kot.getParty().getTable().getTableNumber()) : null);
        bill.setCustomerId(request.getCustomerId());
        bill.setTaxType(request.getTaxType());
        bill.setPaymentMethod(request.getPaymentMethod());

        // Create bill items from KOT items
        List<BillItem> billItems = createBillItems(bill, kot.getItems());

        // Calculate all amounts
        calculateBillAmounts(bill, billItems, request);

        // Save bill and items
        Bill savedBill = billRepository.save(bill);
        billItems.forEach(item -> item.setBillId(savedBill.getId()));
        billItemRepository.saveAll(billItems);

        log.info("Bill generated successfully: {}", savedBill.getBillNumber());
        
        // Audit log
        try {
            auditService.logBillGenerate(restaurantId, savedBill.getId(),
                savedBill.getBillNumber(), savedBill);
        } catch (Exception e) {
            log.error("Failed to log bill generation audit", e);
        }
        
        return toBillDTO(savedBill, billItems);
    }

    private Integer parseTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.isBlank()) return null;
        try {
            return Integer.parseInt(tableNumber.trim());
        } catch (NumberFormatException e) {
            log.warn("Non-numeric table number '{}'; storing null", tableNumber);
            return null;
        }
    }

    /**
     * Create bill items from KOT items
     */
    private List<BillItem> createBillItems(Bill bill, List<KOTItem> kotItems) {
        return kotItems.stream()
                .map(kotItem -> {
                    BillItem billItem = new BillItem();
                    billItem.setMenuItemId(kotItem.getMenuItemId());
                    billItem.setItemName(kotItem.getMenuItemName());
                    billItem.setQuantity(kotItem.getQuantity());
                    billItem.setUnitPrice(BigDecimal.valueOf(kotItem.getPrice()));
                    billItem.setSpiceLevel(kotItem.getSpiceLevel());
                    billItem.setSpecialInstructions(kotItem.getSpecialInstructions());
                    billItem.setIsJain(kotItem.getIsJain());
                    billItem.setHsnCode(kotItem.getHsnCode());
                    billItem.calculateTotal();
                    return billItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate all bill amounts including GST, service charge, discount, and round-off
     */
    private void calculateBillAmounts(Bill bill, List<BillItem> billItems, CreateBillRequest request) {
        // Calculate subtotal
        BigDecimal subtotal = billItems.stream()
                .map(BillItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setSubtotal(subtotal);

        // Calculate service charge (on subtotal before tax)
        BigDecimal serviceChargeRate = request.getServiceChargeRate() != null 
                ? request.getServiceChargeRate() 
                : BigDecimal.ZERO;
        BigDecimal serviceCharge = subtotal.multiply(serviceChargeRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        bill.setServiceChargeAmount(serviceCharge);

        // Add packaging and delivery charges
        BigDecimal packagingCharges = request.getPackagingCharges() != null 
                ? request.getPackagingCharges() 
                : BigDecimal.ZERO;
        BigDecimal deliveryCharges = request.getDeliveryCharges() != null 
                ? request.getDeliveryCharges() 
                : BigDecimal.ZERO;
        bill.setPackagingCharges(packagingCharges);
        bill.setDeliveryCharges(deliveryCharges);

        // Calculate total before tax
        BigDecimal totalBeforeTax = subtotal.add(serviceCharge)
                .add(packagingCharges)
                .add(deliveryCharges);

        // Apply discount before tax
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscountRate() != null && request.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = totalBeforeTax.multiply(request.getDiscountRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            bill.setDiscountRate(request.getDiscountRate());
        } else if (request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = request.getDiscountAmount();
            bill.setDiscountAmount(discountAmount);
        }
        bill.setDiscountAmount(discountAmount);

        // Calculate taxable amount
        BigDecimal taxableAmount = totalBeforeTax.subtract(discountAmount);

        // Calculate GST based on tax type
        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;

        if (request.getTaxType() == TaxType.CGST_SGST) {
            // CGST + SGST (9% + 9% = 18%)
            BigDecimal cgstRate = BigDecimal.valueOf(9);
            BigDecimal sgstRate = BigDecimal.valueOf(9);
            cgstAmount = taxableAmount.multiply(cgstRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            sgstAmount = taxableAmount.multiply(sgstRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            bill.setCgstRate(cgstRate);
            bill.setSgstRate(sgstRate);
            bill.setCgstAmount(cgstAmount);
            bill.setSgstAmount(sgstAmount);
        } else if (request.getTaxType() == TaxType.IGST) {
            // IGST (18%)
            BigDecimal igstRate = BigDecimal.valueOf(18);
            igstAmount = taxableAmount.multiply(igstRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            bill.setIgstRate(igstRate);
            bill.setIgstAmount(igstAmount);
        }

        BigDecimal totalTax = cgstAmount.add(sgstAmount).add(igstAmount);
        bill.setTotalTax(totalTax);

        // Calculate total amount
        BigDecimal totalAmount = taxableAmount.add(totalTax);
        bill.setTotalBeforeRoundOff(totalAmount);

        // Calculate round-off (to nearest rupee)
        BigDecimal roundedTotal = totalAmount.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff = roundedTotal.subtract(totalAmount);
        bill.setRoundOffAmount(roundOff);

        // Set grand total
        BigDecimal grandTotal = totalAmount.add(roundOff);
        bill.setGrandTotal(grandTotal);
    }

    /**
     * Generate unique bill number for the restaurant
     * Format: BILL-YYYYMMDD-XXXX
     */
    private String generateBillNumber(Long restaurantId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BILL-" + dateStr + "-";

        // Find the last bill number for today
        String lastBillNumber = billRepository.findByRestaurantIdAndBillNumberStartingWith(
                restaurantId, prefix)
                .stream()
                .map(Bill::getBillNumber)
                .max(String::compareTo)
                .orElse(prefix + "0000");

        // Extract sequence number and increment
        int sequence = Integer.parseInt(lastBillNumber.substring(lastBillNumber.length() - 4)) + 1;
        return prefix + String.format("%04d", sequence);
    }

    /**
     * Process payment for a bill
     */
    @Transactional
    public EnhancedBillDTO processPayment(Long restaurantId, Long billId, PaymentMethod paymentMethod,
                                          BigDecimal amount, String reference) {
        log.info("Processing payment for bill: {}, method: {}", billId, paymentMethod);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!bill.getRestaurantId().equals(restaurantId)) {
            throw new BusinessValidationException("Bill does not belong to this restaurant");
        }

        if (Boolean.TRUE.equals(bill.getIsPaid())) {
            throw new BusinessValidationException("Bill is already paid");
        }

        bill.markAsPaid(paymentMethod, amount, reference);
        Bill savedBill = billRepository.save(bill);

        // Update customer statistics if customer is linked
        if (bill.getCustomerId() != null) {
            updateCustomerStats(bill);
        }

        // Update daily sales summary
        try {
            dailySalesSummaryService.updateSummaryOnBillPayment(savedBill);
        } catch (Exception e) {
            log.error("Failed to update daily sales summary for bill: {}", savedBill.getBillNumber(), e);
            // Don't fail the payment if summary update fails
        }

        log.info("Payment processed successfully for bill: {}", bill.getBillNumber());

        // Audit log
        try {
            auditService.logBillPayment(restaurantId, savedBill.getId(),
                savedBill.getBillNumber(), paymentMethod.toString(), savedBill.getGrandTotal());
        } catch (Exception e) {
            log.error("Failed to log bill payment audit", e);
        }

        return toBillDTO(savedBill, billItemRepository.findByBillId(savedBill.getId()));
    }

    /**
     * Update customer statistics after payment
     */
    private void updateCustomerStats(Bill bill) {
        Customer customer = customerRepository.findById(bill.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!customer.getRestaurantId().equals(bill.getRestaurantId())) {
            throw new BusinessValidationException("Customer does not belong to this restaurant");
        }

        // Update order count and total spent
        customer.updateOrderStats(bill.getGrandTotal());

        // Award loyalty points (1 point per ₹100 spent)
        int loyaltyPoints = bill.getGrandTotal()
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .intValue();
        if (loyaltyPoints > 0) {
            customer.addLoyaltyPoints(loyaltyPoints);
        }

        customerRepository.save(customer);
        log.info("Updated customer stats: {}, loyalty points: {}", customer.getId(), loyaltyPoints);
    }

    /**
     * Cancel a bill
     */
    @Transactional
    public EnhancedBillDTO cancelBill(Long restaurantId, Long billId, String reason) {
        log.info("Cancelling bill: {}, reason: {}", billId, reason);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!bill.getRestaurantId().equals(restaurantId)) {
            throw new BusinessValidationException("Bill does not belong to this restaurant");
        }

        if (Boolean.TRUE.equals(bill.getIsCancelled())) {
            throw new BusinessValidationException("Bill is already cancelled");
        }

        bill.cancel(reason);
        Bill savedBill = billRepository.save(bill);

        // Update daily sales summary
        try {
            dailySalesSummaryService.updateSummaryOnBillCancellation(savedBill);
        } catch (Exception e) {
            log.error("Failed to update daily sales summary for cancelled bill: {}", savedBill.getBillNumber(), e);
            // Don't fail the cancellation if summary update fails
        }

        log.info("Bill cancelled successfully: {}", bill.getBillNumber());
        
        // Audit log
        try {
            auditService.logBillCancel(restaurantId, savedBill.getId(),
                savedBill.getBillNumber(), reason);
        } catch (Exception e) {
            log.error("Failed to log bill cancellation audit", e);
        }
        
        return toBillDTO(savedBill, billItemRepository.findByBillId(savedBill.getId()));
    }

    /**
     * Get bill by ID
     */
    @Transactional(readOnly = true)
    public EnhancedBillDTO getBill(Long restaurantId, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!bill.getRestaurantId().equals(restaurantId)) {
            throw new BusinessValidationException("Bill does not belong to this restaurant");
        }

        return toBillDTO(bill, billItemRepository.findByBillId(billId));
    }

    /**
     * Get bill by bill number
     */
    @Transactional(readOnly = true)
    public EnhancedBillDTO getBillByNumber(Long restaurantId, String billNumber) {
        Bill bill = billRepository.findByRestaurantIdAndBillNumber(restaurantId, billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        return toBillDTO(bill, billItemRepository.findByBillId(bill.getId()));
    }

    /**
     * Get all bills for a restaurant
     */
    @Transactional(readOnly = true)
    public Page<EnhancedBillDTO> getAllBills(Long restaurantId, Pageable pageable) {
        return billRepository.findByRestaurantId(restaurantId, pageable)
                .map(bill -> toBillDTO(bill, billItemRepository.findByBillId(bill.getId())));
    }

    /**
     * Get bills by date range
     */
    @Transactional(readOnly = true)
    public Page<EnhancedBillDTO> getBillsByDateRange(Long restaurantId, LocalDateTime startDate, 
                                                     LocalDateTime endDate, Pageable pageable) {
        return billRepository.findByDateRange(restaurantId, startDate, endDate, pageable)
                .map(bill -> toBillDTO(bill, billItemRepository.findByBillId(bill.getId())));
    }

    /**
     * Get bills by customer
     */
    @Transactional(readOnly = true)
    public Page<EnhancedBillDTO> getBillsByCustomer(Long restaurantId, Long customerId, Pageable pageable) {
        return billRepository.findByRestaurantIdAndCustomerId(restaurantId, customerId, pageable)
                .map(bill -> toBillDTO(bill, billItemRepository.findByBillId(bill.getId())));
    }

    /**
     * Get unpaid bills
     */
    @Transactional(readOnly = true)
    public Page<EnhancedBillDTO> getUnpaidBills(Long restaurantId, Pageable pageable) {
        List<Bill> unpaidBills = billRepository.findUnpaidBills(restaurantId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), unpaidBills.size());
        List<EnhancedBillDTO> dtos = unpaidBills.subList(start, end).stream()
                .map(bill -> toBillDTO(bill, billItemRepository.findByBillId(bill.getId())))
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, unpaidBills.size());
    }

    /**
     * Get sales summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesSummary(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalSales = billRepository.getTotalSales(restaurantId, startDate, endDate);
        BigDecimal totalTax = billRepository.getTotalTax(restaurantId, startDate, endDate);
        Long totalBills = billRepository.countBillsByDateRange(restaurantId, startDate, endDate);
        BigDecimal avgBill = billRepository.getAverageBillValue(restaurantId, startDate, endDate);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSales", totalSales);
        summary.put("totalTax", totalTax);
        summary.put("totalBills", totalBills);
        summary.put("averageBillValue", avgBill);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        
        return summary;
    }

    /**
     * Get sales by payment method
     */
    @Transactional(readOnly = true)
    public Map<PaymentMethod, BigDecimal> getSalesByPaymentMethod(Long restaurantId, 
                                                                  LocalDateTime startDate, 
                                                                  LocalDateTime endDate) {
        List<Object[]> results = billRepository.getSalesByPaymentMethod(restaurantId, startDate, endDate);
        Map<PaymentMethod, BigDecimal> salesMap = new HashMap<>();
        
        for (Object[] result : results) {
            PaymentMethod method = (PaymentMethod) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            salesMap.put(method, amount);
        }
        
        return salesMap;
    }

    /**
     * Get top selling items
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingItems(Long restaurantId, LocalDateTime startDate, 
                                                        LocalDateTime endDate, int limit) {
        List<Object[]> results = billItemRepository.getTopSellingItems(restaurantId, startDate, endDate);
        
        return results.stream()
                .limit(limit)
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("menuItemId", result[0]);
                    item.put("itemName", result[1]);
                    item.put("totalQuantity", result[2]);
                    item.put("totalRevenue", result[3]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get sales by category
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getSalesByCategory(Long restaurantId, LocalDateTime startDate, 
                                                      LocalDateTime endDate) {
        List<Object[]> results = billItemRepository.getSalesByCategory(restaurantId, startDate, endDate);
        Map<String, BigDecimal> salesMap = new HashMap<>();
        
        for (Object[] result : results) {
            String category = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            salesMap.put(category, amount);
        }
        
        return salesMap;
    }

    /**
     * Convert Bill entity to DTO
     */
    private EnhancedBillDTO toBillDTO(Bill bill, List<BillItem> items) {
        EnhancedBillDTO dto = new EnhancedBillDTO();
        dto.setBillId(bill.getId());
        dto.setRestaurantId(bill.getRestaurantId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setBillTime(bill.getBillTime());
        dto.setOrderId(bill.getOrderId());
        dto.setTableNumber(bill.getTableNumber());
        dto.setCustomerId(bill.getCustomerId());
        dto.setSubtotal(bill.getSubtotal());
        dto.setTaxType(bill.getTaxType());
        dto.setCgstRate(bill.getCgstRate());
        dto.setCgstAmount(bill.getCgstAmount());
        dto.setSgstRate(bill.getSgstRate());
        dto.setSgstAmount(bill.getSgstAmount());
        dto.setIgstRate(bill.getIgstRate());
        dto.setIgstAmount(bill.getIgstAmount());
        dto.setTotalTax(bill.getTotalTax());
        dto.setServiceChargeAmount(bill.getServiceChargeAmount());
        dto.setPackagingCharges(bill.getPackagingCharges());
        dto.setDeliveryCharges(bill.getDeliveryCharges());
        dto.setDiscountRate(bill.getDiscountRate());
        dto.setDiscountAmount(bill.getDiscountAmount());
        dto.setRoundOffAmount(bill.getRoundOffAmount());
        dto.setTotalBeforeRoundOff(bill.getTotalBeforeRoundOff());
        dto.setGrandTotal(bill.getGrandTotal());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setIsPaid(bill.getIsPaid());
        dto.setIsCancelled(bill.getIsCancelled());
        dto.setCancellationReason(bill.getCancellationReason());

        // Convert BillItem entities to BillItemDTOs
        List<EnhancedBillDTO.BillItemDTO> billItemDTOs = items.stream()
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
        dto.setItems(billItemDTOs);

        return dto;
    }
}

// Made with Bob
