package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class KOT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kotNumber;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @ElementCollection // This creates a separate table kot_items with kot_id FK
    private List<KOTItem> items = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    private String status = "NEW"; // NEW, PREPARING, READY, SERVED
}