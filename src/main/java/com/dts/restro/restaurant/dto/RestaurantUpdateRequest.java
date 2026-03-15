package com.dts.restro.restaurant.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Partial update DTO for restaurant profile — only non-null fields are applied. */
@Data
public class RestaurantUpdateRequest {

    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 150)
    private String email;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String gstin;

    private Double gstRate;
}
