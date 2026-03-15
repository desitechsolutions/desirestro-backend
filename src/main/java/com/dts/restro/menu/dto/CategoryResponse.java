package com.dts.restro.menu.dto;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        int displayOrder,
        List<MenuItemResponse> items
) {}