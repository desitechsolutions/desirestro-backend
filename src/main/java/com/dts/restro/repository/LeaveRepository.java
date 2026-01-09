// src/main/java/com/dts/restro/repository/LeaveRepository.java

package com.dts.restro.repository;

import com.dts.restro.entity.Leave;
import com.dts.restro.entity.Staff;
import com.dts.restro.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByStaffOrderByAppliedDateDesc(Staff staff);
}