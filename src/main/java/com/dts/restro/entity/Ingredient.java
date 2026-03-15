package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "Chicken", "Rice", "Paneer"

    private String unit; // "kg", "g", "litre", "pcs"

    private double currentStock;

    private double reorderLevel; // Alert when below this
}