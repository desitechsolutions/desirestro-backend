package com.dts.restro.restaurant.controller;

import com.dts.restro.common.ApiResponse;
import com.dts.restro.restaurant.dto.RestaurantResponse;
import com.dts.restro.restaurant.dto.RestaurantUpdateRequest;
import com.dts.restro.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for the authenticated restaurant owner to view and update
 * their restaurant profile.  All endpoints are scoped to the tenant derived
 * from the JWT, so owners can only see/modify their own restaurant.
 */
@RestController
@RequestMapping("/api/restaurant")
@Tag(name = "Restaurant", description = "Restaurant profile management (owner only)")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Get current restaurant profile")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurant() {
        return ResponseEntity.ok(ApiResponse.success("Restaurant fetched", restaurantService.getCurrentRestaurant()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Update restaurant profile")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @Valid @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated",
                restaurantService.updateCurrentRestaurant(request)));
    }
}
