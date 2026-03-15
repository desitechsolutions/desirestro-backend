package com.dts.restro.report.dto;

import lombok.Data;

@Data
public class TopItemDTO {
    private String name;
    private int quantity;
    private double revenue;
}