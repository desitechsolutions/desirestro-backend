package com.dts.restro.report.dto;

import lombok.Data;

@Data
public class TodayStatsDTO {
    private double revenue;
    private int bills;
    private double avgBill;
    private int orders; // total items served
}
