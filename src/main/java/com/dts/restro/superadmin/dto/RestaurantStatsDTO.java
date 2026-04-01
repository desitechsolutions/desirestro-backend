package com.dts.restro.superadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStatsDTO {
    private Long restaurantId;
    private String restaurantName;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalTables;
    private Long totalMenuItems;
    private Long totalOrders;
    private Double totalRevenue;
    private Long todayOrders;
    private Double todayRevenue;
    private Long openTickets;
    private LocalDateTime lastActivity;
    private Boolean isActive;
}

// Made with Bob
