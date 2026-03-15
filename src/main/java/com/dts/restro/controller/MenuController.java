package com.dts.restro.controller;

import com.dts.restro.dto.menu.CategoryResponse;
import com.dts.restro.dto.menu.MenuItemResponse;
import com.dts.restro.entity.Category;
import com.dts.restro.entity.MenuItem;
import com.dts.restro.repository.CategoryRepository;
import com.dts.restro.repository.MenuItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
/*@RequestMapping("/api/menu")*/
@CrossOrigin(origins = "http://localhost:3000")
public class MenuController {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuController(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // GET all categories with their available items
    @GetMapping("/categories")
    public List<CategoryResponse> getCategoriesWithItems() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc()
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

    // GET all available items (flat list - useful for search later)
    @GetMapping("/items")
    public List<MenuItemResponse> getAllAvailableItems() {
        return menuItemRepository.findByAvailableTrue()
                .stream()
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