package com.dts.restro.restaurant.service;

import com.dts.restro.common.TenantContext;
import com.dts.restro.restaurant.dto.RestaurantResponse;
import com.dts.restro.restaurant.dto.RestaurantUpdateRequest;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing the authenticated restaurant owner's restaurant profile.
 */
@Service
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getCurrentRestaurant() {
        Long id = TenantContext.getCurrentRestaurantId();
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return toResponse(r);
    }

    public RestaurantResponse updateCurrentRestaurant(RestaurantUpdateRequest req) {
        Long id = TenantContext.getCurrentRestaurantId();
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (req.getName() != null)    r.setName(req.getName());
        if (req.getAddress() != null) r.setAddress(req.getAddress());
        if (req.getPhone() != null)   r.setPhone(req.getPhone());
        if (req.getEmail() != null)   r.setEmail(req.getEmail());
        if (req.getState() != null)   r.setState(req.getState());
        if (req.getGstin() != null)   r.setGstin(req.getGstin());
        if (req.getGstRate() != null) r.setGstRate(req.getGstRate());

        return toResponse(restaurantRepository.save(r));
    }

    private RestaurantResponse toResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .phone(r.getPhone())
                .email(r.getEmail())
                .state(r.getState())
                .gstin(r.getGstin())
                .gstRate(r.getGstRate())
                .code(r.getCode())
                .active(r.isActive())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
