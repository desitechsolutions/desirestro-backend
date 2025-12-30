package com.dts.restro.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class KOTItem {
    private Long menuItemId;
    private String menuItemName;
    private double price;
    private int quantity;
    private String notes; // future modifiers
    private int guestNumber; // 1-4 for table seats
}