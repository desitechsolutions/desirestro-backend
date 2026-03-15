package com.dts.restro.mapper;

import com.dts.restro.dto.AttendanceDTO;
import com.dts.restro.entity.Attendance;
import com.dts.restro.entity.Staff;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T10:42:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AttendanceMapperImpl implements AttendanceMapper {

    @Override
    public AttendanceDTO toDTO(Attendance attendance) {
        if ( attendance == null ) {
            return null;
        }

        AttendanceDTO attendanceDTO = new AttendanceDTO();

        attendanceDTO.setStaffName( attendanceStaffFullName( attendance ) );
        attendanceDTO.setId( attendance.getId() );
        attendanceDTO.setDate( attendance.getDate() );
        attendanceDTO.setClockIn( attendance.getClockIn() );
        attendanceDTO.setClockOut( attendance.getClockOut() );
        attendanceDTO.setHoursWorked( attendance.getHoursWorked() );

        return attendanceDTO;
    }

    private String attendanceStaffFullName(Attendance attendance) {
        Staff staff = attendance.getStaff();
        if ( staff == null ) {
            return null;
        }
        return staff.getFullName();
    }
}
