package com.dts.restro.controller;

import com.dts.restro.dto.IngredientDTO;
import com.dts.restro.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@CrossOrigin(origins = "http://localhost:3000")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<IngredientDTO> getAllIngredients() {
        return ingredientService.getAll();
    }

    @PostMapping
    public IngredientDTO createIngredient(@Valid @RequestBody IngredientDTO dto) {
        return ingredientService.create(dto);
    }

    @PutMapping("/{id}")
    public IngredientDTO updateIngredient(@PathVariable Long id,
                                          @Valid @RequestBody IngredientDTO dto) {
        return ingredientService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
