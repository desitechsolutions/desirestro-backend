package com.dts.restro.mapper;

import com.dts.restro.dto.StaffDTO;
import com.dts.restro.entity.Role;
import com.dts.restro.entity.Staff;
import com.dts.restro.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T10:42:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class StaffMapperImpl implements StaffMapper {

    @Override
    public StaffDTO toDTO(Staff staff) {
        if ( staff == null ) {
            return null;
        }

        StaffDTO staffDTO = new StaffDTO();

        staffDTO.setUserId( staffUserId( staff ) );
        staffDTO.setUsername( staffUserUsername( staff ) );
        staffDTO.setRole( staffUserRole( staff ) );
        staffDTO.setJoinDate( staff.getJoinDate() );
        staffDTO.setId( staff.getId() );
        staffDTO.setFullName( staff.getFullName() );
        staffDTO.setPhone( staff.getPhone() );
        staffDTO.setEmail( staff.getEmail() );

        return staffDTO;
    }

    @Override
    public Staff toEntity(StaffDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Staff staff = new Staff();

        staff.setId( dto.getId() );
        staff.setFullName( dto.getFullName() );
        staff.setPhone( dto.getPhone() );
        staff.setEmail( dto.getEmail() );
        staff.setJoinDate( dto.getJoinDate() );

        return staff;
    }

    @Override
    public List<StaffDTO> toDtoList(List<Staff> staffList) {
        if ( staffList == null ) {
            return null;
        }

        List<StaffDTO> list = new ArrayList<StaffDTO>( staffList.size() );
        for ( Staff staff : staffList ) {
            list.add( toDTO( staff ) );
        }

        return list;
    }

    private Long staffUserId(Staff staff) {
        User user = staff.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private String staffUserUsername(Staff staff) {
        User user = staff.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUsername();
    }

    private Role staffUserRole(Staff staff) {
        User user = staff.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getRole();
    }
}
