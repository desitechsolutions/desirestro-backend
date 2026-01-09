package com.dts.restro.dto.stats;

import lombok.Data;

@Data
public class WeeklyRevenueDTO {
    private String day; // Mon, Tue, etc.
    private double revenue;
}