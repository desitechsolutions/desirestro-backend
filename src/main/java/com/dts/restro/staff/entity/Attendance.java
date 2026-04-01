package com.dts.restro.staff.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "attendance")
@Data
@EqualsAndHashCode(callSuper = false)
public class Attendance extends RestaurantAwareEntity {

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    @Transient
    public double getHoursWorked() {
        if (clockIn == null || clockOut == null) return 0;
        return Duration.between(clockIn, clockOut).toHours()
                + Duration.between(clockIn, clockOut).toMinutes() / 60.0;
    }
}