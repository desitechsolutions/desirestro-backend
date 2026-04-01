package com.dts.restro.reports.services;

import com.dts.restro.billing.entity.Bill;
import com.dts.restro.billing.entity.BillItem;
import com.dts.restro.billing.enums.BillStatus;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.repository.BillItemRepository;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.billing.service.DailySalesSummaryService;
import com.dts.restro.customer.entity.Customer;
import com.dts.restro.customer.repository.CustomerRepository;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.reports.dto.*;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final DailySalesSummaryService dailySalesSummaryService;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    
    // ==================== REPORT GENERATION METHODS ====================
    
    /**
     * Generate Daily Sales Report
     */
    @Transactional(readOnly = true)
    public SalesReportDTO generateDailySalesReport(Long restaurantId, LocalDate date) {
        log.info("Generating daily sales report for restaurant {} on {}", restaurantId, date);
        
        // Get restaurant info
        Restaurant restaurant = getRestaurant(restaurantId);
        
        // Get daily summary
        var summary = dailySalesSummaryService.getSummaryByDate(restaurantId, date);
        
        // Build metadata
        ReportDTO metadata = buildMetadata(restaurant, "DAILY_SALES", 
            "Daily Sales Report", date, date, "DAILY");
        
        // Get bills for the day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(
            restaurantId, startOfDay, endOfDay);
        
        // Get top items
        List<SalesReportDTO.TopItemDTO> topItems = getTopSellingItems(restaurantId, date, date, 10);
        
        // Get hourly sales
        List<SalesReportDTO.HourlySalesDTO> hourlySales = getHourlySales(bills);
        
        // Get top customers
        List<SalesReportDTO.TopCustomerDTO> topCustomers = getTopCustomers(restaurantId, date, date, 10);
        
        // Build report
        SalesReportDTO report = SalesReportDTO.builder()
            .metadata(metadata)
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
            .topSellingItems(topItems)
            .hourlySales(hourlySales)
            .topCustomers(topCustomers)
            .build();
        
        report.calculateCollectionEfficiency();
        report.calculateAverageBillValue();
        report.calculateAverageItemsPerBill();
        
        return report;
    }
    
    /**
     * Generate Monthly Sales Report
     */
    @Transactional(readOnly = true)
    public SalesReportDTO generateMonthlySalesReport(Long restaurantId, int month, int year) {
        log.info("Generating monthly sales report for restaurant {} - {}/{}", restaurantId, month, year);
        
        Restaurant restaurant = getRestaurant(restaurantId);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // Get monthly summaries
        var summaries = dailySalesSummaryService.getMonthlySummaries(restaurantId, month, year);
        
        // Aggregate data
        SalesReportDTO report = aggregateSummaries(summaries, restaurant, startDate, endDate, "MONTHLY");
        
        // Add top performers
        report.setTopSellingItems(getTopSellingItems(restaurantId, startDate, endDate, 10));
        report.setTopCustomers(getTopCustomers(restaurantId, startDate, endDate, 10));
        
        // Add comparison with previous month
        if (month > 1) {
            var previousMonth = dailySalesSummaryService.getMonthlySummaries(restaurantId, month - 1, year);
            report.setComparison(buildComparison(previousMonth, summaries, "Previous Month"));
        }
        
        return report;
    }
    
    /**
     * Generate Item-wise Sales Report
     */
    @Transactional(readOnly = true)
    public ItemSalesReportDTO generateItemWiseSalesReport(Long restaurantId, 
                                                          LocalDate startDate, LocalDate endDate) {
        log.info("Generating item-wise sales report for restaurant {} from {} to {}", 
            restaurantId, startDate, endDate);
        
        Restaurant restaurant = getRestaurant(restaurantId);
        
        // Build metadata
        ReportDTO metadata = buildMetadata(restaurant, "ITEM_SALES", 
            "Item-wise Sales Report", startDate, endDate, "CUSTOM");
        
        // Get all bill items for the period
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(restaurantId, start, end)
            .stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        // Get all bill items
        List<BillItem> allItems = bills.stream()
            .flatMap(b -> billItemRepository.findByBillId(b.getId()).stream())
            .collect(Collectors.toList());
        
        // Group by menu item
        Map<Long, List<BillItem>> itemGroups = allItems.stream()
            .collect(Collectors.groupingBy(BillItem::getMenuItemId));
        
        // Calculate item sales details
        List<ItemSalesReportDTO.ItemSalesDetail> itemDetails = itemGroups.entrySet().stream()
            .map(entry -> {
                Long menuItemId = entry.getKey();
                List<BillItem> items = entry.getValue();
                
                int totalQty = items.stream().mapToInt(BillItem::getQuantity).sum();
                BigDecimal totalRev = items.stream()
                    .map(BillItem::getItemTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BillItem sample = items.get(0);
                
                return ItemSalesReportDTO.ItemSalesDetail.builder()
                    .menuItemId(menuItemId)
                    .itemName(sample.getItemName())
                    .category(sample.getCategory())
                    .hsnCode(sample.getHsnCode())
                    .quantitySold(totalQty)
                    .unitPrice(sample.getPrice())
                    .totalRevenue(totalRev)
                    .averageSellingPrice(totalRev.divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP))
                    .build();
            })
            .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
            .collect(Collectors.toList());
        
        // Calculate percentages and ranks
        BigDecimal totalRevenue = itemDetails.stream()
            .map(ItemSalesReportDTO.ItemSalesDetail::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int rank = 1;
        for (var item : itemDetails) {
            item.setPercentageOfTotalRevenue(
                item.getTotalRevenue().divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue()
            );
            item.setRank(rank++);
        }
        
        // Build category summary
        Map<String, ItemSalesReportDTO.CategorySummary> categorySummary = buildCategorySummary(itemDetails);
        
        // Build report
        return ItemSalesReportDTO.builder()
            .metadata(metadata)
            .totalItemsSold(itemDetails.stream().mapToInt(ItemSalesReportDTO.ItemSalesDetail::getQuantitySold).sum())
            .uniqueItemsCount(itemDetails.size())
            .totalRevenue(totalRevenue)
            .items(itemDetails)
            .categorySummary(categorySummary)
            .topSellingItems(itemDetails.stream().limit(10).collect(Collectors.toList()))
            .slowMovingItems(itemDetails.stream().skip(Math.max(0, itemDetails.size() - 10)).collect(Collectors.toList()))
            .build();
    }
    
    /**
     * Generate GST Report (GSTR-1 Format)
     */
    @Transactional(readOnly = true)
    public GSTReportDTO generateGSTReport(Long restaurantId, int month, int year) {
        log.info("Generating GST report for restaurant {} - {}/{}", restaurantId, month, year);
        
        Restaurant restaurant = getRestaurant(restaurantId);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        // Get all paid bills for the month
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(restaurantId, start, end)
            .stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        // Separate B2B and B2C
        List<GSTReportDTO.B2BInvoice> b2bInvoices = new ArrayList<>();
        List<GSTReportDTO.B2CInvoice> b2cInvoices = new ArrayList<>();
        
        for (Bill bill : bills) {
            if (bill.getCustomerId() != null) {
                Customer customer = customerRepository.findById(bill.getCustomerId()).orElse(null);
                if (customer != null && customer.getGstin() != null && !customer.getGstin().isEmpty()) {
                    // B2B Invoice
                    b2bInvoices.add(GSTReportDTO.B2BInvoice.builder()
                        .customerGSTIN(customer.getGstin())
                        .customerName(customer.getName())
                        .customerState(customer.getState())
                        .invoiceNumber(bill.getBillNumber())
                        .invoiceDate(bill.getBillDate().toLocalDate())
                        .invoiceType("Regular")
                        .taxableValue(bill.getSubtotal())
                        .cgstAmount(bill.getCgstAmount())
                        .sgstAmount(bill.getSgstAmount())
                        .igstAmount(bill.getIgstAmount())
                        .totalTax(bill.getTotalTax())
                        .invoiceValue(bill.getFinalAmount())
                        .placeOfSupply(customer.getState())
                        .isReverseCharge(false)
                        .build());
                    continue;
                }
            }
            
            // B2C Invoice
            b2cInvoices.add(GSTReportDTO.B2CInvoice.builder()
                .invoiceNumber(bill.getBillNumber())
                .invoiceDate(bill.getBillDate().toLocalDate())
                .invoiceType("Regular")
                .taxableValue(bill.getSubtotal())
                .cgstAmount(bill.getCgstAmount())
                .sgstAmount(bill.getSgstAmount())
                .totalTax(bill.getTotalTax())
                .invoiceValue(bill.getFinalAmount())
                .placeOfSupply(restaurant.getState())
                .build());
        }
        
        // Calculate totals
        BigDecimal totalTaxableValue = bills.stream()
            .map(Bill::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCGST = bills.stream()
            .map(Bill::getCgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSGST = bills.stream()
            .map(Bill::getSgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalIGST = bills.stream()
            .map(Bill::getIgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Build metadata
        ReportDTO metadata = buildMetadata(restaurant, "GST_REPORT", 
            "GST Report (GSTR-1)", startDate, endDate, "MONTHLY");
        
        // Build report
        GSTReportDTO report = GSTReportDTO.builder()
            .metadata(metadata)
            .gstPeriod(String.format("%02d-%d", month, year))
            .month(month)
            .year(year)
            .gstin(restaurant.getGstin())
            .legalName(restaurant.getName())
            .tradeName(restaurant.getName())
            .address(restaurant.getAddress())
            .stateCode(restaurant.getStateCode())
            .b2bInvoices(b2bInvoices)
            .b2cInvoices(b2cInvoices)
            .totalTaxableValue(totalTaxableValue)
            .totalCGST(totalCGST)
            .totalSGST(totalSGST)
            .totalIGST(totalIGST)
            .totalTax(totalCGST.add(totalSGST).add(totalIGST))
            .totalB2BInvoices(b2bInvoices.size())
            .totalB2CInvoices(b2cInvoices.size())
            .totalInvoices(bills.size())
            .build();
        
        report.calculateTaxLiability();
        
        return report;
    }
    
    // ==================== HELPER METHODS ====================
    
    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }
    
    private ReportDTO buildMetadata(Restaurant restaurant, String type, String title,
                                    LocalDate startDate, LocalDate endDate, String period) {
        return ReportDTO.builder()
            .reportType(type)
            .reportTitle(title)
            .generatedAt(LocalDateTime.now())
            .restaurantId(restaurant.getId())
            .restaurantName(restaurant.getName())
            .restaurantAddress(restaurant.getAddress())
            .restaurantGSTIN(restaurant.getGstin())
            .restaurantPhone(restaurant.getPhone())
            .startDate(startDate)
            .endDate(endDate)
            .period(period)
            .build();
    }
    
    private List<SalesReportDTO.TopItemDTO> getTopSellingItems(Long restaurantId,
                                                                LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(restaurantId, start, end)
            .stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        List<BillItem> allItems = bills.stream()
            .flatMap(b -> billItemRepository.findByBillId(b.getId()).stream())
            .collect(Collectors.toList());
        
        Map<Long, List<BillItem>> itemGroups = allItems.stream()
            .collect(Collectors.groupingBy(BillItem::getMenuItemId));
        
        return itemGroups.entrySet().stream()
            .map(entry -> {
                List<BillItem> items = entry.getValue();
                int totalQty = items.stream().mapToInt(BillItem::getQuantity).sum();
                BigDecimal totalRev = items.stream()
                    .map(BillItem::getItemTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BillItem sample = items.get(0);
                
                return SalesReportDTO.TopItemDTO.builder()
                    .itemName(sample.getItemName())
                    .category(sample.getCategory())
                    .quantitySold(totalQty)
                    .revenue(totalRev)
                    .build();
            })
            .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private List<SalesReportDTO.HourlySalesDTO> getHourlySales(List<Bill> bills) {
        Map<Integer, List<Bill>> hourlyGroups = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.groupingBy(b -> b.getBillDate().getHour()));
        
        return hourlyGroups.entrySet().stream()
            .map(entry -> {
                int hour = entry.getKey();
                List<Bill> hourBills = entry.getValue();
                
                BigDecimal revenue = hourBills.stream()
                    .map(Bill::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return SalesReportDTO.HourlySalesDTO.builder()
                    .hour(hour)
                    .timeSlot(String.format("%02d:00 - %02d:00", hour, hour + 1))
                    .billCount(hourBills.size())
                    .revenue(revenue)
                    .averageBillValue(revenue.divide(BigDecimal.valueOf(hourBills.size()), 2, RoundingMode.HALF_UP))
                    .build();
            })
            .sorted(Comparator.comparing(SalesReportDTO.HourlySalesDTO::getHour))
            .collect(Collectors.toList());
    }
    
    private List<SalesReportDTO.TopCustomerDTO> getTopCustomers(Long restaurantId,
                                                                 LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(restaurantId, start, end)
            .stream()
            .filter(b -> b.getStatus() == BillStatus.PAID && b.getCustomerId() != null)
            .collect(Collectors.toList());
        
        Map<Long, List<Bill>> customerGroups = bills.stream()
            .collect(Collectors.groupingBy(Bill::getCustomerId));
        
        return customerGroups.entrySet().stream()
            .map(entry -> {
                Long customerId = entry.getKey();
                List<Bill> customerBills = entry.getValue();
                
                Customer customer = customerRepository.findById(customerId).orElse(null);
                if (customer == null) return null;
                
                BigDecimal totalSpent = customerBills.stream()
                    .map(Bill::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return SalesReportDTO.TopCustomerDTO.builder()
                    .customerId(customerId)
                    .customerName(customer.getName())
                    .customerPhone(customer.getPhone())
                    .totalOrders(customerBills.size())
                    .totalSpent(totalSpent)
                    .averageOrderValue(totalSpent.divide(BigDecimal.valueOf(customerBills.size()), 2, RoundingMode.HALF_UP))
                    .build();
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private SalesReportDTO aggregateSummaries(List<com.dts.restro.billing.dto.DailySalesSummaryDTO> summaries,
                                              Restaurant restaurant, LocalDate startDate, LocalDate endDate, String period) {
        ReportDTO metadata = buildMetadata(restaurant, "SALES_REPORT",
            "Sales Report", startDate, endDate, period);
        
        int totalBills = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalBills).sum();
        int paidBills = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getPaidBills).sum();
        int pendingBills = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getPendingBills).sum();
        int cancelledBills = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getCancelledBills).sum();
        
        BigDecimal totalRevenue = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal subtotalAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getSubtotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discountAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal serviceChargeAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getServiceChargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal packagingChargeAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getPackagingChargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal deliveryChargeAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getDeliveryChargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal cgstAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getCgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal sgstAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getSgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal igstAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getIgstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalTaxAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal cashAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getCashAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal cardAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getCardAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal upiAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getUpiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netBankingAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getNetBankingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal creditAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getCreditAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int uniqueCustomers = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getUniqueCustomers).sum();
        int newCustomers = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getNewCustomers).sum();
        int totalItemsSold = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalItemsSold).sum();
        int uniqueItemsSold = summaries.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getUniqueItemsSold).sum();
        
        BigDecimal roundOffAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getRoundOffAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal pendingAmount = summaries.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getPendingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        SalesReportDTO report = SalesReportDTO.builder()
            .metadata(metadata)
            .totalBills(totalBills)
            .paidBills(paidBills)
            .pendingBills(pendingBills)
            .cancelledBills(cancelledBills)
            .totalRevenue(totalRevenue)
            .subtotalAmount(subtotalAmount)
            .discountAmount(discountAmount)
            .serviceChargeAmount(serviceChargeAmount)
            .packagingChargeAmount(packagingChargeAmount)
            .deliveryChargeAmount(deliveryChargeAmount)
            .cgstAmount(cgstAmount)
            .sgstAmount(sgstAmount)
            .igstAmount(igstAmount)
            .totalTaxAmount(totalTaxAmount)
            .cashAmount(cashAmount)
            .cardAmount(cardAmount)
            .upiAmount(upiAmount)
            .netBankingAmount(netBankingAmount)
            .creditAmount(creditAmount)
            .uniqueCustomers(uniqueCustomers)
            .newCustomers(newCustomers)
            .totalItemsSold(totalItemsSold)
            .uniqueItemsSold(uniqueItemsSold)
            .roundOffAmount(roundOffAmount)
            .pendingAmount(pendingAmount)
            .build();
        
        report.calculateCollectionEfficiency();
        report.calculateAverageBillValue();
        report.calculateAverageItemsPerBill();
        
        return report;
    }
    
    private SalesReportDTO.ComparisonDTO buildComparison(
            List<com.dts.restro.billing.dto.DailySalesSummaryDTO> previous,
            List<com.dts.restro.billing.dto.DailySalesSummaryDTO> current,
            String comparisonPeriod) {
        
        BigDecimal prevRevenue = previous.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal currRevenue = current.stream()
            .map(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int prevBills = previous.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalBills).sum();
        int currBills = current.stream().mapToInt(com.dts.restro.billing.dto.DailySalesSummaryDTO::getTotalBills).sum();
        
        double revenueGrowth = prevRevenue.compareTo(BigDecimal.ZERO) > 0
            ? currRevenue.subtract(prevRevenue).divide(prevRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;
        
        double billGrowth = prevBills > 0
            ? ((double)(currBills - prevBills) / prevBills) * 100
            : 0.0;
        
        return SalesReportDTO.ComparisonDTO.builder()
            .comparisonPeriod(comparisonPeriod)
            .previousRevenue(prevRevenue)
            .currentRevenue(currRevenue)
            .revenueGrowth(revenueGrowth)
            .previousBills(prevBills)
            .currentBills(currBills)
            .billGrowth(billGrowth)
            .build();
    }
    
    private Map<String, ItemSalesReportDTO.CategorySummary> buildCategorySummary(
            List<ItemSalesReportDTO.ItemSalesDetail> items) {
        return items.stream()
            .collect(Collectors.groupingBy(ItemSalesReportDTO.ItemSalesDetail::getCategory))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    List<ItemSalesReportDTO.ItemSalesDetail> categoryItems = entry.getValue();
                    BigDecimal categoryRevenue = categoryItems.stream()
                        .map(ItemSalesReportDTO.ItemSalesDetail::getTotalRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return ItemSalesReportDTO.CategorySummary.builder()
                        .categoryName(entry.getKey())
                        .itemCount(categoryItems.size())
                        .totalQuantitySold(categoryItems.stream()
                            .mapToInt(ItemSalesReportDTO.ItemSalesDetail::getQuantitySold).sum())
                        .totalRevenue(categoryRevenue)
                        .build();
                }
            ));
    
    // ==================== EXPORT METHODS ====================
    
    /**
     * Export report to PDF
     */
    public byte[] exportReportToPDF(SalesReportDTO report) {
        log.info("Exporting report to PDF: {}", report.getMetadata().getReportTitle());
        
        try {
            // TODO: Implement PDF generation using iText
            // This is a placeholder implementation
            // In production, use iText library to generate proper PDF
            
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("=".repeat(80)).append("\n");
            pdfContent.append(report.getMetadata().getReportTitle()).append("\n");
            pdfContent.append("=".repeat(80)).append("\n\n");
            
            pdfContent.append("Restaurant: ").append(report.getMetadata().getRestaurantName()).append("\n");
            pdfContent.append("Period: ").append(report.getMetadata().getStartDate())
                .append(" to ").append(report.getMetadata().getEndDate()).append("\n");
            pdfContent.append("Generated: ").append(report.getMetadata().getGeneratedAt()).append("\n\n");
            
            pdfContent.append("-".repeat(80)).append("\n");
            pdfContent.append("SUMMARY\n");
            pdfContent.append("-".repeat(80)).append("\n");
            pdfContent.append(String.format("Total Bills: %d\n", report.getTotalBills()));
            pdfContent.append(String.format("Total Revenue: ₹%.2f\n", report.getTotalRevenue()));
            pdfContent.append(String.format("Average Bill Value: ₹%.2f\n", report.getAverageBillValue()));
            pdfContent.append(String.format("Total Tax: ₹%.2f\n", report.getTotalTaxAmount()));
            
            // Return as bytes (in production, use iText to generate proper PDF)
            return pdfContent.toString().getBytes();
            
        } catch (Exception e) {
            log.error("Failed to export report to PDF", e);
            throw new RuntimeException("Failed to export report to PDF", e);
        }
    }
    
    /**
     * Export report to Excel
     */
    public byte[] exportReportToExcel(SalesReportDTO report) {
        log.info("Exporting report to Excel: {}", report.getMetadata().getReportTitle());
        
        try {
            // TODO: Implement Excel generation using Apache POI
            // This is a placeholder implementation
            // In production, use Apache POI library to generate proper Excel file
            
            StringBuilder excelContent = new StringBuilder();
            excelContent.append("Report Title,").append(report.getMetadata().getReportTitle()).append("\n");
            excelContent.append("Restaurant,").append(report.getMetadata().getRestaurantName()).append("\n");
            pdfContent.append("Period,").append(report.getMetadata().getStartDate())
                .append(" to ").append(report.getMetadata().getEndDate()).append("\n");
            excelContent.append("\n");
            
            excelContent.append("Metric,Value\n");
            excelContent.append("Total Bills,").append(report.getTotalBills()).append("\n");
            excelContent.append("Paid Bills,").append(report.getPaidBills()).append("\n");
            excelContent.append("Pending Bills,").append(report.getPendingBills()).append("\n");
            excelContent.append("Total Revenue,").append(report.getTotalRevenue()).append("\n");
            excelContent.append("Average Bill Value,").append(report.getAverageBillValue()).append("\n");
            excelContent.append("Total Tax,").append(report.getTotalTaxAmount()).append("\n");
            
            // Return as CSV bytes (in production, use Apache POI to generate proper Excel)
            return excelContent.toString().getBytes();
            
        } catch (Exception e) {
            log.error("Failed to export report to Excel", e);
            throw new RuntimeException("Failed to export report to Excel", e);
        }
    }
    
    /**
     * Export GST report to PDF
     */
    public byte[] exportGSTReportToPDF(GSTReportDTO report) {
        log.info("Exporting GST report to PDF for period: {}", report.getGstPeriod());
        
        try {
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("=".repeat(80)).append("\n");
            pdfContent.append("GST REPORT (GSTR-1 FORMAT)\n");
            pdfContent.append("=".repeat(80)).append("\n\n");
            
            pdfContent.append("GSTIN: ").append(report.getGstin()).append("\n");
            pdfContent.append("Legal Name: ").append(report.getLegalName()).append("\n");
            pdfContent.append("Period: ").append(report.getGstPeriod()).append("\n\n");
            
            pdfContent.append("-".repeat(80)).append("\n");
            pdfContent.append("SUMMARY\n");
            pdfContent.append("-".repeat(80)).append("\n");
            pdfContent.append(String.format("Total Invoices: %d\n", report.getTotalInvoices()));
            pdfContent.append(String.format("B2B Invoices: %d\n", report.getTotalB2BInvoices()));
            pdfContent.append(String.format("B2C Invoices: %d\n", report.getTotalB2CInvoices()));
            pdfContent.append(String.format("Total Taxable Value: ₹%.2f\n", report.getTotalTaxableValue()));
            pdfContent.append(String.format("Total CGST: ₹%.2f\n", report.getTotalCGST()));
            pdfContent.append(String.format("Total SGST: ₹%.2f\n", report.getTotalSGST()));
            pdfContent.append(String.format("Total IGST: ₹%.2f\n", report.getTotalIGST()));
            pdfContent.append(String.format("Total Tax: ₹%.2f\n", report.getTotalTax()));
            
            return pdfContent.toString().getBytes();
            
        } catch (Exception e) {
            log.error("Failed to export GST report to PDF", e);
            throw new RuntimeException("Failed to export GST report to PDF", e);
        }
    }
    }
}