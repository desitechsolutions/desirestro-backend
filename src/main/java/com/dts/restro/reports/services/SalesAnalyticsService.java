package com.dts.restro.reports.services;

import com.dts.restro.billing.dto.DailySalesSummaryDTO;
import com.dts.restro.billing.entity.Bill;
import com.dts.restro.billing.enums.BillStatus;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.billing.service.DailySalesSummaryService;
import com.dts.restro.customer.entity.Customer;
import com.dts.restro.customer.repository.CustomerRepository;
import com.dts.restro.exception.BusinessValidationException;
import com.dts.restro.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesAnalyticsService {
    
    private final DailySalesSummaryService dailySalesSummaryService;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * Analyze sales trends over a period
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeSalesTrends(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing sales trends for restaurant {} from {} to {}", restaurantId, startDate, endDate);
        
        validateDateRange(startDate, endDate);
        
        List<DailySalesSummaryDTO> summaries = dailySalesSummaryService.getSummariesByDateRange(
            restaurantId, startDate, endDate);
        
        if (summaries.isEmpty()) {
            throw new ResourceNotFoundException("No sales data found for the specified period");
        }
        
        // Calculate trend metrics
        List<Map<String, Object>> dailyTrends = summaries.stream()
            .map(summary -> {
                Map<String, Object> trend = new HashMap<>();
                trend.put("date", summary.getDate());
                trend.put("revenue", summary.getTotalRevenue());
                trend.put("bills", summary.getTotalBills());
                trend.put("averageBillValue", summary.getAverageBillValue());
                trend.put("customers", summary.getUniqueCustomers());
                return trend;
            })
            .collect(Collectors.toList());
        
        // Calculate moving averages (7-day)
        List<BigDecimal> movingAverages = calculateMovingAverage(summaries, 7);
        
        // Identify peak days
        DailySalesSummaryDTO peakDay = summaries.stream()
            .max(Comparator.comparing(DailySalesSummaryDTO::getTotalRevenue))
            .orElse(null);
        
        // Identify lowest day
        DailySalesSummaryDTO lowestDay = summaries.stream()
            .min(Comparator.comparing(DailySalesSummaryDTO::getTotalRevenue))
            .orElse(null);
        
        // Calculate growth rate
        BigDecimal firstWeekAvg = summaries.stream()
            .limit(7)
            .map(DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.min(7, summaries.size())), 2, RoundingMode.HALF_UP);
        
        BigDecimal lastWeekAvg = summaries.stream()
            .skip(Math.max(0, summaries.size() - 7))
            .map(DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.min(7, summaries.size())), 2, RoundingMode.HALF_UP);
        
        double growthRate = firstWeekAvg.compareTo(BigDecimal.ZERO) > 0
            ? lastWeekAvg.subtract(firstWeekAvg).divide(firstWeekAvg, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;
        
        // Day of week analysis
        Map<DayOfWeek, BigDecimal> dayOfWeekRevenue = summaries.stream()
            .collect(Collectors.groupingBy(
                s -> s.getDate().getDayOfWeek(),
                Collectors.reducing(BigDecimal.ZERO, 
                    DailySalesSummaryDTO::getTotalRevenue, 
                    BigDecimal::add)
            ));
        
        DayOfWeek bestDay = dayOfWeekRevenue.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("dailyTrends", dailyTrends);
        result.put("movingAverages", movingAverages);
        result.put("peakDay", peakDay != null ? Map.of(
            "date", peakDay.getDate(),
            "revenue", peakDay.getTotalRevenue(),
            "bills", peakDay.getTotalBills()
        ) : null);
        result.put("lowestDay", lowestDay != null ? Map.of(
            "date", lowestDay.getDate(),
            "revenue", lowestDay.getTotalRevenue(),
            "bills", lowestDay.getTotalBills()
        ) : null);
        result.put("growthRate", growthRate);
        result.put("firstWeekAverage", firstWeekAvg);
        result.put("lastWeekAverage", lastWeekAvg);
        result.put("dayOfWeekRevenue", dayOfWeekRevenue);
        result.put("bestDayOfWeek", bestDay);
        
        return result;
    }
    
    /**
     * Analyze hourly sales pattern for a specific date
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeHourlySales(Long restaurantId, LocalDate date) {
        log.info("Analyzing hourly sales for restaurant {} on {}", restaurantId, date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(restaurantId, startOfDay, endOfDay)
            .stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .collect(Collectors.toList());
        
        if (bills.isEmpty()) {
            throw new ResourceNotFoundException("No sales data found for " + date);
        }
        
        // Group by hour
        Map<Integer, List<Bill>> hourlyGroups = bills.stream()
            .collect(Collectors.groupingBy(b -> b.getBillDate().getHour()));
        
        List<Map<String, Object>> hourlyData = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<Bill> hourBills = hourlyGroups.getOrDefault(hour, Collections.emptyList());
            
            BigDecimal revenue = hourBills.stream()
                .map(Bill::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", hour);
            hourData.put("timeSlot", String.format("%02d:00 - %02d:00", hour, hour + 1));
            hourData.put("billCount", hourBills.size());
            hourData.put("revenue", revenue);
            hourData.put("averageBillValue", hourBills.isEmpty() ? BigDecimal.ZERO :
                revenue.divide(BigDecimal.valueOf(hourBills.size()), 2, RoundingMode.HALF_UP));
            
            hourlyData.add(hourData);
        }
        
        // Find peak hours
        Map<String, Object> peakHour = hourlyData.stream()
            .max(Comparator.comparing(h -> (BigDecimal) h.get("revenue")))
            .orElse(null);
        
        // Calculate rush hours (hours with above-average sales)
        BigDecimal avgHourlyRevenue = bills.stream()
            .map(Bill::getFinalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
        
        List<Integer> rushHours = hourlyData.stream()
            .filter(h -> ((BigDecimal) h.get("revenue")).compareTo(avgHourlyRevenue) > 0)
            .map(h -> (Integer) h.get("hour"))
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("hourlyData", hourlyData);
        result.put("peakHour", peakHour);
        result.put("rushHours", rushHours);
        result.put("averageHourlyRevenue", avgHourlyRevenue);
        result.put("totalBills", bills.size());
        result.put("totalRevenue", bills.stream().map(Bill::getFinalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return result;
    }
    
    /**
     * Calculate performance metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calculatePerformanceMetrics(Long restaurantId, 
                                                           LocalDate startDate, LocalDate endDate) {
        log.info("Calculating performance metrics for restaurant {} from {} to {}", 
            restaurantId, startDate, endDate);
        
        validateDateRange(startDate, endDate);
        
        List<DailySalesSummaryDTO> summaries = dailySalesSummaryService.getSummariesByDateRange(
            restaurantId, startDate, endDate);
        
        if (summaries.isEmpty()) {
            throw new ResourceNotFoundException("No sales data found for the specified period");
        }
        
        // Calculate key metrics
        BigDecimal totalRevenue = summaries.stream()
            .map(DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalBills = summaries.stream()
            .mapToInt(DailySalesSummaryDTO::getTotalBills)
            .sum();
        
        int totalCustomers = summaries.stream()
            .mapToInt(DailySalesSummaryDTO::getUniqueCustomers)
            .sum();
        
        BigDecimal averageBillValue = totalBills > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalBills), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        BigDecimal averageDailyRevenue = totalRevenue.divide(
            BigDecimal.valueOf(summaries.size()), 2, RoundingMode.HALF_UP);
        
        // Collection efficiency
        BigDecimal totalPaidAmount = summaries.stream()
            .map(DailySalesSummaryDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPendingAmount = summaries.stream()
            .map(DailySalesSummaryDTO::getPendingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmount = totalPaidAmount.add(totalPendingAmount);
        double collectionEfficiency = totalAmount.compareTo(BigDecimal.ZERO) > 0
            ? totalPaidAmount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 100.0;
        
        // Customer metrics
        int newCustomers = summaries.stream()
            .mapToInt(DailySalesSummaryDTO::getNewCustomers)
            .sum();
        
        double customerRetentionRate = totalCustomers > 0
            ? ((double)(totalCustomers - newCustomers) / totalCustomers) * 100
            : 0.0;
        
        // Item metrics
        int totalItemsSold = summaries.stream()
            .mapToInt(DailySalesSummaryDTO::getTotalItemsSold)
            .sum();
        
        double averageItemsPerBill = totalBills > 0
            ? (double) totalItemsSold / totalBills
            : 0.0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("period", Map.of("startDate", startDate, "endDate", endDate));
        result.put("totalRevenue", totalRevenue);
        result.put("totalBills", totalBills);
        result.put("totalCustomers", totalCustomers);
        result.put("newCustomers", newCustomers);
        result.put("averageBillValue", averageBillValue);
        result.put("averageDailyRevenue", averageDailyRevenue);
        result.put("collectionEfficiency", collectionEfficiency);
        result.put("customerRetentionRate", customerRetentionRate);
        result.put("totalItemsSold", totalItemsSold);
        result.put("averageItemsPerBill", averageItemsPerBill);
        
        return result;
    }
    
    /**
     * Compare performance between two periods
     */
    @Transactional(readOnly = true)
    public Map<String, Object> comparePerformance(Long restaurantId,
                                                  LocalDate start1, LocalDate end1,
                                                  LocalDate start2, LocalDate end2) {
        log.info("Comparing performance for restaurant {} between periods", restaurantId);
        
        validateDateRange(start1, end1);
        validateDateRange(start2, end2);
        
        Map<String, Object> period1Metrics = calculatePerformanceMetrics(restaurantId, start1, end1);
        Map<String, Object> period2Metrics = calculatePerformanceMetrics(restaurantId, start2, end2);
        
        // Calculate differences
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("period1", period1Metrics);
        comparison.put("period2", period2Metrics);
        
        BigDecimal revenue1 = (BigDecimal) period1Metrics.get("totalRevenue");
        BigDecimal revenue2 = (BigDecimal) period2Metrics.get("totalRevenue");
        double revenueGrowth = revenue1.compareTo(BigDecimal.ZERO) > 0
            ? revenue2.subtract(revenue1).divide(revenue1, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;
        
        int bills1 = (Integer) period1Metrics.get("totalBills");
        int bills2 = (Integer) period2Metrics.get("totalBills");
        double billGrowth = bills1 > 0 ? ((double)(bills2 - bills1) / bills1) * 100 : 0.0;
        
        comparison.put("revenueGrowth", revenueGrowth);
        comparison.put("billGrowth", billGrowth);
        comparison.put("revenueDifference", revenue2.subtract(revenue1));
        comparison.put("billDifference", bills2 - bills1);
        
        return comparison;
    }
    
    /**
     * Forecast sales for upcoming days (simple linear regression)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> forecastSales(Long restaurantId, int daysAhead) {
        log.info("Forecasting sales for restaurant {} for next {} days", restaurantId, daysAhead);
        
        if (daysAhead < 1 || daysAhead > 30) {
            throw new BusinessValidationException("Days ahead must be between 1 and 30");
        }
        
        // Get last 30 days of data
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(29);
        
        List<DailySalesSummaryDTO> summaries = dailySalesSummaryService.getSummariesByDateRange(
            restaurantId, startDate, endDate);
        
        if (summaries.size() < 7) {
            throw new BusinessValidationException("Insufficient data for forecasting. Need at least 7 days of data.");
        }
        
        // Simple linear regression
        List<BigDecimal> revenues = summaries.stream()
            .map(DailySalesSummaryDTO::getTotalRevenue)
            .collect(Collectors.toList());
        
        double[] forecast = simpleLinearRegression(revenues, daysAhead);
        
        List<Map<String, Object>> forecastData = new ArrayList<>();
        LocalDate forecastDate = LocalDate.now();
        for (int i = 0; i < daysAhead; i++) {
            forecastDate = forecastDate.plusDays(1);
            Map<String, Object> dayForecast = new HashMap<>();
            dayForecast.put("date", forecastDate);
            dayForecast.put("forecastedRevenue", BigDecimal.valueOf(forecast[i]).setScale(2, RoundingMode.HALF_UP));
            dayForecast.put("confidence", "Medium"); // Simplified confidence level
            forecastData.add(dayForecast);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("forecastPeriod", daysAhead);
        result.put("basedOnDays", summaries.size());
        result.put("forecastData", forecastData);
        result.put("averageHistoricalRevenue", revenues.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(revenues.size()), 2, RoundingMode.HALF_UP));
        
        return result;
    }
    
    /**
     * Analyze customer retention
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeCustomerRetention(Long restaurantId, 
                                                        LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing customer retention for restaurant {} from {} to {}", 
            restaurantId, startDate, endDate);
        
        validateDateRange(startDate, endDate);
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        List<Bill> bills = billRepository.findByRestaurantIdAndBillDateBetween(restaurantId, start, end)
            .stream()
            .filter(b -> b.getStatus() == BillStatus.PAID && b.getCustomerId() != null)
            .collect(Collectors.toList());
        
        Set<Long> uniqueCustomers = bills.stream()
            .map(Bill::getCustomerId)
            .collect(Collectors.toSet());
        
        // Calculate repeat customers
        Map<Long, Long> customerFrequency = bills.stream()
            .collect(Collectors.groupingBy(Bill::getCustomerId, Collectors.counting()));
        
        long repeatCustomers = customerFrequency.values().stream()
            .filter(count -> count > 1)
            .count();
        
        double retentionRate = uniqueCustomers.size() > 0
            ? ((double) repeatCustomers / uniqueCustomers.size()) * 100
            : 0.0;
        
        // Customer segments
        Map<String, Long> segments = new HashMap<>();
        segments.put("oneTime", customerFrequency.values().stream().filter(c -> c == 1).count());
        segments.put("occasional", customerFrequency.values().stream().filter(c -> c >= 2 && c <= 5).count());
        segments.put("regular", customerFrequency.values().stream().filter(c -> c >= 6 && c <= 10).count());
        segments.put("loyal", customerFrequency.values().stream().filter(c -> c > 10).count());
        
        Map<String, Object> result = new HashMap<>();
        result.put("period", Map.of("startDate", startDate, "endDate", endDate));
        result.put("totalCustomers", uniqueCustomers.size());
        result.put("repeatCustomers", repeatCustomers);
        result.put("retentionRate", retentionRate);
        result.put("customerSegments", segments);
        result.put("averageVisitsPerCustomer", uniqueCustomers.size() > 0 
            ? (double) bills.size() / uniqueCustomers.size() : 0.0);
        
        return result;
    }
    
    // ==================== HELPER METHODS ====================
    
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date must be before end date");
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 365) {
            throw new BusinessValidationException("Date range cannot exceed 365 days");
        }
    }
    
    private List<BigDecimal> calculateMovingAverage(List<DailySalesSummaryDTO> summaries, int window) {
        List<BigDecimal> movingAverages = new ArrayList<>();
        
        for (int i = 0; i < summaries.size(); i++) {
            int start = Math.max(0, i - window + 1);
            int end = i + 1;
            
            BigDecimal sum = summaries.subList(start, end).stream()
                .map(DailySalesSummaryDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avg = sum.divide(BigDecimal.valueOf(end - start), 2, RoundingMode.HALF_UP);
            movingAverages.add(avg);
        }
        
        return movingAverages;
    }
    
    private double[] simpleLinearRegression(List<BigDecimal> data, int forecastDays) {
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = data.get(i).doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        double[] forecast = new double[forecastDays];
        for (int i = 0; i < forecastDays; i++) {
            forecast[i] = Math.max(0, slope * (n + i + 1) + intercept);
        }
        
        return forecast;
    }
}

// Made with Bob
