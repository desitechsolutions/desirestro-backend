package com.dts.restro.staff.mapper;

import com.dts.restro.staff.dto.StaffDTO;
import com.dts.restro.staff.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    /* ============================
       ENTITY → DTO
       ============================ */

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.role", target = "role")
    StaffDTO toDTO(Staff staff);

    /* ============================
       DTO → ENTITY
       ============================ */

    // ❗ We IGNORE user here because User is created separately in service
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "attendances", ignore = true)
    @Mapping(target = "leaves", ignore = true)
    Staff toEntity(StaffDTO dto);

    List<StaffDTO> toDtoList(List<Staff> staffList);
}
