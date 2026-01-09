package com.dts.restro.service;

import com.dts.restro.dto.AttendanceDTO;
import com.dts.restro.dto.LeaveDTO;
import com.dts.restro.dto.StaffDTO;
import com.dts.restro.entity.*;
import com.dts.restro.enums.LeaveStatus;
import com.dts.restro.mapper.AttendanceMapper;
import com.dts.restro.mapper.LeaveMapper;
import com.dts.restro.mapper.StaffMapper;
import com.dts.restro.repository.AttendanceRepository;
import com.dts.restro.repository.LeaveRepository;
import com.dts.restro.repository.StaffRepository;
import com.dts.restro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final StaffMapper staffMapper;
    private final AttendanceMapper attendanceMapper;

    @Autowired
    private LeaveMapper leaveMapper;
    @Autowired
    private UserRepository userRepository;

    public StaffService(StaffRepository staffRepository, AttendanceRepository attendanceRepository, LeaveRepository leaveRepository, StaffMapper staffMapper
    ,AttendanceMapper attendanceMapper) {
        this.staffRepository = staffRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.staffMapper = staffMapper;
        this.attendanceMapper = attendanceMapper;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StaffDTO createStaff(StaffDTO dto) {

        // 0️⃣ Validate
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 1️⃣ Create USER
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode("staff@123")) // TODO: improve later
                .fullName(dto.getFullName())
                .role(dto.getRole() != null ? dto.getRole() : Role.STAFF)
                .build();

        user = userRepository.save(user);

        // 2️⃣ Create STAFF
        Staff staff = staffMapper.toEntity(dto);
        staff.setUser(user);
        staff.setJoinDate(LocalDate.now());

        staff = staffRepository.save(staff);

        return staffMapper.toDTO(staff);
    }

    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAllByOrderByJoinDateDesc().stream()
                .map(staffMapper::toDTO)
                .toList();
    }

    public AttendanceDTO clockIn(StaffDTO staffDTO) {
        Attendance attendance = new Attendance();
        Staff staff = staffRepository.findById(staffDTO.getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        attendance.setStaff(staff);
        attendance.setDate(LocalDate.now());
        attendance.setClockIn(LocalDateTime.now());
        return attendanceMapper.toDTO(attendanceRepository.save(attendance));
    }

    public AttendanceDTO clockOut(Long staffId) {
        Attendance active = attendanceRepository
                .findByStaffIdAndDateAndClockOutIsNull(staffId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No active attendance for today"));

        active.setClockOut(LocalDateTime.now());
        return attendanceMapper.toDTO(attendanceRepository.save(active));
    }


    public List<AttendanceDTO> getTodayAttendance() {
        List<Attendance> today = attendanceRepository.findByDate(LocalDate.now());
        return today.stream()
                .map(attendanceMapper::toDTO)
                .collect(Collectors.toList());
    }


    public List<LeaveDTO> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING).stream()
                .map(leaveMapper::toDTO).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public LeaveDTO applyLeave(LeaveDTO leaveDTO) {
        Staff staff = staffRepository.findById(leaveDTO.getStaffId())
                .orElseThrow(() -> new RuntimeException("No Staff found"));

        Leave leave = new Leave();
        leave.setStaff(staff);
        leave.setFromDate(leaveDTO.getFromDate());
        leave.setToDate(leaveDTO.getToDate());
        leave.setReason(leaveDTO.getReason());
        leave.setAppliedDate(LocalDate.now());
        leave.setStatus(LeaveStatus.PENDING);

        Leave savedLeave = leaveRepository.save(leave);
        return leaveMapper.toDTO(savedLeave);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public LeaveDTO approveLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedDate(LocalDate.now());
        return leaveMapper.toDTO(leaveRepository.save(leave));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public LeaveDTO rejectLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveStatus.REJECTED);
        return leaveMapper.toDTO(leaveRepository.save(leave));
    }

    public StaffDTO getStaffById(Long id) {
        return staffMapper.toDTO(staffRepository.findById(id).orElseThrow(() -> new RuntimeException("No Staff Found")));
    }
}