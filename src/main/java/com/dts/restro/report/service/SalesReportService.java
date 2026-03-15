package com.dts.restro.report.service;

import com.dts.restro.report.dto.TodayStatsDTO;
import com.dts.restro.report.dto.TopItemDTO;
import com.dts.restro.report.dto.WeeklyRevenueDTO;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.order.repository.KOTRepository;
import org.springframework.stereotype.Service;
import java.time.format.TextStyle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesReportService {

    private final BillRepository billRepository;
    private final KOTRepository kotRepository;

    public SalesReportService(BillRepository billRepository, KOTRepository kotRepository) {
        this.billRepository = billRepository;
        this.kotRepository = kotRepository;
    }

    public TodayStatsDTO getTodayStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<Object[]> billResults = billRepository.getDailyBillStats(startOfDay, endOfDay);
        Long totalItemCount = kotRepository.getDailyItemCount(startOfDay, endOfDay);

        double revenue = 0;
        int bills = 0;
        if (!billResults.isEmpty()) {
            Object[] row = billResults.get(0);
            revenue = ((Number) row[0]).doubleValue();
            bills = ((Number) row[1]).intValue();
        }

        int totalItems = totalItemCount != null ? totalItemCount.intValue() : 0;

        double avgBill = bills > 0 ? revenue / bills : 0;

        TodayStatsDTO stats = new TodayStatsDTO();
        stats.setRevenue(revenue);
        stats.setBills(bills);
        stats.setAvgBill(avgBill);
        stats.setOrders(totalItems);
        return stats;
    }

    public List<TopItemDTO> getTopSellingItemsToday(int limit) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<Object[]> results = kotRepository.getTopSellingItems(startOfDay, endOfDay, limit);

        return results.stream()
                .map(row -> {
                    TopItemDTO dto = new TopItemDTO();
                    dto.setName((String) row[0]);
                    dto.setQuantity(((Number) row[1]).intValue());
                    dto.setRevenue(((Number) row[2]).doubleValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<WeeklyRevenueDTO> getWeeklyRevenue() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<Object[]> results = billRepository.getRevenueByDay(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        Map<String, Double> revenueMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            revenueMap.put(dayName, 0.0);
        }

        results.forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            double amount = ((Number) row[1]).doubleValue();
            revenueMap.put(dayName, amount);
        });

        return revenueMap.entrySet().stream()
                .map(entry -> {
                    WeeklyRevenueDTO dto = new WeeklyRevenueDTO();
                    dto.setDay(entry.getKey());
                    dto.setRevenue(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<WeeklyRevenueDTO> getRevenueByDateRange(LocalDate from, LocalDate to) {
        List<Object[]> results = billRepository.getRevenueByDay(from.atStartOfDay(), to.atTime(23, 59, 59));

        Map<String, Double> revenueMap = new LinkedHashMap<>();
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        for (int i = 0; i < days; i++) {
            LocalDate date = from.plusDays(i);
            revenueMap.put(date.toString(), 0.0);
        }

        results.forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            double amount = ((Number) row[1]).doubleValue();
            revenueMap.put(date.toString(), amount);
        });

        return revenueMap.entrySet().stream()
                .map(entry -> {
                    WeeklyRevenueDTO dto = new WeeklyRevenueDTO();
                    dto.setDay(entry.getKey());
                    dto.setRevenue(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}