package com.dts.restro.restaurant.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Public view of a restaurant returned after registration or profile lookup. */
@Data
@Builder
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String state;
    private String gstin;
    private double gstRate;
    private String code;
    private boolean active;
    private LocalDateTime createdAt;
}
