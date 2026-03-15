package com.dts.restro.auth.dto;

import com.dts.restro.auth.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Unified registration request.
 * When {@code restaurantName} is provided a new restaurant tenant is created
 * and the user is assigned the OWNER role.  When {@code restaurantName} is
 * absent the request is treated as an invitation-style staff registration
 * (handled internally by the owner/admin).
 */
@Data
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    /** Leave null when registering staff — OWNER is assigned automatically for restaurant registration. */
    private Role role = Role.CAPTAIN;

    // ── Restaurant fields (required for new restaurant registration) ──────

    /** Name of the new restaurant. Providing this triggers restaurant tenant creation. */
    @Size(max = 150)
    private String restaurantName;

    @Size(max = 255)
    private String restaurantAddress;

    @Size(max = 20)
    private String restaurantPhone;

    @Size(max = 150)
    private String restaurantEmail;

    @Size(max = 100)
    private String restaurantState;

    @Size(max = 20)
    private String gstin;
}
