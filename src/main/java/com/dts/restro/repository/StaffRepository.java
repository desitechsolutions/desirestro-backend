package com.dts.restro.repository;

import com.dts.restro.dto.StaffDTO;
import com.dts.restro.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findAllByOrderByJoinDateDesc();
}