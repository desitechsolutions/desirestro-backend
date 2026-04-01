# Staff Management Code Review & Fixes

## Executive Summary

After reviewing the existing staff management code, I've identified several critical issues that need to be addressed to align with the latest multi-tenancy patterns, audit logging, and best practices used in other modules (Customer, Billing, DailySalesSummary).

## Issues Identified

### 1. **Missing Audit Logging Integration** ❌
- **Current**: No audit logging for staff operations
- **Required**: Log all CRUD operations (create, update, delete staff, approve/reject leave)
- **Impact**: No audit trail for staff management activities

### 2. **Inconsistent Exception Handling** ❌
- **Current**: Using generic `RuntimeException`
- **Required**: Use `ResourceNotFoundException`, `BusinessValidationException`, `DuplicateResourceException`
- **Impact**: Poor error messages and inconsistent error handling

### 3. **Missing Validation** ❌
- **Current**: Minimal validation in service layer
- **Required**: Comprehensive validation for all inputs
- **Impact**: Data integrity issues

### 4. **Incomplete Multi-Tenancy** ⚠️
- **Current**: Entities extend `RestaurantAwareEntity` but repositories don't filter by restaurantId
- **Required**: Add restaurantId validation in service methods
- **Impact**: Potential cross-tenant data access

### 5. **Missing API Response Wrapper** ❌
- **Current**: Controller returns raw DTOs
- **Required**: Use `ApiResponse<T>` wrapper for consistency
- **Impact**: Inconsistent API responses

### 6. **Hardcoded Password** ⚠️
- **Current**: Default password "staff@123" hardcoded
- **Required**: Generate random password or allow admin to set it
- **Impact**: Security risk

### 7. **Missing Pagination** ⚠️
- **Current**: `getAllStaff()` returns all records
- **Required**: Add pagination support
- **Impact**: Performance issues with large datasets

### 8. **Incomplete DTO Mapping** ⚠️
- **Current**: Mappers don't handle all fields properly
- **Required**: Complete mapper implementation with restaurantId
- **Impact**: Data loss during mapping

### 9. **Missing Business Logic** ❌
- **Current**: No validation for overlapping leaves, duplicate clock-ins
- **Required**: Add business rules validation
- **Impact**: Data inconsistency

### 10. **Frontend Issues** ⚠️
- **Current**: Basic error handling with alerts
- **Required**: Use Toast notifications, proper error messages
- **Impact**: Poor user experience

## Detailed Fixes Required

### Backend Fixes

#### 1. Update StaffService.java

**Add Audit Logging:**
```java
@Autowired
private AuditService auditService;

// In createStaff method:
auditService.logStaffCreated(staff.getId(), staff.getFullName(), staff.getRole().name());

// In updateStaff method:
auditService.logStaffUpdated(id, staff.getFullName());

// In deleteStaff method:
auditService.logStaffDeleted(id, staff.getFullName());

// In approveLeave method:
auditService.logLeaveApproved(leaveId, leave.getStaff().getFullName());

// In rejectLeave method:
auditService.logLeaveRejected(leaveId, leave.getStaff().getFullName());
```

**Add Exception Handling:**
```java
// Replace RuntimeException with specific exceptions
throw new ResourceNotFoundException("Staff not found with id: " + id);
throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
throw new BusinessValidationException("Cannot clock out without clocking in first");
```

**Add Validation:**
```java
private void validateStaffDTO(StaffDTO dto) {
    if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
        throw new BusinessValidationException("Full name is required");
    }
    if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
        throw new BusinessValidationException("Username is required");
    }
    if (dto.getUsername().length() < 3) {
        throw new BusinessValidationException("Username must be at least 3 characters");
    }
}

private void validateLeaveRequest(LeaveDTO dto) {
    if (dto.getFromDate() == null || dto.getToDate() == null) {
        throw new BusinessValidationException("From date and to date are required");
    }
    if (dto.getFromDate().isAfter(dto.getToDate())) {
        throw new BusinessValidationException("From date must be before to date");
    }
    if (dto.getFromDate().isBefore(LocalDate.now())) {
        throw new BusinessValidationException("Cannot apply leave for past dates");
    }
    
    // Check for overlapping leaves
    List<Leave> overlapping = leaveRepository.findOverlappingLeaves(
        dto.getStaffId(), dto.getFromDate(), dto.getToDate()
    );
    if (!overlapping.isEmpty()) {
        throw new BusinessValidationException("Leave request overlaps with existing leave");
    }
}
```

