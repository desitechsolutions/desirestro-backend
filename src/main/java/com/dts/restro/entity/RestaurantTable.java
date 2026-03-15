package com.dts.restro.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "restaurant_table")
@Data
@EqualsAndHashCode(callSuper = false)
public class RestaurantTable extends RestaurantAwareEntity {

    private String tableNumber;

    private int capacity;

    private int occupiedSeats = 0;

    private String status = "EMPTY"; // EMPTY, OCCUPIED, BILLING, DIRTY

    private String currentCaptain;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Party> parties;
}