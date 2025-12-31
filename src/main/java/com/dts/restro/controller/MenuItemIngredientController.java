package com.dts.restro.controller;

import com.dts.restro.entity.Ingredient;
import com.dts.restro.entity.MenuItem;
import com.dts.restro.entity.MenuItemIngredient;
import com.dts.restro.entity.MenuItemIngredientId;
import com.dts.restro.repository.MenuItemIngredientRepository;
import com.dts.restro.repository.MenuItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/recipes")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuItemIngredientController {

    private final MenuItemIngredientRepository recipeRepo;
    private final MenuItemRepository menuItemRepo;

    public MenuItemIngredientController(MenuItemIngredientRepository recipeRepo, MenuItemRepository menuItemRepo) {
        this.recipeRepo = recipeRepo;
        this.menuItemRepo = menuItemRepo;
    }

    @GetMapping("/menu/{menuItemId}")
    public List<MenuItemIngredient> getRecipe(@PathVariable Long menuItemId) {
        return recipeRepo.findByMenuItemId(menuItemId);
    }

    @PostMapping
    public MenuItemIngredient addIngredientToRecipe(@RequestBody AddRecipeRequest request) {
        MenuItem menuItem = menuItemRepo.findById(request.menuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        MenuItemIngredient recipe = new MenuItemIngredient();
        recipe.setMenuItem(menuItem);
        recipe.setIngredient(request.ingredient());
        recipe.setQuantityRequired(request.quantityRequired());

        return recipeRepo.save(recipe);
    }
}

record AddRecipeRequest(Long menuItemId, Ingredient ingredient, double quantityRequired) {}