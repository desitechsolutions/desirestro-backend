package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class KOT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kotNumber; // e.g., KOT-20251230-0001

    @ManyToOne
    private RestaurantTable table;

    @ElementCollection
    private List<KOTItem> items;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String status = "NEW"; // NEW, PREPARING, READY, SERVED
}