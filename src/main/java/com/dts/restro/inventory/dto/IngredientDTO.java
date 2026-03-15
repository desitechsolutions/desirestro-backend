// src/main/java/com/dts/restro/dto/IngredientDTO.java

package com.dts.restro.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {

    private Long id;

    @NotBlank(message = "Ingredient name is required")
    private String name;

    @NotBlank(message = "Unit is required (e.g., kg, litre, pcs)")
    private String unit;

    @PositiveOrZero(message = "Current stock must be >= 0")
    private double currentStock;

    @PositiveOrZero(message = "Reorder level must be >= 0")
    private double reorderLevel;
}