package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    // Computed field
    @Transient
    public double getHoursWorked() {
        if (clockIn == null || clockOut == null) return 0;
        return Duration.between(clockIn, clockOut).toHours() + Duration.between(clockIn, clockOut).toMinutes() / 60.0;
    }
}