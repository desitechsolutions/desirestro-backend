package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private double price;

    private boolean veg = true; // true = veg, false = non-veg

    private boolean available = true;

    @ManyToOne
    private Category category;
}