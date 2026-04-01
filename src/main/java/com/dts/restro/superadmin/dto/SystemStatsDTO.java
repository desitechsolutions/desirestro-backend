package com.dts.restro.superadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsDTO {
    private Long totalRestaurants;
    private Long activeRestaurants;
    private Long inactiveRestaurants;
    private Long totalUsers;
    private Long activeUsers;
    private Long openTickets;
    private Long inProgressTickets;
    private Long resolvedTickets;
    private Double totalRevenue;
    private Long todayOrders;
    private Long todayRevenue;
}

// Made with Bob
