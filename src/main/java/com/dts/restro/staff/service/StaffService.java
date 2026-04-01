package com.dts.restro.staff.service;

import com.dts.restro.audit.service.AuditService;
import com.dts.restro.auth.entity.Role;
import com.dts.restro.auth.entity.User;
import com.dts.restro.auth.repository.UserRepository;
import com.dts.restro.common.TenantContext;
import com.dts.restro.exception.BusinessValidationException;
import com.dts.restro.exception.DuplicateResourceException;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.restaurant.repository.RestaurantRepository;
import com.dts.restro.staff.dto.AttendanceDTO;
import com.dts.restro.staff.dto.LeaveDTO;
import com.dts.restro.staff.dto.StaffDTO;
import com.dts.restro.staff.entity.Attendance;
import com.dts.restro.staff.entity.Leave;
import com.dts.restro.staff.entity.Staff;
import com.dts.restro.staff.enums.LeaveStatus;
import com.dts.restro.staff.mapper.AttendanceMapper;
import com.dts.restro.staff.mapper.LeaveMapper;
import com.dts.restro.staff.mapper.StaffMapper;
import com.dts.restro.staff.repository.AttendanceRepository;
import com.dts.restro.staff.repository.LeaveRepository;
import com.dts.restro.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditService auditService;
    private final StaffMapper staffMapper;
    private final AttendanceMapper attendanceMapper;
    private final LeaveMapper leaveMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Create new staff member
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StaffDTO createStaff(StaffDTO dto) {
        log.info("Creating staff member: {}", dto.getUsername());
        
        // Validate input
        validateStaffDTO(dto);
        
        // Get current restaurant
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        if (restaurantId == null) {
            throw new BusinessValidationException("Restaurant context not set");
        }
        
        // Check for duplicate username
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }
        
        // Get restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        // Create user account
        String generatedPassword = generateRandomPassword();
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(generatedPassword))
                .fullName(dto.getFullName())
                .role(dto.getRole() != null ? dto.getRole() : Role.STAFF)
                .restaurant(restaurant)
                .build();
        
        user = userRepository.save(user);
        log.info("User account created for staff: {}", user.getUsername());
        
        // Create staff record
        Staff staff = staffMapper.toEntity(dto);
        staff.setUser(user);
        staff.setJoinDate(LocalDate.now());
        staff.setRestaurantId(restaurantId);
        
        staff = staffRepository.save(staff);
        log.info("Staff member created with ID: {}", staff.getId());
        
        // Audit log
        auditService.logStaffCreate(restaurantId, staff.getId(), staff.getFullName(), 
            staff.getUser().getRole().name());
        
        StaffDTO result = staffMapper.toDTO(staff);
        // Note: In production, send the generated password via email/SMS
        log.warn("Generated password for {}: {} (Send this securely to the user)", 
            user.getUsername(), generatedPassword);
        
        return result;
    }

    /**
     * Get all staff members for current restaurant
     */
    @Transactional(readOnly = true)
    public List<StaffDTO> getAllStaff() {
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        if (restaurantId == null) {
            throw new BusinessValidationException("Restaurant context not set");
        }
        
        log.info("Fetching all staff for restaurant: {}", restaurantId);
        return staffRepository.findAllByRestaurantIdOrderByJoinDateDesc(restaurantId).stream()
                .map(staffMapper::toDTO)
                .toList();
    }

    /**
     * Get staff by ID
     */
    @Transactional(readOnly = true)
    public StaffDTO getStaffById(Long id) {
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(id, restaurantId);
        
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        
        return staffMapper.toDTO(staff);
    }

    /**
     * Update staff member
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StaffDTO updateStaff(Long id, StaffDTO dto) {
        log.info("Updating staff member: {}", id);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(id, restaurantId);
        
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        
        // Store old values for audit
        String oldFullName = staff.getFullName();
        String oldPhone = staff.getPhone();
        String oldEmail = staff.getEmail();
        Role oldRole = staff.getUser().getRole();
        
        // Update staff details
        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            staff.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) {
            staff.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            staff.setEmail(dto.getEmail());
        }
        
        // Update user role if changed
        if (dto.getRole() != null && dto.getRole() != oldRole) {
            staff.getUser().setRole(dto.getRole());
            staff.getUser().setFullName(dto.getFullName());
            userRepository.save(staff.getUser());
            log.info("Updated role for staff {} from {} to {}", id, oldRole, dto.getRole());
        }
        
        staff = staffRepository.save(staff);
        
        // Audit log
        String oldValue = String.format("Name: %s, Phone: %s, Email: %s, Role: %s", 
            oldFullName, oldPhone, oldEmail, oldRole);
        String newValue = String.format("Name: %s, Phone: %s, Email: %s, Role: %s", 
            staff.getFullName(), staff.getPhone(), staff.getEmail(), staff.getUser().getRole());
        auditService.logStaffUpdate(restaurantId, id, staff.getFullName(), oldValue, newValue);
        
        return staffMapper.toDTO(staff);
    }

    /**
     * Delete staff member
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteStaff(Long id) {
        log.info("Deleting staff member: {}", id);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(id, restaurantId);
        
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        
        String staffName = staff.getFullName();
        
        // Delete staff (cascade will handle attendance and leaves)
        staffRepository.delete(staff);
        log.info("Staff member deleted: {}", staffName);
        
        // Audit log
        auditService.logStaffDelete(restaurantId, id, staffName);
    }

    /**
     * Clock in staff member
     */
    @Transactional
    public AttendanceDTO clockIn(Long staffId) {
        log.info("Clock-in for staff: {}", staffId);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(staffId, restaurantId);
        
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        
        // Check if already clocked in today
        long activeCount = attendanceRepository.countActiveAttendanceForToday(staffId, LocalDate.now());
        if (activeCount > 0) {
            throw new BusinessValidationException("Staff member is already clocked in for today");
        }
        
        Attendance attendance = new Attendance();
        attendance.setStaff(staff);
        attendance.setDate(LocalDate.now());
        attendance.setClockIn(LocalDateTime.now());
        attendance.setRestaurantId(restaurantId);
        
        attendance = attendanceRepository.save(attendance);
        log.info("Clock-in recorded for staff: {} at {}", staff.getFullName(), attendance.getClockIn());
        
        // Audit log
        auditService.logAttendanceClockIn(restaurantId, attendance.getId(), staff.getFullName());
        
        return attendanceMapper.toDTO(attendance);
    }

    /**
     * Clock out staff member
     */
    @Transactional
    public AttendanceDTO clockOut(Long staffId) {
        log.info("Clock-out for staff: {}", staffId);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(staffId, restaurantId);
        
        Attendance active = attendanceRepository
                .findByStaffIdAndDateAndClockOutIsNull(staffId, LocalDate.now())
                .orElseThrow(() -> new BusinessValidationException(
                    "No active attendance found for today. Please clock in first."));
        
        active.setClockOut(LocalDateTime.now());
        active = attendanceRepository.save(active);
        
        double hoursWorked = active.getHoursWorked();
        log.info("Clock-out recorded for staff: {} at {} (Hours worked: {})", 
            active.getStaff().getFullName(), active.getClockOut(), hoursWorked);
        
        // Audit log
        auditService.logAttendanceClockOut(restaurantId, active.getId(), 
            active.getStaff().getFullName(), hoursWorked);
        
        return attendanceMapper.toDTO(active);
    }

    /**
     * Get today's attendance
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getTodayAttendance() {
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        if (restaurantId == null) {
            throw new BusinessValidationException("Restaurant context not set");
        }
        
        List<Attendance> today = attendanceRepository.findByRestaurantIdAndDate(restaurantId, LocalDate.now());
        return today.stream()
                .map(attendanceMapper::toDTO)
                .toList();
    }

    /**
     * Apply for leave
     */
    @Transactional
    public LeaveDTO applyLeave(LeaveDTO leaveDTO) {
        log.info("Applying leave for staff: {}", leaveDTO.getStaffId());
        
        // Validate leave request
        validateLeaveRequest(leaveDTO);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(leaveDTO.getStaffId(), restaurantId);
        
        Staff staff = staffRepository.findById(leaveDTO.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + leaveDTO.getStaffId()));
        
        Leave leave = new Leave();
        leave.setStaff(staff);
        leave.setFromDate(leaveDTO.getFromDate());
        leave.setToDate(leaveDTO.getToDate());
        leave.setReason(leaveDTO.getReason());
        leave.setAppliedDate(LocalDate.now());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setRestaurantId(restaurantId);
        
        leave = leaveRepository.save(leave);
        log.info("Leave application submitted for staff: {} from {} to {}", 
            staff.getFullName(), leave.getFromDate(), leave.getToDate());
        
        return leaveMapper.toDTO(leave);
    }

    /**
     * Get pending leave requests
     */
    @Transactional(readOnly = true)
    public List<LeaveDTO> getPendingLeaves() {
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        if (restaurantId == null) {
            throw new BusinessValidationException("Restaurant context not set");
        }
        
        return leaveRepository.findByRestaurantIdAndStatus(restaurantId, LeaveStatus.PENDING).stream()
                .map(leaveMapper::toDTO)
                .toList();
    }

    /**
     * Approve leave request
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveDTO approveLeave(Long leaveId) {
        log.info("Approving leave: {}", leaveId);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));
        
        if (!leave.getRestaurantId().equals(restaurantId)) {
            throw new BusinessValidationException("Access denied: Leave belongs to different restaurant");
        }
        
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessValidationException("Leave is not in pending status");
        }
        
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedDate(LocalDate.now());
        leave = leaveRepository.save(leave);
        
        String period = leave.getFromDate() + " to " + leave.getToDate();
        log.info("Leave approved for staff: {} ({})", leave.getStaff().getFullName(), period);
        
        // Audit log
        auditService.logLeaveApprove(restaurantId, leaveId, leave.getStaff().getFullName(), period);
        
        return leaveMapper.toDTO(leave);
    }

    /**
     * Reject leave request
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveDTO rejectLeave(Long leaveId) {
        log.info("Rejecting leave: {}", leaveId);
        
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));
        
        if (!leave.getRestaurantId().equals(restaurantId)) {
            throw new BusinessValidationException("Access denied: Leave belongs to different restaurant");
        }
        
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessValidationException("Leave is not in pending status");
        }
        
        leave.setStatus(LeaveStatus.REJECTED);
        leave = leaveRepository.save(leave);
        
        String period = leave.getFromDate() + " to " + leave.getToDate();
        log.info("Leave rejected for staff: {} ({})", leave.getStaff().getFullName(), period);
        
        // Audit log
        auditService.logLeaveReject(restaurantId, leaveId, leave.getStaff().getFullName(), period);
        
        return leaveMapper.toDTO(leave);
    }

    /**
     * Get leaves by staff
     */
    @Transactional(readOnly = true)
    public List<LeaveDTO> getLeavesByStaff(Long staffId) {
        Long restaurantId = TenantContext.getCurrentRestaurantId();
        validateRestaurantAccess(staffId, restaurantId);
        
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        
        return leaveRepository.findByStaffOrderByAppliedDateDesc(staff).stream()
                .map(leaveMapper::toDTO)
                .toList();
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validate staff DTO
     */
    private void validateStaffDTO(StaffDTO dto) {
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new BusinessValidationException("Full name is required");
        }
        
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BusinessValidationException("Username is required");
        }
        
        if (dto.getUsername().length() < 3) {
            throw new BusinessValidationException("Username must be at least 3 characters long");
        }
        
        if (dto.getUsername().length() > 50) {
            throw new BusinessValidationException("Username must not exceed 50 characters");
        }
        
        // Validate username format (alphanumeric and underscore only)
        if (!dto.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessValidationException("Username can only contain letters, numbers, and underscores");
        }
    }

    /**
     * Validate leave request
     */
    private void validateLeaveRequest(LeaveDTO dto) {
        if (dto.getStaffId() == null) {
            throw new BusinessValidationException("Staff ID is required");
        }
        
        if (dto.getFromDate() == null || dto.getToDate() == null) {
            throw new BusinessValidationException("From date and to date are required");
        }
        
        if (dto.getFromDate().isAfter(dto.getToDate())) {
            throw new BusinessValidationException("From date must be before or equal to to date");
        }
        
        if (dto.getFromDate().isBefore(LocalDate.now())) {
            throw new BusinessValidationException("Cannot apply leave for past dates");
        }
        
        // Check for overlapping leaves
        List<Leave> overlapping = leaveRepository.findOverlappingLeaves(
            dto.getStaffId(), dto.getFromDate(), dto.getToDate());
        
        if (!overlapping.isEmpty()) {
            throw new BusinessValidationException(
                "Leave request overlaps with existing leave from " + 
                overlapping.get(0).getFromDate() + " to " + overlapping.get(0).getToDate());
        }
    }

    /**
     * Validate restaurant access
     */
    private void validateRestaurantAccess(Long staffId, Long restaurantId) {
        if (restaurantId == null) {
            throw new BusinessValidationException("Restaurant context not set");
        }
        
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        
        if (!staff.getRestaurantId().equals(restaurantId)) {
            throw new BusinessValidationException("Access denied: Staff belongs to different restaurant");
        }
    }

    /**
     * Generate random password
     */
    private String generateRandomPassword() {
        // Generate a secure random password
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "Staff@" + uuid.substring(0, 8);
    }
}

// Made with Bob
