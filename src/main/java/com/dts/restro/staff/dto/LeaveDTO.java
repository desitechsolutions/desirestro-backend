package com.dts.restro.staff.dto;

import com.dts.restro.staff.enums.LeaveStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveDTO {
    private Long id;          // optional for existing leave
    private Long staffId;
    private String staffName; // optional, for UI display
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private LeaveStatus status;
    private LocalDate appliedDate;
    private LocalDate approvedDate;
}
