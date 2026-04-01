package com.dts.restro.staff.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import com.dts.restro.staff.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "leave_request")
@Data
@EqualsAndHashCode(callSuper = false)
public class Leave extends RestaurantAwareEntity {

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    private LocalDate appliedDate;
    private LocalDate approvedDate;
}