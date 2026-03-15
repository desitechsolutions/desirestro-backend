package com.dts.restro.order.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kot")
@Data
@EqualsAndHashCode(callSuper = false)
public class KOT extends RestaurantAwareEntity {

    private String kotNumber;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @ElementCollection
    private List<KOTItem> items = new ArrayList<>();

    private String status = "NEW"; // NEW, PREPARING, READY, SERVED
}