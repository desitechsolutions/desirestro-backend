package com.dts.restro.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for restaurant owner self-registration.
 * This is combined with the user registration fields in {@link com.dts.restro.dto.RegisterRequest}.
 */
@Data
public class RestaurantRegistrationRequest {

    @NotBlank
    @Size(max = 150)
    private String restaurantName;

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
}
