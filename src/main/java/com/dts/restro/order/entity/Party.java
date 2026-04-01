package com.dts.restro.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dts.restro.billing.entity.Bill;
import com.dts.restro.common.entity.RestaurantAwareEntity;
import com.dts.restro.order.entity.RestaurantTable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "party")
@Data
@EqualsAndHashCode(callSuper = false)
public class Party extends RestaurantAwareEntity {

    @ManyToOne
    @JoinColumn(name = "table_id")
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