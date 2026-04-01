package com.dts.restro.menu.entity;

import com.dts.restro.inventory.entity.Ingredient;
import com.dts.restro.menu.entity.MenuItem;
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