package com.dts.restro.service;

import com.dts.restro.entity.Ingredient;
import com.dts.restro.entity.KOT;
import com.dts.restro.entity.KOTItem;
import com.dts.restro.entity.MenuItemIngredient;
import com.dts.restro.repository.IngredientRepository;
import com.dts.restro.repository.MenuItemIngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final MenuItemIngredientRepository menuItemIngredientRepo;
    private final IngredientRepository ingredientRepo;

    public InventoryService(MenuItemIngredientRepository menuItemIngredientRepo, IngredientRepository ingredientRepo) {
        this.menuItemIngredientRepo = menuItemIngredientRepo;
        this.ingredientRepo = ingredientRepo;
    }

    public void deductFromKOT(KOT kot) {
        for (KOTItem item : kot.getItems()) {
            List<MenuItemIngredient> recipes = menuItemIngredientRepo
                    .findByMenuItemId(item.getMenuItemId());

            for (MenuItemIngredient recipe : recipes) {
                Ingredient ing = recipe.getIngredient();
                double deduct = recipe.getQuantityRequired() * item.getQuantity();
                ing.setCurrentStock(ing.getCurrentStock() - deduct);
                ingredientRepo.save(ing);
            }
        }
    }

    public List<Ingredient> getLowStock() {
        return ingredientRepo.findAll().stream()
                .filter(i -> i.getCurrentStock() < i.getReorderLevel())
                .toList();
    }

    public Ingredient restock(Long ingredientId, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        Ingredient ingredient = ingredientRepo.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientId));
        ingredient.setCurrentStock(ingredient.getCurrentStock() + quantity);
        return ingredientRepo.save(ingredient);
    }
}