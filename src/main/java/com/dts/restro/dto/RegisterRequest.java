package com.dts.restro.dto;

import com.dts.restro.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    private Role role = Role.CAPTAIN; // default
}