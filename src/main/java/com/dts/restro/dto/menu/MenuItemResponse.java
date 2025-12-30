package com.dts.restro.dto.menu;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        double price,
        boolean veg,
        boolean available
) {}