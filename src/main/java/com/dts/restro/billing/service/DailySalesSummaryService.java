package com.dts.restro.billing.service;

import com.dts.restro.billing.dto.DailySalesSummaryDTO;
import com.dts.restro.billing.entity.Bill;
import com.dts.restro.billing.entity.BillItem;
import com.dts.restro.billing.entity.DailySalesSummary;
import com.dts.restro.billing.enums.BillStatus;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.repository.BillItemRepository;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.billing.repository.DailySalesSummaryRepository;
import com.dts.restro.customer.entity.Customer;
import com.dts.restro.customer.repository.CustomerRepository;
import com.dts.restro.exception.BusinessValidationException;
import com.dts.restro.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing daily sales summaries
 * Automatically generates and updates summaries based on bill operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailySalesSummaryService {
    
    private final DailySalesSummaryRepository summaryRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * Generate or update daily sales summary for a specific date
     * This method aggregates all bills for the given date and restaurant
     */
    @Transactional
    public DailySalesSummaryDTO generateDailySummary(Long restaurantId, LocalDate date) {
        log.info("Generating daily sales summary for restaurant {} on {}", restaurantId, date);
        
        // Validate restaurant ID
        if (restaurantId == null || restaurantId <= 0) {
            throw new BusinessValidationException("Invalid restaurant ID");
        }
        
        // Get all bills for the date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Bill> bills = billRepository.findByRestaurantId(restaurantId).stream()
            .filter(b -> b.getBillTime() != null
                && !b.getBillTime().isBefore(startOfDay)
                && b.getBillTime().isBefore(endOfDay))
            .collect(Collectors.toList());
        
        // Find or create summary
        DailySalesSummary summary = summaryRepository
            .findByRestaurantIdAndSalesDate(restaurantId, date)
            .orElse(DailySalesSummary.builder()
                .restaurantId(restaurantId)
                .salesDate(date)
                .build());
        
        // Calculate all metrics
        calculateBillCounts(summary, bills);
        calculateRevenue(summary, bills);
        calculateTaxes(summary, bills);
        calculatePaymentMethods(summary, bills);
        calculateCustomerMetrics(summary, bills, restaurantId, date);
        calculateItemMetrics(summary, bills);
        
        // Save and return
        summary = summaryRepository.save(summary);
        log.info("Daily sales summary generated successfully for restaurant {} on {}", restaurantId, date);
        
        return convertToDTO(summary);
    }
    
    /**
     * Update summary when a bill is paid
     * This is called automatically from BillingService
     */
    @Transactional
    public void updateSummaryOnBillPayment(Bill bill) {
        log.info("Updating daily summary for bill payment: {}", bill.getBillNumber());
        
        LocalDate billDate = bill.getBillTime().toLocalDate();
        generateDailySummary(bill.getRestaurantId(), billDate);
    }

    /**
     * Update summary when a bill is cancelled
     */
    @Transactional
    public void updateSummaryOnBillCancellation(Bill bill) {
        log.info("Updating daily summary for bill cancellation: {}", bill.getBillNumber());

        LocalDate billDate = bill.getBillTime().toLocalDate();
        generateDailySummary(bill.getRestaurantId(), billDate);
    }
    
    /**
     * Get summary for a specific date
     */
    @Transactional(readOnly = true)
    public DailySalesSummaryDTO getSummaryByDate(Long restaurantId, LocalDate date) {
        log.info("Fetching daily summary for restaurant {} on {}", restaurantId, date);
        
        DailySalesSummary summary = summaryRepository
            .findByRestaurantIdAndSalesDate(restaurantId, date)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Daily sales summary not found for date: " + date
            ));
        
        return convertToDTO(summary);
    }
    
    /**
     * Get summaries for a month
     */
    @Transactional(readOnly = true)
    public List<DailySalesSummaryDTO> getMonthlySummaries(Long restaurantId, int month, int year) {
        log.info("Fetching monthly summaries for restaurant {} - {}/{}", restaurantId, month, year);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<DailySalesSummary> summaries = summaryRepository
            .findByRestaurantIdAndSalesDateBetween(restaurantId, startDate, endDate);

        return summaries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get summaries for a date range
     */
    @Transactional(readOnly = true)
    public List<DailySalesSummaryDTO> getSummariesByDateRange(
        Long restaurantId, LocalDate startDate, LocalDate endDate
    ) {
        log.info("Fetching summaries for restaurant {} from {} to {}",
            restaurantId, startDate, endDate);

        List<DailySalesSummary> summaries = summaryRepository
            .findByRestaurantIdAndSalesDateBetween(restaurantId, startDate, endDate);

        return summaries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Regenerate summary for a specific date
     * Useful for fixing data inconsistencies
     */
    @Transactional
    public DailySalesSummaryDTO regenerateSummary(Long restaurantId, LocalDate date) {
        log.info("Regenerating daily summary for restaurant {} on {}", restaurantId, date);
        
        // Delete existing summary if present
        summaryRepository.findByRestaurantIdAndSalesDate(restaurantId, date)
            .ifPresent(summaryRepository::delete);
        
        // Generate fresh summary
        return generateDailySummary(restaurantId, date);
    }
    
    // ==================== Private Helper Methods ====================
    
    private void calculateBillCounts(DailySalesSummary summary, List<Bill> bills) {
        summary.setTotalBills(bills.size());
        summary.setPaidBills((int) bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .count());
        summary.setPendingBills((int) bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PENDING)
            .count());
        summary.setCancelledBills((int) bills.stream()
            .filter(b -> b.getStatus() == BillStatus.CANCELLED)
            .count());
    }
    
    private void calculateRevenue(DailySalesSummary summary, List<Bill> bills) {
        List<Bill> paidBills = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        summary.setTotalRevenue(paidBills.stream()
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setSubtotalAmount(paidBills.stream()
            .map(Bill::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setDiscountAmount(paidBills.stream()
            .map(Bill::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setServiceChargeAmount(paidBills.stream()
            .map(Bill::getServiceChargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setPackagingChargeAmount(paidBills.stream()
            .map(Bill::getPackagingCharges)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setDeliveryChargeAmount(paidBills.stream()
            .map(Bill::getDeliveryCharges)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setRoundOffAmount(paidBills.stream()
            .map(Bill::getRoundOffAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // Calculate pending amount
        summary.setPendingAmount(bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PENDING)
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    
    private void calculateTaxes(DailySalesSummary summary, List<Bill> bills) {
        List<Bill> paidBills = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        summary.setCgstAmount(paidBills.stream()
            .map(Bill::getCgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setSgstAmount(paidBills.stream()
            .map(Bill::getSgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setIgstAmount(paidBills.stream()
            .map(Bill::getIgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setTotalTaxAmount(paidBills.stream()
            .map(Bill::getTotalTax)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    
    private void calculatePaymentMethods(DailySalesSummary summary, List<Bill> bills) {
        List<Bill> paidBills = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        summary.setCashAmount(paidBills.stream()
            .filter(b -> b.getPaymentMethod() == PaymentMethod.CASH)
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setCardAmount(paidBills.stream()
            .filter(b -> b.getPaymentMethod() == PaymentMethod.CARD)
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setUpiAmount(paidBills.stream()
            .filter(b -> b.getPaymentMethod() == PaymentMethod.UPI)
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setNetBankingAmount(paidBills.stream()
            .filter(b -> b.getPaymentMethod() == PaymentMethod.NET_BANKING)
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        summary.setCreditAmount(paidBills.stream()
            .filter(b -> b.getPaymentMethod() == PaymentMethod.CREDIT)
            .map(Bill::getGrandTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    
    private void calculateCustomerMetrics(
        DailySalesSummary summary, List<Bill> bills, Long restaurantId, LocalDate date
    ) {
        // Count unique customers
        Set<Long> uniqueCustomerIds = bills.stream()
            .map(Bill::getCustomerId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        summary.setUniqueCustomers(uniqueCustomerIds.size());
        
        // Count new customers registered today
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        long newCustomers = customerRepository.countByRestaurantIdAndCreatedAtBetween(
            restaurantId, startOfDay, endOfDay
        );
        summary.setNewCustomers((int) newCustomers);
        
        // Calculate average bill value
        List<Bill> paidBills = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        if (!paidBills.isEmpty()) {
            BigDecimal totalRevenue = summary.getTotalRevenue();
            BigDecimal avgBillValue = totalRevenue.divide(
                BigDecimal.valueOf(paidBills.size()),
                2,
                BigDecimal.ROUND_HALF_UP
            );
            summary.setAverageBillValue(avgBillValue);
        } else {
            summary.setAverageBillValue(BigDecimal.ZERO);
        }
    }
    
    private void calculateItemMetrics(DailySalesSummary summary, List<Bill> bills) {
        List<Bill> paidBills = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        // Total items sold (sum of quantities)
        int totalItems = paidBills.stream()
            .flatMap(b -> billItemRepository.findByBillId(b.getId()).stream())
            .mapToInt(BillItem::getQuantity)
            .sum();
        summary.setTotalItemsSold(totalItems);

        // Unique items sold (distinct menu items)
        Set<Long> uniqueMenuItems = paidBills.stream()
            .flatMap(b -> billItemRepository.findByBillId(b.getId()).stream())
            .map(BillItem::getMenuItemId)
            .collect(Collectors.toSet());
        summary.setUniqueItemsSold(uniqueMenuItems.size());
    }
    
    private DailySalesSummaryDTO convertToDTO(DailySalesSummary summary) {
        DailySalesSummaryDTO dto = DailySalesSummaryDTO.builder()
            .id(summary.getId())
            .restaurantId(summary.getRestaurantId())
            .salesDate(summary.getSalesDate())
            .totalBills(summary.getTotalBills())
            .paidBills(summary.getPaidBills())
            .pendingBills(summary.getPendingBills())
            .cancelledBills(summary.getCancelledBills())
            .totalRevenue(summary.getTotalRevenue())
            .subtotalAmount(summary.getSubtotalAmount())
            .discountAmount(summary.getDiscountAmount())
            .serviceChargeAmount(summary.getServiceChargeAmount())
            .packagingChargeAmount(summary.getPackagingChargeAmount())
            .deliveryChargeAmount(summary.getDeliveryChargeAmount())
            .cgstAmount(summary.getCgstAmount())
            .sgstAmount(summary.getSgstAmount())
            .igstAmount(summary.getIgstAmount())
            .totalTaxAmount(summary.getTotalTaxAmount())
            .cashAmount(summary.getCashAmount())
            .cardAmount(summary.getCardAmount())
            .upiAmount(summary.getUpiAmount())
            .netBankingAmount(summary.getNetBankingAmount())
            .creditAmount(summary.getCreditAmount())
            .uniqueCustomers(summary.getUniqueCustomers())
            .newCustomers(summary.getNewCustomers())
            .averageBillValue(summary.getAverageBillValue())
            .totalItemsSold(summary.getTotalItemsSold())
            .uniqueItemsSold(summary.getUniqueItemsSold())
            .roundOffAmount(summary.getRoundOffAmount())
            .pendingAmount(summary.getPendingAmount())
            .createdAt(summary.getCreatedAt() != null ? summary.getCreatedAt().toString() : null)
            .updatedAt(summary.getUpdatedAt() != null ? summary.getUpdatedAt().toString() : null)
            .build();
        
        // Initialize null fields and calculate derived values
        dto.initializeNullFields();
        dto.calculateTotalTax();
        dto.calculateAverageBillValue();
        
        return dto;
    }
}

// Made with Bob
