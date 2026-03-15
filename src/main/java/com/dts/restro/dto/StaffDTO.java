package com.dts.restro.dto;

import com.dts.restro.entity.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StaffDTO {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private Role role;
    private LocalDate joinDate;
}
