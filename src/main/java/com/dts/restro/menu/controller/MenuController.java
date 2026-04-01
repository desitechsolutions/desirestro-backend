package com.dts.restro.menu.controller;

import com.dts.restro.menu.dto.CategoryResponse;
import com.dts.restro.menu.dto.MenuItemResponse;
import com.dts.restro.menu.entity.Category;
import com.dts.restro.menu.entity.MenuItem;
import com.dts.restro.menu.repository.CategoryRepository;
import com.dts.restro.menu.repository.MenuItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuController {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuController(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping
    public List<CategoryResponse> getCategoriesWithItems() {
        return categoryRepository.findAllWithAvailableItemsOrdered()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDisplayOrder(),
                        category.getItems().stream()
                                .filter(MenuItem::isAvailable)
                                .map(item -> new MenuItemResponse(
                                        item.getId(),
                                        item.getName(),
                                        item.getDescription(),
                                        item.getPrice(),
                                        item.isVeg(),
                                        item.isAvailable()
                                ))
                                .toList()
                ))
                .toList();
    }

    @GetMapping("/available-items")
    public List<MenuItemResponse> getAllAvailableItems(@RequestParam(required = false) String search) {
        List<MenuItem> items;
        if (search != null && !search.isBlank()) {
            items = menuItemRepository.findAvailableByNameWithCategory(search);
        } else {
            items = menuItemRepository.findAvailableWithCategory();
        }
        return items.stream()
                .map(item -> new MenuItemResponse(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getPrice(),
                        item.isVeg(),
                        item.isAvailable()
                ))
                .toList();
    }
}