package com.dts.restro.mapper;

import com.dts.restro.dto.LeaveDTO;
import com.dts.restro.entity.Leave;
import com.dts.restro.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LeaveMapper {

    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    LeaveDTO toDTO(Leave leave);

    @Mapping(target = "staff", source = "staffId", qualifiedByName = "mapStaff")
    @Mapping(target = "status", ignore = true)        // will be set in service
    @Mapping(target = "appliedDate", ignore = true)   // will be set in service
    @Mapping(target = "approvedDate", ignore = true)
    Leave toEntity(LeaveDTO dto);

    @Named("mapStaff")
    default Staff mapStaff(Long staffId) {
        if (staffId == null) return null;
        Staff staff = new Staff();
        staff.setId(staffId);
        return staff;
    }
}
