package com.dts.restro.inventory.service;

import com.dts.restro.inventory.dto.IngredientDTO;
import com.dts.restro.inventory.entity.Ingredient;
import com.dts.restro.inventory.mapper.IngredientMapper;
import com.dts.restro.inventory.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository,
                             IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
    }

    public List<IngredientDTO> getAll() {
        return ingredientRepository.findAll()
                .stream()
                .map(ingredientMapper::toDto)
                .collect(Collectors.toList());
    }

    public IngredientDTO create(IngredientDTO dto) {
        validate(dto);
        Ingredient entity = ingredientMapper.toEntity(dto);
        Ingredient saved = ingredientRepository.save(entity);
        return ingredientMapper.toDto(saved);
    }

    public IngredientDTO update(Long id, IngredientDTO dto) {
        validate(dto);

        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        existing.setName(dto.getName());
        existing.setUnit(dto.getUnit());
        existing.setCurrentStock(dto.getCurrentStock());
        existing.setReorderLevel(dto.getReorderLevel());

        Ingredient saved = ingredientRepository.save(existing);
        return ingredientMapper.toDto(saved);
    }

    public void delete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        ingredientRepository.delete(ingredient);
    }

    private void validate(IngredientDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }
        if (dto.getUnit() == null || dto.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit is required");
        }
        if (dto.getCurrentStock() < 0 || dto.getReorderLevel() < 0) {
            throw new IllegalArgumentException("Stock values cannot be negative");
        }
    }
}
