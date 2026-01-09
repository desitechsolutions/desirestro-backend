package com.dts.restro.service;

import com.dts.restro.dto.menu.MenuItemDTO;
import com.dts.restro.entity.Category;
import com.dts.restro.entity.Ingredient;
import com.dts.restro.entity.MenuItem;
import com.dts.restro.entity.MenuItemIngredient;
import com.dts.restro.mapper.MenuItemIngredientMapper;
import com.dts.restro.mapper.MenuItemMapper;
import com.dts.restro.repository.CategoryRepository;
import com.dts.restro.repository.IngredientRepository;
import com.dts.restro.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemMapper menuItemMapper;
    private final MenuItemIngredientMapper ingredientMapper;

    public MenuItemService(MenuItemRepository menuItemRepository,
                           CategoryRepository categoryRepository,
                           IngredientRepository ingredientRepository,
                           MenuItemMapper menuItemMapper,
                           MenuItemIngredientMapper ingredientMapper) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemMapper = menuItemMapper;
        this.ingredientMapper = ingredientMapper;
    }

    public List<MenuItemDTO> getAllMenuItems() {
        return menuItemMapper.toDTOList(menuItemRepository.findAll());
    }

    public MenuItemDTO createMenuItem(MenuItemDTO dto) {
        MenuItem item = menuItemMapper.toEntity(dto);

        // Set category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
        item.setCategory(category);

        // Map ingredients
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            List<Long> ingredientIds = dto.getIngredients().stream()
                    .map(i -> i.getIngredientId())
                    .collect(Collectors.toList());

            Map<Long, Ingredient> ingredientMap = ingredientRepository.findAllById(ingredientIds)
                    .stream()
                    .collect(Collectors.toMap(Ingredient::getId, i -> i));

            List<MenuItemIngredient> ingredients = ingredientMapper.toEntityList(dto.getIngredients());
            ingredients.forEach(i -> {
                i.setMenuItem(item);
                Ingredient ing = ingredientMap.get(i.getIngredient().getId());
                if (ing == null) throw new ResourceNotFoundException("Ingredient not found with id: " + i.getIngredient().getId());
                i.setIngredient(ing);
            });

            item.setIngredients(ingredients);
        }

        MenuItem saved = menuItemRepository.save(item);
        return menuItemMapper.toDTO(saved);
    }

    public MenuItemDTO updateMenuItem(Long id, MenuItemDTO dto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setVeg(dto.isVeg());
        item.setAvailable(dto.isAvailable());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            item.setCategory(category);
        }

        // Handle ingredients efficiently
        item.getIngredients().clear(); // make sure orphanRemoval = true in MenuItem entity
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            List<Long> ingredientIds = dto.getIngredients().stream()
                    .map(i -> i.getIngredientId())
                    .collect(Collectors.toList());

            Map<Long, Ingredient> ingredientMap = ingredientRepository.findAllById(ingredientIds)
                    .stream()
                    .collect(Collectors.toMap(Ingredient::getId, i -> i));

            List<MenuItemIngredient> ingredients = ingredientMapper.toEntityList(dto.getIngredients());
            ingredients.forEach(i -> {
                i.setMenuItem(item);
                Ingredient ing = ingredientMap.get(i.getIngredient().getId());
                if (ing == null) throw new ResourceNotFoundException("Ingredient not found with id: " + i.getIngredient().getId());
                i.setIngredient(ing);
            });

            item.setIngredients(ingredients);
        }

        MenuItem saved = menuItemRepository.save(item);
        return menuItemMapper.toDTO(saved);
    }

    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    // Custom exception for better clarity
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
