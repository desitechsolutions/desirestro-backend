package com.dts.restro.service;

import com.dts.restro.entity.RestaurantTable;
import com.dts.restro.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableService {
    private final RestaurantTableRepository tableRepository;

    public TableService(RestaurantTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public RestaurantTable createTable(RestaurantTable table) {
        // Validate unique tableNumber
        if (tableRepository.existsByTableNumber(table.getTableNumber())) {
            throw new RuntimeException("Table number already exists");
        }
        return tableRepository.save(table);
    }

    public RestaurantTable updateTable(Long id, RestaurantTable updated) {
        RestaurantTable t = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        t.setTableNumber(updated.getTableNumber());
        t.setCapacity(updated.getCapacity());
        return tableRepository.save(t);
    }

    public void deleteTable(Long id) {
        RestaurantTable t = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        if (!t.getStatus().equals("EMPTY")) {
            throw new RuntimeException("Cannot delete occupied table");
        }
        tableRepository.deleteById(id);
    }

    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAllByOrderByTableNumberAsc();
    }

    public List<RestaurantTable> getTablesByStatus(String status) {
        return tableRepository.findByStatus(status.toUpperCase());
    }

    public RestaurantTable updateTableStatus(Long id, String status) {
        RestaurantTable t = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        t.setStatus(status.toUpperCase());
        return tableRepository.save(t);
    }
}