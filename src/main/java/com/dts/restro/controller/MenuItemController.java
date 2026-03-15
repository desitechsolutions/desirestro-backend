package com.dts.restro.controller;

import com.dts.restro.dto.menu.MenuItemDTO;
import com.dts.restro.service.MenuItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/items")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public List<MenuItemDTO> getAllMenuItems() {
        return menuItemService.getAllMenuItems();
    }

    @PostMapping
    public MenuItemDTO createMenuItem(@RequestBody MenuItemDTO dto) {
        return menuItemService.createMenuItem(dto);
    }

    @PutMapping("/{id}")
    public MenuItemDTO updateMenuItem(@PathVariable Long id, @RequestBody MenuItemDTO dto) {
        return menuItemService.updateMenuItem(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
