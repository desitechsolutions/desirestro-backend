package com.dts.restro.staff.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceDTO {
    private Long id;
    private String staffName;       // Only name, no recursive staff object
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private double hoursWorked;     // Computed
}
