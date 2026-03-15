package com.dts.restro.inventory.controller;

import com.dts.restro.inventory.entity.Ingredient;
import com.dts.restro.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/low-stock")
    public List<Ingredient> getLowStockIngredients() {
        return inventoryService.getLowStock();
    }

    @PatchMapping("/ingredients/{id}/restock")
    public ResponseEntity<Ingredient> restockIngredient(
            @PathVariable Long id,
            @RequestBody RestockRequest request) {
        Ingredient updated = inventoryService.restock(id, request.quantity());
        return ResponseEntity.ok(updated);
    }
}

record RestockRequest(double quantity) {}