**Add Multi-Tenancy Validation:**
```java
private void validateRestaurantAccess(Long staffId) {
    Long currentRestaurantId = TenantContext.getCurrentRestaurantId();
    if (currentRestaurantId == null) {
        throw new BusinessValidationException("Restaurant context not set");
    }
    
    Staff staff = staffRepository.findById(staffId)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    
    if (!staff.getRestaurantId().equals(currentRestaurantId)) {
        throw new BusinessValidationException("Access denied: Staff belongs to different restaurant");
    }
}
```

#### 2. Update StaffController.java

**Add ApiResponse Wrapper:**
```java
@GetMapping
public ResponseEntity<ApiResponse<List<StaffDTO>>> getAllStaff() {
    List<StaffDTO> staff = staffService.getAllStaff();
    return ResponseEntity.ok(ApiResponse.success(staff));
}

@PostMapping
public ResponseEntity<ApiResponse<StaffDTO>> createStaff(@Valid @RequestBody StaffDTO staff) {
    StaffDTO created = staffService.createStaff(staff);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(created, "Staff member created successfully"));
}
```

**Add Validation Annotations:**
```java
@PostMapping
public ResponseEntity<ApiResponse<StaffDTO>> createStaff(
    @Valid @RequestBody StaffDTO staff
) {
    // ...
}
```

#### 3. Update StaffRepository.java

**Add Custom Queries:**
```java
@Query("SELECT s FROM Staff s WHERE s.restaurantId = :restaurantId ORDER BY s.joinDate DESC")
List<Staff> findAllByRestaurantId(@Param("restaurantId") Long restaurantId);

@Query("SELECT s FROM Staff s WHERE s.restaurantId = :restaurantId AND s.id = :id")
Optional<Staff> findByIdAndRestaurantId(@Param("id") Long id, @Param("restaurantId") Long restaurantId);
```

#### 4. Update LeaveRepository.java

**Add Overlap Check:**
```java
@Query("SELECT l FROM Leave l WHERE l.staff.id = :staffId " +
       "AND l.status != 'REJECTED' " +
       "AND ((l.fromDate <= :toDate AND l.toDate >= :fromDate))")
List<Leave> findOverlappingLeaves(
    @Param("staffId") Long staffId,
    @Param("fromDate") LocalDate fromDate,
    @Param("toDate") LocalDate toDate
);
```

#### 5. Update AttendanceRepository.java

**Add Today's Active Check:**
```java
@Query("SELECT COUNT(a) FROM Attendance a WHERE a.staff.id = :staffId " +
       "AND a.date = :date AND a.clockOut IS NULL")
long countActiveAttendanceForToday(@Param("staffId") Long staffId, @Param("date") LocalDate date);
```

#### 6. Enhance AuditService.java

**Add Staff-Specific Methods:**
```java
public void logStaffCreated(Long staffId, String staffName, String role) {
    log(AuditAction.STAFF_CREATED, "Staff", staffId, 
        String.format("Created staff: %s (Role: %s)", staffName, role));
}

public void logStaffUpdated(Long staffId, String staffName) {
    log(AuditAction.STAFF_UPDATED, "Staff", staffId, 
        String.format("Updated staff: %s", staffName));
}

public void logStaffDeleted(Long staffId, String staffName) {
    log(AuditAction.STAFF_DELETED, "Staff", staffId, 
        String.format("Deleted staff: %s", staffName));
}

public void logLeaveApproved(Long leaveId, String staffName) {
    log(AuditAction.LEAVE_APPROVED, "Leave", leaveId, 
        String.format("Approved leave for: %s", staffName));
}

public void logLeaveRejected(Long leaveId, String staffName) {
    log(AuditAction.LEAVE_REJECTED, "Leave", leaveId, 
        String.format("Rejected leave for: %s", staffName));
}
```

#### 7. Update AuditAction Enum

**Add New Actions:**
```java
STAFF_CREATED,
STAFF_UPDATED,
STAFF_DELETED,
LEAVE_APPROVED,
LEAVE_REJECTED,
ATTENDANCE_CLOCK_IN,
ATTENDANCE_CLOCK_OUT
```

