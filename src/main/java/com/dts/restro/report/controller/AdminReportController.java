package com.dts.restro.report.controller;

import com.dts.restro.report.dto.TodayStatsDTO;
import com.dts.restro.report.dto.TopItemDTO;
import com.dts.restro.report.dto.WeeklyRevenueDTO;
import com.dts.restro.report.service.SalesReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminReportController {

    private final SalesReportService salesReportService;

    public AdminReportController(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    @GetMapping("/today-stats")
    public TodayStatsDTO getTodayStats() {
        return salesReportService.getTodayStats();
    }

    @GetMapping("/top-items")
    public List<TopItemDTO> getTopItems(@RequestParam(defaultValue = "10") int limit) {
        return salesReportService.getTopSellingItemsToday(limit);
    }

    @GetMapping("/weekly-revenue")
    public List<WeeklyRevenueDTO> getWeeklyRevenue() {
        return salesReportService.getWeeklyRevenue();
    }

    @GetMapping("/revenue")
    public List<WeeklyRevenueDTO> getRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return salesReportService.getRevenueByDateRange(from, to);
    }
}
