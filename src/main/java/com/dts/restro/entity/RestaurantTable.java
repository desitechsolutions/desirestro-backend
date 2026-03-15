package com.dts.restro.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "restaurant_table")
@Data
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableNumber; // e.g., "T1", "T2"

    private int capacity;

    private int occupiedSeats = 0;
    private String status = "EMPTY"; // EMPTY, OCCUPIED, BILLING, DIRTY

    private String currentCaptain; // username of captain handling it (optional)

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Party> parties;
}