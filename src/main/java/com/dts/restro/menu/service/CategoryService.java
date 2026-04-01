package com.dts.restro.menu.service;

import com.dts.restro.menu.dto.CategoryDTO;
import com.dts.restro.menu.entity.Category;
import com.dts.restro.menu.mapper.CategoryMapper;
import com.dts.restro.menu.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    // Get all categories as DTOs
    public List<CategoryDTO> getAllOrdered() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    // Create category from DTO
    public CategoryDTO create(CategoryDTO dto) {
        validateCategory(dto);

        Category category = categoryMapper.toEntity(dto);

        if (category.getDisplayOrder() == null || category.getDisplayOrder() <= 0) {
            Integer maxOrder = categoryRepository.findMaxDisplayOrder();
            category.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);
        }

        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    // Update category from DTO
    public CategoryDTO update(Long id, CategoryDTO dto) {
        validateCategory(dto);

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existing.setName(dto.getName());
        if (dto.getDisplayOrder() != null && dto.getDisplayOrder() > 0) {
            existing.setDisplayOrder(dto.getDisplayOrder());
        }

        Category updated = categoryRepository.save(existing);
        return categoryMapper.toDTO(updated);
    }

    // Delete category
    public void delete(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (cat.getItems() != null && !cat.getItems().isEmpty()) {
            throw new RuntimeException("Cannot delete category '" + cat.getName() + "' because it has " + cat.getItems().size() + " menu items assigned");
        }

        categoryRepository.delete(cat);
    }

    // Validate DTO fields
    private void validateCategory(CategoryDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
    }
}