### Frontend Fixes

#### 1. Update StaffManagement.js

**Replace Alerts with Toast:**
```javascript
import { toast } from 'react-toastify';

// Replace all alert() calls with:
toast.success('Staff member added successfully!');
toast.error('Failed to add staff — username may already exist');
toast.info('Leave request submitted!');
```

**Add Better Error Handling:**
```javascript
const createStaff = async () => {
  if (!newStaff.fullName || !newStaff.username) {
    toast.error('Full Name and Username are required');
    return;
  }
  
  try {
    const res = await API.post('/api/staff', newStaff);
    setStaff(prev => [...prev, res.data.data]); // Handle ApiResponse wrapper
    setNewStaff({ fullName: '', username: '', role: 'CAPTAIN', phone: '', email: '' });
    toast.success('Staff member added successfully!');
  } catch (err) {
    const message = err.response?.data?.message || 'Failed to add staff';
    toast.error(message);
  }
};
```

**Add Loading States:**
```javascript
const [isSubmitting, setIsSubmitting] = useState(false);

const createStaff = async () => {
  setIsSubmitting(true);
  try {
    // ... existing code
  } finally {
    setIsSubmitting(false);
  }
};
```

**Add Confirmation Dialogs:**
```javascript
import { useConfirm } from '../../hooks/useConfirm';

const { confirm } = useConfirm();

const deleteStaff = async (id, name) => {
  const confirmed = await confirm({
    title: 'Delete Staff Member',
    message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
    confirmText: 'Delete',
    confirmColor: 'red'
  });
  
  if (!confirmed) return;
  
  try {
    await API.delete(`/api/staff/${id}`);
    setStaff(prev => prev.filter(s => s.id !== id));
    toast.success('Staff member deleted successfully');
  } catch (err) {
    toast.error('Failed to delete staff member');
  }
};
```

## Priority Implementation Order

### Phase 1: Critical Fixes (High Priority)
1. ✅ Add exception handling (ResourceNotFoundException, BusinessValidationException)
2. ✅ Add audit logging integration
3. ✅ Add validation methods
4. ✅ Update controller with ApiResponse wrapper

### Phase 2: Business Logic (Medium Priority)
5. ✅ Add leave overlap validation
6. ✅ Add duplicate clock-in prevention
7. ✅ Add multi-tenancy validation
8. ✅ Add repository custom queries

### Phase 3: Frontend Improvements (Medium Priority)
9. ✅ Replace alerts with Toast notifications
10. ✅ Add loading states
11. ✅ Add confirmation dialogs
12. ✅ Improve error messages

### Phase 4: Enhancements (Low Priority)
13. ⏳ Add pagination support
14. ⏳ Generate random passwords
15. ⏳ Add staff performance reports
16. ⏳ Add attendance analytics

## Files to Update

### Backend (8 files)
1. `StaffService.java` - Add validation, audit logging, exception handling
2. `StaffController.java` - Add ApiResponse wrapper, validation annotations
3. `StaffRepository.java` - Add custom queries
4. `AttendanceRepository.java` - Add active attendance check
5. `LeaveRepository.java` - Add overlap check query
6. `AuditService.java` - Add staff-specific methods
7. `AuditAction.java` - Add new enum values
8. `StaffDTO.java` - Add validation annotations

### Frontend (1 file)
1. `StaffManagement.js` - Replace alerts, add loading states, improve UX

## Testing Checklist

- [ ] Test staff creation with duplicate username
- [ ] Test staff creation with invalid data
- [ ] Test staff update
- [ ] Test staff deletion
- [ ] Test leave application with overlapping dates
- [ ] Test leave approval/rejection
- [ ] Test clock-in/clock-out
- [ ] Test duplicate clock-in prevention
- [ ] Test cross-tenant access prevention
- [ ] Test audit log entries for all operations

## Conclusion

The staff management module needs significant updates to match the quality and patterns used in the Customer and Billing modules. The fixes are straightforward and follow established patterns from other modules.

**Estimated Effort**: 4-6 hours for complete implementation and testing

**Risk Level**: Low (following established patterns)

**Impact**: High (improves security, data integrity, and user experience)