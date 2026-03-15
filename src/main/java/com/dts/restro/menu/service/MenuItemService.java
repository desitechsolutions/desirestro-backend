package com.dts.restro.menu.service;

import com.dts.restro.menu.dto.MenuItemDTO;
import com.dts.restro.menu.entity.Category;
import com.dts.restro.inventory.entity.Ingredient;
import com.dts.restro.menu.entity.MenuItem;
import com.dts.restro.menu.entity.MenuItemIngredient;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.menu.mapper.MenuItemIngredientMapper;
import com.dts.restro.menu.mapper.MenuItemMapper;
import com.dts.restro.menu.repository.CategoryRepository;
import com.dts.restro.inventory.repository.IngredientRepository;
import com.dts.restro.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
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

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
        item.setCategory(category);

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

        // orphanRemoval=true on MenuItem.ingredients handles deletion of old records
        item.getIngredients().clear();
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
}
