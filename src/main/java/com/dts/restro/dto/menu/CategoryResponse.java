package com.dts.restro.dto.menu;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        int displayOrder,
        List<MenuItemResponse> items
) {}