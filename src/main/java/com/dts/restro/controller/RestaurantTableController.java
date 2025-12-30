package com.dts.restro.controller;

import com.dts.restro.entity.RestaurantTable;
import com.dts.restro.repository.RestaurantTableRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantTableController {

    private final RestaurantTableRepository restaurantTableRepository;

    public RestaurantTableController(RestaurantTableRepository restaurantTableRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @GetMapping
    public List<RestaurantTable> getAllTables() {
        return restaurantTableRepository.findAllByOrderByTableNumberAsc();
    }

    // We'll add update status later when sending KOT
}