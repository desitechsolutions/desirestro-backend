// src/main/java/com/dts/restro/repository/AttendanceRepository.java

package com.dts.restro.repository;

import com.dts.restro.entity.Attendance;
import com.dts.restro.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByStaffOrderByDateDesc(Staff staff);

    @Query("SELECT a FROM Attendance a WHERE a.date = :date AND a.clockOut IS NULL")
    List<Attendance> findActiveAttendance(@Param("date") LocalDate date);

    Optional<Attendance> findByStaffIdAndDateAndClockOutIsNull(Long staffId, LocalDate date);
}