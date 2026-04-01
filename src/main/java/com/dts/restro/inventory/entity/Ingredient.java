package com.dts.restro.inventory.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "ingredient")
@Data
@EqualsAndHashCode(callSuper = false)
public class Ingredient extends RestaurantAwareEntity {

    private String name;

    private String unit; // "kg", "g", "litre", "pcs"

    private double currentStock;

    private double reorderLevel;
}