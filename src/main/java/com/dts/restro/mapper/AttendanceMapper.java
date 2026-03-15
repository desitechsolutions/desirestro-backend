package com.dts.restro.mapper;

import com.dts.restro.dto.AttendanceDTO;
import com.dts.restro.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(source = "staff.fullName", target = "staffName") // Map staff fullName
    AttendanceDTO toDTO(Attendance attendance);

    // Custom method to compute hours worked
    @Named("computeHoursWorked")
    default double computeHoursWorked(Attendance attendance) {
        if (attendance.getClockIn() == null || attendance.getClockOut() == null) return 0;
        return attendance.getHoursWorked();
    }
}
