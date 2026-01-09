package com.dts.restro.dto.stats;

import lombok.Data;

@Data
public class TopItemDTO {
    private String name;
    private int quantity;
    private double revenue;
}