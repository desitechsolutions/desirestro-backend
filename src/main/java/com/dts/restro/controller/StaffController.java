package com.dts.restro.controller;

import com.dts.restro.dto.AttendanceDTO;
import com.dts.restro.dto.LeaveDTO;
import com.dts.restro.dto.StaffDTO;
import com.dts.restro.entity.Attendance;
import com.dts.restro.entity.Leave;
import com.dts.restro.entity.Staff;
import com.dts.restro.service.StaffService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:3000")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<StaffDTO> getAllStaff() {
        return staffService.getAllStaff();
    }

    @PostMapping
    public StaffDTO createStaff(@RequestBody StaffDTO staff) {
        return staffService.createStaff(staff);
    }

    @PostMapping("/{id}/clock-in")
    public AttendanceDTO clockIn(@PathVariable Long id) {
        StaffDTO staff = staffService.getStaffById(id); // Add this method
        return staffService.clockIn(staff);
    }

    @PostMapping("/{id}/clock-out")
    public AttendanceDTO clockOut(@PathVariable Long id) {
        return staffService.clockOut(id);
    }


    @GetMapping("/attendance/today")
    public List<AttendanceDTO> getTodayAttendance() {
        return staffService.getTodayAttendance();
    }

    @GetMapping("/leaves/pending")
    public List<LeaveDTO> getPendingLeaves() {
        return staffService.getPendingLeaves();
    }

    @PostMapping("/leaves")
    public LeaveDTO applyLeave(@RequestBody LeaveDTO leave) {
        return staffService.applyLeave(leave);
    }

    @PatchMapping("/leaves/{id}/approve")
    public LeaveDTO approveLeave(@PathVariable Long id) {
        return staffService.approveLeave(id);
    }

    @PatchMapping("/leaves/{id}/reject")
    public LeaveDTO rejectLeave(@PathVariable Long id) {
        return staffService.rejectLeave(id);
    }
}