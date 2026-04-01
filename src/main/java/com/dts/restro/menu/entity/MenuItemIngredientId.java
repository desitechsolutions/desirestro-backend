package com.dts.restro.menu.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuItemIngredientId implements Serializable {
    private Long menuItem;
    private Long ingredient;
}