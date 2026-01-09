package com.dts.restro.controller;

import com.dts.restro.dto.stats.TodayStatsDTO;
import com.dts.restro.dto.stats.TopItemDTO;
import com.dts.restro.dto.stats.WeeklyRevenueDTO;
import com.dts.restro.service.SalesReportService;
import org.springframework.web.bind.annotation.*;

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
}