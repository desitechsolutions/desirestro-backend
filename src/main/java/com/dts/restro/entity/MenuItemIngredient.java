package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@IdClass(MenuItemIngredientId.class)
@Data
public class MenuItemIngredient {
    @Id
    @ManyToOne
    private MenuItem menuItem;

    @Id
    @ManyToOne
    private Ingredient ingredient;

    private double quantityRequired; // e.g., 200g chicken per Butter Chicken
}