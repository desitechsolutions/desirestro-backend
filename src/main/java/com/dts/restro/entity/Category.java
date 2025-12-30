package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., Starters, Main Course, Desserts

    private int displayOrder;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<MenuItem> items;
}