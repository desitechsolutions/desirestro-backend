package com.dts.restro.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class KOTItem {
    private Long menuItemId;        // Store ID only
    private String menuItemName;    // Denormalized for display & printing
    private double price;
    private int quantity;
    private String notes = "";
    private Integer guestNumber;    // Optional: for split billing
}