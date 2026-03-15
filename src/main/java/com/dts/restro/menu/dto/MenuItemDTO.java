package com.dts.restro.menu.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuItemDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private boolean veg;
    private boolean available;
    private Long categoryId;
    private String categoryName; // For frontend display
    private List<MenuItemIngredientDTO> ingredients;
}
