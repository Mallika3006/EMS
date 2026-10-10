package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Attendance;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.AttendanceRepository;
import com.mallika.EmployeeManagementSystem.repository.EmployeeRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
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

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository) {

        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }


    // =========================================================
    // CREATE
    // =========================================================

    public Attendance createAttendance(
            Attendance attendance) {

        return attendanceRepository
                .createAttendance(attendance);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Attendance> getAllAttendance() {

        return attendanceRepository
                .getAllAttendance();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Attendance getAttendanceById(
            Integer id) {

        return attendanceRepository
                .getAttendanceById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attendance not found with id: "
                                        + id
                        ));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Attendance updateAttendance(
            Integer id,
            Attendance attendanceDetails) {

        return attendanceRepository
                .updateAttendance(
                        id,
                        attendanceDetails
                );
    }


    // =========================================================
    // DELETE
    // =========================================================

    public void deleteAttendance(Integer id) {

        boolean deleted =
                attendanceRepository
                        .deleteAttendance(id);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Attendance not found with id: "
                            + id
            );
        }
    }


    // =========================================================
    // GET BY EMPLOYEE
    // =========================================================

    public List<Attendance> getAttendanceByEmployee(
            Integer employeeId) {

        return attendanceRepository
                .findByEmployeeEmployeeId(
                        employeeId
                );
    }


    // =========================================================
    // GET BY DATE
    // =========================================================

    public List<Attendance> getAttendanceByDate(
            LocalDate date) {

        return attendanceRepository
                .findByAttDate(date);
    }


    // =========================================================
    // GET BY EMPLOYEE AND DATE
    // =========================================================

    public List<Attendance>
    getEmployeeAttendanceByDate(
            Integer employeeId,
            LocalDate date) {

        return attendanceRepository
                .findByEmployeeEmployeeIdAndAttDate(
                        employeeId,
                        date
                );
    }


    // =========================================================
    // GET BY STATUS
    // =========================================================

    public List<Attendance> getAttendanceByStatus(
            String status) {

        return attendanceRepository
                .findByStatusIgnoreCase(status);
    }


    // =========================================================
    // GET EMPLOYEE ATTENDANCE BY STATUS
    // =========================================================

    public List<Attendance>
    getEmployeeAttendanceByStatus(
            Integer employeeId,
            String status) {

        return attendanceRepository
                .findByEmployeeEmployeeIdAndStatusIgnoreCase(
                        employeeId,
                        status
                );
    }


    // =========================================================
    // GET LOGGED-IN EMPLOYEE'S ATTENDANCE
    // =========================================================

    public List<Attendance> getMyAttendance() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException(
                    "Employee not found"
            );
        }

        Integer employeeId =
                user.getEmployee()
                        .getEmployeeId();

        return attendanceRepository
                .findByEmployeeEmployeeId(
                        employeeId
                );
    }

    // =========================================================
// MANAGER - GET MY TEAM ATTENDANCE
// =========================================================

    public List<Attendance> getMyTeamAttendance() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (user.getEmployee() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found"
            );
        }

        Integer managerId =
                user.getEmployee()
                        .getEmployeeId();

        return attendanceRepository
                .findTeamAttendanceByManager(
                        managerId
                );
    }

    // =========================================================
// MANAGER - CREATE TEAM ATTENDANCE
// =========================================================

    public Attendance createTeamAttendance(
            Attendance attendance) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (user.getEmployee() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found"
            );
        }

        Integer managerId =
                user.getEmployee()
                        .getEmployeeId();

        if (attendance.getEmployee() == null ||
                attendance.getEmployee().getEmployeeId() == null) {

            throw new IllegalArgumentException(
                    "Employee is required"
            );
        }

        Integer employeeId =
                attendance.getEmployee()
                        .getEmployeeId();

        // Get employees belonging to this manager
        List<com.mallika.EmployeeManagementSystem.model.Employee> teamEmployees =
                employeeRepository.getEmployeesByManager(managerId);

        boolean isTeamMember =
                teamEmployees.stream()
                        .anyMatch(employee ->
                                employee.getEmployeeId()
                                        .equals(employeeId)
                        );

        if (!isTeamMember) {

            throw new IllegalArgumentException(
                    "You can only add attendance for your team members"
            );
        }

        return attendanceRepository
                .createAttendance(attendance);
    }

    // =========================================================
// HR - GET ATTENDANCE OF ASSIGNED EMPLOYEES
// =========================================================

    public List<Attendance> getHrEmployeeAttendance() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException(
                    "HR employee profile not found"
            );
        }

        Integer hrId = user.getEmployee().getEmployeeId();

        return attendanceRepository.getAttendanceByHr(hrId);
    }
}
