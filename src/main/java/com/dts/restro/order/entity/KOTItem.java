package com.dts.restro.order.entity;

import jakarta.persistence.Column;
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

    // Indian Restaurant Features
    @Column(name = "spice_level", length = 20)
    private String spiceLevel; // MILD, MEDIUM, HOT, EXTRA_HOT

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "is_jain")
    private Boolean isJain = false;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode; // HSN code for GST
}