package com.dts.restro.menu.entity;

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

    // Indian Restaurant Features
    @Column(name = "spice_level", length = 20)
    private String spiceLevel; // MILD, MEDIUM, HOT, EXTRA_HOT

    @Column(name = "is_jain")
    private Boolean isJain = false;

    @Column(name = "is_swaminarayan")
    private Boolean isSwaminarayan = false;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode; // HSN code for GST

    @Column(name = "preparation_time")
    private Integer preparationTime = 15; // in minutes
}