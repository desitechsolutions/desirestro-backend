package com.dts.restro.mapper;

import com.dts.restro.dto.LeaveDTO;
import com.dts.restro.entity.Leave;
import com.dts.restro.entity.Staff;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T10:42:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class LeaveMapperImpl implements LeaveMapper {

    @Override
    public LeaveDTO toDTO(Leave leave) {
        if ( leave == null ) {
            return null;
        }

        LeaveDTO leaveDTO = new LeaveDTO();

        leaveDTO.setStaffId( leaveStaffId( leave ) );
        leaveDTO.setStaffName( leaveStaffFullName( leave ) );
        leaveDTO.setId( leave.getId() );
        leaveDTO.setFromDate( leave.getFromDate() );
        leaveDTO.setToDate( leave.getToDate() );
        leaveDTO.setReason( leave.getReason() );
        leaveDTO.setStatus( leave.getStatus() );
        leaveDTO.setAppliedDate( leave.getAppliedDate() );
        leaveDTO.setApprovedDate( leave.getApprovedDate() );

        return leaveDTO;
    }

    @Override
    public Leave toEntity(LeaveDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Leave leave = new Leave();

        leave.setStaff( mapStaff( dto.getStaffId() ) );
        leave.setId( dto.getId() );
        leave.setFromDate( dto.getFromDate() );
        leave.setToDate( dto.getToDate() );
        leave.setReason( dto.getReason() );

        return leave;
    }

    private Long leaveStaffId(Leave leave) {
        Staff staff = leave.getStaff();
        if ( staff == null ) {
            return null;
        }
        return staff.getId();
    }

    private String leaveStaffFullName(Leave leave) {
        Staff staff = leave.getStaff();
        if ( staff == null ) {
            return null;
        }
        return staff.getFullName();
    }
}
