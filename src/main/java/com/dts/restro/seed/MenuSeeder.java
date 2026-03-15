/*
// src/main/java/com/dts/retro/seed/MenuSeeder.java

package com.dts.restro.seed;

import com.dts.restro.menu.entity.Category;
import com.dts.restro.menu.entity.MenuItem;
import com.dts.restro.menu.entity.MenuItemIngredient;
import com.dts.restro.menu.entity.MenuItemIngredientId;
import com.dts.restro.inventory.entity.Ingredient;
import com.dts.restro.order.entity.RestaurantTable;
import com.dts.restro.menu.repository.CategoryRepository;
import com.dts.restro.menu.repository.MenuItemRepository;
import com.dts.restro.menu.repository.MenuItemIngredientRepository;
import com.dts.restro.inventory.repository.IngredientRepository;
import com.dts.restro.order.repository.RestaurantTableRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MenuSeeder {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemIngredientRepository menuItemIngredientRepository;

    public MenuSeeder(
            CategoryRepository categoryRepository,
            MenuItemRepository menuItemRepository,
            RestaurantTableRepository tableRepository,
            IngredientRepository ingredientRepository,
            MenuItemIngredientRepository menuItemIngredientRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemIngredientRepository = menuItemIngredientRepository;
    }

    @PostConstruct
    public void seed() {
        seedCategoriesAndMenu();
        seedTables();
        seedIngredientsAndRecipes();
    }

    private void seedCategoriesAndMenu() {
        if (categoryRepository.count() == 0) {
            Category starters = createCategory("Starters", 1);
            Category mainCourse = createCategory("Main Course", 2);
            Category desserts = createCategory("Desserts", 3);
            Category beverages = createCategory("Beverages", 4);

            categoryRepository.saveAll(List.of(starters, mainCourse, desserts, beverages));

            List<MenuItem> menuItems = List.of(
                    createMenuItem("Paneer Tikka", "Cottage cheese marinated in spices and grilled", 249.00, true, starters),
                    createMenuItem("Chicken 65", "Spicy deep-fried chicken", 299.00, false, starters),
                    createMenuItem("Veg Biryani", "Fragrant rice with mixed vegetables", 349.00, true, mainCourse),
                    createMenuItem("Butter Chicken", "Creamy tomato-based chicken curry", 429.00, false, mainCourse),
                    createMenuItem("Gulab Jamun", "Soft milk-solid balls in rose syrup", 149.00, true, desserts),
                    createMenuItem("Masala Chai", "Spiced Indian tea", 69.00, true, beverages),
                    createMenuItem("Lassi", "Sweet yogurt drink", 99.00, true, beverages)
            );

            menuItemRepository.saveAll(menuItems);
            System.out.println("Sample menu and categories seeded successfully!");
        }
    }

    private void seedTables() {
        if (tableRepository.count() == 0) {
            List<RestaurantTable> tables = List.of(
                    createTable("T1", 4),
                    createTable("T2", 6),
                    createTable("T3", 4),
                    createTable("T4", 8),
                    createTable("T5", 2),
                    createTable("T6", 4),
                    createTable("T7", 6),
                    createTable("T8", 4)
            );
            tableRepository.saveAll(tables);
            System.out.println("Sample tables seeded!");
        }
    }

    private void seedIngredientsAndRecipes() {
        if (ingredientRepository.count() == 0) {
            Ingredient chicken = createIngredient("Chicken", "kg", 50.0, 10.0);
            Ingredient paneer = createIngredient("Paneer", "kg", 30.0, 8.0);
            Ingredient rice = createIngredient("Rice", "kg", 100.0, 20.0);

            ingredientRepository.saveAll(List.of(chicken, paneer, rice));

            // Now safely link recipes — menu items already exist
            Optional<MenuItem> butterChickenOpt = menuItemRepository.findByName("Butter Chicken").stream().findFirst();
            Optional<MenuItem> paneerTikkaOpt = menuItemRepository.findByName("Paneer Tikka").stream().findFirst();

            if (butterChickenOpt.isPresent() && paneerTikkaOpt.isPresent()) {
                MenuItem butterChicken = butterChickenOpt.get();
                MenuItem paneerTikka = paneerTikkaOpt.get();

                MenuItemIngredient bcChickenLink = new MenuItemIngredient();
                bcChickenLink.setMenuItem(butterChicken);
                bcChickenLink.setIngredient(chicken);
                bcChickenLink.setQuantityRequired(0.2); // 200g

                MenuItemIngredient ptPaneerLink = new MenuItemIngredient();
                ptPaneerLink.setMenuItem(paneerTikka);
                ptPaneerLink.setIngredient(paneer);
                ptPaneerLink.setQuantityRequired(0.25); // 250g

                menuItemIngredientRepository.saveAll(List.of(bcChickenLink, ptPaneerLink));
                System.out.println("Ingredients and recipe links seeded!");
            } else {
                System.out.println("Warning: Could not find menu items for recipe linking");
            }
        }
    }

    private Category createCategory(String name, int order) {
        Category cat = new Category();
        cat.setName(name);
        cat.setDisplayOrder(order);
        return cat;
    }

    private MenuItem createMenuItem(String name, String desc, double price, boolean veg, Category category) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(desc);
        item.setPrice(price);
        item.setVeg(veg);
        item.setAvailable(true);
        item.setCategory(category);
        return item;
    }

    private Ingredient createIngredient(String name, String unit, double stock, double reorder) {
        Ingredient ing = new Ingredient();
        ing.setName(name);
        ing.setUnit(unit);
        ing.setCurrentStock(stock);
        ing.setReorderLevel(reorder);
        return ing;
    }

    private RestaurantTable createTable(String number, int seats) {
        RestaurantTable t = new RestaurantTable();
        t.setTableNumber(number);
        t.setCapacity(seats);
        t.setOccupiedSeats(0);
        t.setStatus("EMPTY");
        return t;
    }
}*/
