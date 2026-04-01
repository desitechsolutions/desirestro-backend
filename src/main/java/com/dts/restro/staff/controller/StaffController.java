package com.dts.restro.staff.controller;

import com.dts.restro.common.ApiResponse;
import com.dts.restro.staff.dto.AttendanceDTO;
import com.dts.restro.staff.dto.LeaveDTO;
import com.dts.restro.staff.dto.StaffDTO;
import com.dts.restro.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Staff Management", description = "Staff, attendance, and leave management APIs")
public class StaffController {

    private final StaffService staffService;

    /**
     * Get all staff members
     */
    @GetMapping
    @Operation(summary = "Get all staff", description = "Get all staff members for the current restaurant")
    public ResponseEntity<ApiResponse<List<StaffDTO>>> getAllStaff() {
        log.info("GET /api/staff - Fetching all staff");
        List<StaffDTO> staff = staffService.getAllStaff();
        return ResponseEntity.ok(ApiResponse.success(staff, "Staff list retrieved successfully"));
    }

    /**
     * Create new staff member
     */
    @PostMapping
    @Operation(summary = "Create staff", description = "Create a new staff member with user account")
    public ResponseEntity<ApiResponse<StaffDTO>> createStaff(@Valid @RequestBody StaffDTO staff) {
        log.info("POST /api/staff - Creating staff: {}", staff.getUsername());
        StaffDTO created = staffService.createStaff(staff);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Staff member created successfully"));
    }

    /**
     * Get staff by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get staff by ID", description = "Get staff member details by ID")
    public ResponseEntity<ApiResponse<StaffDTO>> getStaffById(@PathVariable Long id) {
        log.info("GET /api/staff/{} - Fetching staff", id);
        StaffDTO staff = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success(staff, "Staff details retrieved successfully"));
    }

    /**
     * Update staff member
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update staff", description = "Update staff member details")
    public ResponseEntity<ApiResponse<StaffDTO>> updateStaff(
            @PathVariable Long id, 
            @Valid @RequestBody StaffDTO dto) {
        log.info("PUT /api/staff/{} - Updating staff", id);
        StaffDTO updated = staffService.updateStaff(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Staff member updated successfully"));
    }

    /**
     * Delete staff member
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete staff", description = "Delete a staff member")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        log.info("DELETE /api/staff/{} - Deleting staff", id);
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Staff member deleted successfully"));
    }

    /**
     * Get leaves by staff
     */
    @GetMapping("/{id}/leaves")
    @Operation(summary = "Get staff leaves", description = "Get all leave requests for a staff member")
    public ResponseEntity<ApiResponse<List<LeaveDTO>>> getLeavesByStaff(@PathVariable Long id) {
        log.info("GET /api/staff/{}/leaves - Fetching leaves", id);
        List<LeaveDTO> leaves = staffService.getLeavesByStaff(id);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Leave history retrieved successfully"));
    }

    /**
     * Clock in staff member
     */
    @PostMapping("/{id}/clock-in")
    @Operation(summary = "Clock in", description = "Record staff clock-in time")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockIn(@PathVariable Long id) {
        log.info("POST /api/staff/{}/clock-in - Clock in", id);
        AttendanceDTO attendance = staffService.clockIn(id);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Clock-in recorded successfully"));
    }

    /**
     * Clock out staff member
     */
    @PostMapping("/{id}/clock-out")
    @Operation(summary = "Clock out", description = "Record staff clock-out time")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockOut(@PathVariable Long id) {
        log.info("POST /api/staff/{}/clock-out - Clock out", id);
        AttendanceDTO attendance = staffService.clockOut(id);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Clock-out recorded successfully"));
    }

    /**
     * Get today's attendance
     */
    @GetMapping("/attendance/today")
    @Operation(summary = "Get today's attendance", description = "Get attendance records for today")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getTodayAttendance() {
        log.info("GET /api/staff/attendance/today - Fetching today's attendance");
        List<AttendanceDTO> attendance = staffService.getTodayAttendance();
        return ResponseEntity.ok(ApiResponse.success(attendance, "Today's attendance retrieved successfully"));
    }

    /**
     * Get pending leave requests
     */
    @GetMapping("/leaves/pending")
    @Operation(summary = "Get pending leaves", description = "Get all pending leave requests")
    public ResponseEntity<ApiResponse<List<LeaveDTO>>> getPendingLeaves() {
        log.info("GET /api/staff/leaves/pending - Fetching pending leaves");
        List<LeaveDTO> leaves = staffService.getPendingLeaves();
        return ResponseEntity.ok(ApiResponse.success(leaves, "Pending leave requests retrieved successfully"));
    }

    /**
     * Apply for leave
     */
    @PostMapping("/leaves")
    @Operation(summary = "Apply for leave", description = "Submit a new leave request")
    public ResponseEntity<ApiResponse<LeaveDTO>> applyLeave(@Valid @RequestBody LeaveDTO leave) {
        log.info("POST /api/staff/leaves - Applying leave for staff: {}", leave.getStaffId());
        LeaveDTO applied = staffService.applyLeave(leave);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(applied, "Leave request submitted successfully"));
    }

    /**
     * Approve leave request
     */
    @PatchMapping("/leaves/{id}/approve")
    @Operation(summary = "Approve leave", description = "Approve a pending leave request")
    public ResponseEntity<ApiResponse<LeaveDTO>> approveLeave(@PathVariable Long id) {
        log.info("PATCH /api/staff/leaves/{}/approve - Approving leave", id);
        LeaveDTO approved = staffService.approveLeave(id);
        return ResponseEntity.ok(ApiResponse.success(approved, "Leave request approved successfully"));
    }

    /**
     * Reject leave request
     */
    @PatchMapping("/leaves/{id}/reject")
    @Operation(summary = "Reject leave", description = "Reject a pending leave request")
    public ResponseEntity<ApiResponse<LeaveDTO>> rejectLeave(@PathVariable Long id) {
        log.info("PATCH /api/staff/leaves/{}/reject - Rejecting leave", id);
        LeaveDTO rejected = staffService.rejectLeave(id);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Leave request rejected successfully"));
    }
}

// Made with Bob
