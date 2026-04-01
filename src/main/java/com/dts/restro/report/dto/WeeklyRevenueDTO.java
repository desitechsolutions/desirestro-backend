package com.dts.restro.report.dto;

import lombok.Data;

@Data
public class WeeklyRevenueDTO {
    private String day; // Mon, Tue, etc.
    private double revenue;
}