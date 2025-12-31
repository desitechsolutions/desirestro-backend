package com.dts.restro.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RestaurantTable table;

    private int occupiedSeats;

    private LocalDateTime arrivedAt = LocalDateTime.now();

    private String status = "ACTIVE";

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<KOT> kots;

    @OneToOne(mappedBy = "party")
    private Bill bill;
}