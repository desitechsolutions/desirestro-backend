package com.dts.restro.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_item")
@Data
@EqualsAndHashCode(callSuper = false)
public class MenuItem extends RestaurantAwareEntity {

    private String name;

    private String description;

    private double price;

    private boolean veg = true;

    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItemIngredient> ingredients = new ArrayList<>();
}