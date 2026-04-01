package com.dts.restro.menu.dto;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        double price,
        boolean veg,
        boolean available
) {}