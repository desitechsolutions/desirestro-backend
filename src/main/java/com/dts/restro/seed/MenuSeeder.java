package com.dts.restro.seed;

import com.dts.restro.entity.*;
import com.dts.restro.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuSeeder {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;

    private final IngredientRepository ingredientRepository ;
    private final MenuItemIngredientRepository menuItemIngredientRepository;

    public MenuSeeder(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository, RestaurantTableRepository tableRepository
    ,IngredientRepository ingredientRepository, MenuItemIngredientRepository menuItemIngredientRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemIngredientRepository = menuItemIngredientRepository;
    }

    @PostConstruct
    public void seed() {
        if (categoryRepository.count() == 0) {
            // Create Categories
            Category starters = new Category();
            starters.setName("Starters");
            starters.setDisplayOrder(1);

            Category mainCourse = new Category();
            mainCourse.setName("Main Course");
            mainCourse.setDisplayOrder(2);

            Category desserts = new Category();
            desserts.setName("Desserts");
            desserts.setDisplayOrder(3);

            Category beverages = new Category();
            beverages.setName("Beverages");
            beverages.setDisplayOrder(4);

            categoryRepository.saveAll(List.of(starters, mainCourse, desserts, beverages));

            // Create Menu Items
            MenuItem item1 = new MenuItem();
            item1.setName("Paneer Tikka");
            item1.setDescription("Cottage cheese marinated in spices and grilled");
            item1.setPrice(249.00);
            item1.setVeg(true);
            item1.setAvailable(true);
            item1.setCategory(starters);

            MenuItem item2 = new MenuItem();
            item2.setName("Chicken 65");
            item2.setDescription("Spicy deep-fried chicken");
            item2.setPrice(299.00);
            item2.setVeg(false);
            item2.setAvailable(true);
            item2.setCategory(starters);

            MenuItem item3 = new MenuItem();
            item3.setName("Veg Biryani");
            item3.setDescription("Fragrant rice with mixed vegetables");
            item3.setPrice(349.00);
            item3.setVeg(true);
            item3.setAvailable(true);
            item3.setCategory(mainCourse);

            MenuItem item4 = new MenuItem();
            item4.setName("Butter Chicken");
            item4.setDescription("Creamy tomato-based chicken curry");
            item4.setPrice(429.00);
            item4.setVeg(false);
            item4.setAvailable(true);
            item4.setCategory(mainCourse);

            MenuItem item5 = new MenuItem();
            item5.setName("Gulab Jamun");
            item5.setDescription("Soft milk-solid balls in rose syrup");
            item5.setPrice(149.00);
            item5.setVeg(true);
            item5.setAvailable(true);
            item5.setCategory(desserts);

            MenuItem item6 = new MenuItem();
            item6.setName("Masala Chai");
            item6.setDescription("Spiced Indian tea");
            item6.setPrice(69.00);
            item6.setVeg(true);
            item6.setAvailable(true);
            item6.setCategory(beverages);

            MenuItem item7 = new MenuItem();
            item7.setName("Lassi");
            item7.setDescription("Sweet yogurt drink");
            item7.setPrice(99.00);
            item7.setVeg(true);
            item7.setAvailable(true);
            item7.setCategory(beverages);

            menuItemRepository.saveAll(List.of(item1, item2, item3, item4, item5, item6, item7));

            System.out.println("Sample menu seeded successfully!");
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

        if (ingredientRepository.count() == 0) {
            Ingredient chicken = new Ingredient();
            chicken.setName("Chicken");
            chicken.setUnit("kg");
            chicken.setCurrentStock(50);
            chicken.setReorderLevel(10);

            Ingredient paneer = new Ingredient();
            paneer.setName("Paneer");
            paneer.setUnit("kg");
            paneer.setCurrentStock(30);
            paneer.setReorderLevel(8);

            Ingredient rice = new Ingredient();
            rice.setName("Rice");
            rice.setUnit("kg");
            rice.setCurrentStock(100);
            rice.setReorderLevel(20);

            ingredientRepository.saveAll(List.of(chicken, paneer, rice));

            // Link to menu items (example for Butter Chicken & Paneer Tikka)
            MenuItem butterChicken = menuItemRepository.findByName("Butter Chicken").get(0);
            MenuItem paneerTikka = menuItemRepository.findByName("Paneer Tikka").get(0);

            MenuItemIngredient bcChicken = new MenuItemIngredient();
            bcChicken.setMenuItem(butterChicken);
            bcChicken.setIngredient(chicken);
            bcChicken.setQuantityRequired(0.2); // 200g per portion

            MenuItemIngredient ptPaneer = new MenuItemIngredient();
            ptPaneer.setMenuItem(paneerTikka);
            ptPaneer.setIngredient(paneer);
            ptPaneer.setQuantityRequired(0.25); // 250g per portion

            menuItemIngredientRepository.saveAll(List.of(bcChicken, ptPaneer));
        }
    }

    private RestaurantTable createTable(String number, int seats) {
        RestaurantTable t = new RestaurantTable();
        t.setTableNumber(number);
        t.setCapacity(seats);
        t.setOccupiedSeats(0);
        t.setStatus("EMPTY");
        return t;
    }
}