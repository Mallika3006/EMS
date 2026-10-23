package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Attendance;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.AttendanceRepository;
import com.mallika.EmployeeManagementSystem.repository.EmployeeRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyContext companyContext;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            CompanyContext companyContext) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.companyContext = companyContext;
    }

    private Long getCompanyId() {
        Long companyId = companyContext.getCompanyId();

        if (companyId == null) {
            throw new AccessDeniedException(
                    "No company is associated with the authenticated user");
        }

        return companyId;
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException("User is not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getCompanyId() == null
                || !user.getCompanyId().equals(getCompanyId())) {
            throw new AccessDeniedException(
                    "User does not belong to the current company");
        }

        if (user.getEmployee() == null
                || user.getEmployee().getEmployeeId() == null) {
            throw new ResourceNotFoundException(
                    "Employee profile not found");
        }

        return user;
    }

    private boolean hasRole(String roleName) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_" + roleName));
    }

    private Integer currentEmployeeId() {
        return getCurrentUser().getEmployee().getEmployeeId();
    }

    private void validateAttendance(Attendance attendance) {
        if (attendance == null || attendance.getEmployee() == null
                || attendance.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee is required");
        }
        if (attendance.getAttDate() == null) throw new IllegalArgumentException("Attendance date is required");
        if (attendance.getStatus() == null) throw new IllegalArgumentException("Attendance status is required");
        switch (attendance.getStatus().trim().toLowerCase()) {
            case "present" -> attendance.setStatus("Present");
            case "absent" -> attendance.setStatus("Absent");
            case "late" -> attendance.setStatus("Late");
            case "leave" -> attendance.setStatus("Leave");
            default -> throw new IllegalArgumentException("Status must be Present, Absent, Late, or Leave");
        }
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null
                && attendance.getCheckOutTime().isBefore(attendance.getCheckInTime())) {
            throw new IllegalArgumentException("Check-out time cannot be earlier than check-in time");
        }
    }

    // Manual attendance entry; repository enforces company and active-account membership.
    public Attendance createAttendance(Attendance attendance) {
        validateAttendance(attendance);
        return attendanceRepository.createAttendance(attendance);
    }

    public Attendance checkIn() {
        User user = getCurrentUser();
        return attendanceRepository.checkIn(user.getEmployee().getEmployeeId());
    }

    public Attendance checkOut() {
        User user = getCurrentUser();
        return attendanceRepository.checkOut(user.getEmployee().getEmployeeId());
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.getAllAttendance();
    }

    public Attendance getAttendanceById(Integer id) {
        return attendanceRepository.getAttendanceById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attendance not found with id: " + id));
    }

    public Attendance updateAttendance(
            Integer id, Attendance attendanceDetails) {
        validateAttendance(attendanceDetails);
        return attendanceRepository.updateAttendance(id, attendanceDetails);
    }

    public void deleteAttendance(Integer id) {
        if (!attendanceRepository.deleteAttendance(id)) {
            throw new ResourceNotFoundException(
                    "Attendance not found with id: " + id);
        }
    }

    public List<Attendance> getAttendanceByEmployee(Integer employeeId) {
        return attendanceRepository.findByEmployeeEmployeeId(employeeId);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttDate(date);
    }

    public List<Attendance> getEmployeeAttendanceByDate(
            Integer employeeId, LocalDate date) {
        return attendanceRepository
                .findByEmployeeEmployeeIdAndAttDate(employeeId, date);
    }

    public List<Attendance> getAttendanceByStatus(String status) {
        return attendanceRepository.findByStatusIgnoreCase(status);
    }

    public List<Attendance> getEmployeeAttendanceByStatus(
            Integer employeeId, String status) {
        return attendanceRepository
                .findByEmployeeEmployeeIdAndStatusIgnoreCase(
                        employeeId, status);
    }

    // LOGGED-IN EMPLOYEE: own attendance only.
    public List<Attendance> getMyAttendance() {
        return attendanceRepository.findByEmployeeEmployeeId(
                currentEmployeeId());
    }

    // MANAGER: attendance of active team members in the same company.
    public List<Attendance> getMyTeamAttendance() {
        User manager = getCurrentUser();

        if (!hasRole("MANAGER")) {
            throw new AccessDeniedException(
                    "Only managers can view team attendance");
        }

        return attendanceRepository.findTeamAttendanceByManager(
                manager.getEmployee().getEmployeeId());
    }

    // MANAGER: create attendance only for an active member of their team.
    public Attendance createTeamAttendance(Attendance attendance) {
        User manager = getCurrentUser();

        if (!hasRole("MANAGER")) {
            throw new AccessDeniedException(
                    "Only managers can add team attendance");
        }

        if (attendance == null
                || attendance.getEmployee() == null
                || attendance.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee is required");
        }

        validateAttendance(attendance);

        Integer managerId = manager.getEmployee().getEmployeeId();
        Integer employeeId = attendance.getEmployee().getEmployeeId();

        List<Employee> teamMembers =
                employeeRepository.getEmployeesByManager(
                        managerId, getCompanyId());

        boolean isTeamMember = teamMembers.stream()
                .anyMatch(employee ->
                        employee.getEmployeeId().equals(employeeId));

        if (!isTeamMember) {
            throw new AccessDeniedException(
                    "You can only add attendance for your active team members");
        }

        return attendanceRepository.createAttendance(attendance);
    }

    // HR: attendance of active employees assigned to this HR user.
    public List<Attendance> getHrEmployeeAttendance() {
        User hr = getCurrentUser();

        if (!hasRole("HR")) {
            throw new AccessDeniedException(
                    "Only HR users can view assigned employee attendance");
        }

        return attendanceRepository.getAttendanceByHr(
                hr.getEmployee().getEmployeeId());
    }
}