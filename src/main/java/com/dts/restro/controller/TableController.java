package com.dts.restro.controller;

import com.dts.restro.entity.RestaurantTable;
import com.dts.restro.service.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:3000")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<RestaurantTable> getAllTables(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return tableService.getTablesByStatus(status);
        }
        return tableService.getAllTables();
    }

    @PostMapping
    public RestaurantTable createTable(@RequestBody RestaurantTable table) {
        return tableService.createTable(table);
    }

    @PutMapping("/{id}")
    public RestaurantTable updateTable(@PathVariable Long id, @RequestBody RestaurantTable updated) {
        return tableService.updateTable(id, updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}