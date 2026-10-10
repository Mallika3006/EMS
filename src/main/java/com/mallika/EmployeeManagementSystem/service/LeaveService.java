package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Leave;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.LeaveRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;

    public LeaveService(
            LeaveRepository leaveRepository,
            UserRepository userRepository) {

        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // CREATE LEAVE
    // =========================================================

    public Leave createLeave(Leave leave) {

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
                                        "User not found: "
                                                + username
                                ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found for user: "
                            + username
            );
        }

        Integer employeeId =
                user.getEmployee().getEmployeeId();

        Employee employee =
                new Employee();

        employee.setEmployeeId(employeeId);

        leave.setEmployee(employee);

        // Always PENDING when employee applies
        leave.setStatus("PENDING");

        return leaveRepository.createLeave(leave);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Leave> getAllLeaves() {

        return leaveRepository.getAllLeaves();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Leave getLeaveById(Integer id) {

        return leaveRepository
                .getLeaveById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: "
                                        + id
                        ));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Leave updateLeave(
            Integer id,
            Leave leaveDetails) {

        return leaveRepository.updateLeave(
                id,
                leaveDetails
        );
    }


    // =========================================================
    // WITHDRAW LEAVE
    // =========================================================

    public Leave withdrawLeave(Integer id) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();


        // Find logged-in user
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: "
                                                + username
                                ));


        // Make sure employee exists
        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found for user: "
                            + username
            );
        }


        Integer employeeId =
                user.getEmployee().getEmployeeId();


        // Get leave
        Leave leave =
                leaveRepository
                        .getLeaveById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave not found with id: "
                                                + id
                                ));


        // Make sure leave belongs to logged-in employee
        if (leave.getEmployee() == null ||
                leave.getEmployee().getEmployeeId() == null ||
                !employeeId.equals(
                        leave.getEmployee().getEmployeeId()
                )) {

            throw new RuntimeException(
                    "You are not allowed to withdraw this leave"
            );
        }


        // Only PENDING leave can be withdrawn
        if (!"PENDING".equalsIgnoreCase(
                leave.getStatus()
        )) {

            throw new RuntimeException(
                    "Only pending leave can be withdrawn"
            );
        }


        // Call JDBC repository
        return leaveRepository.withdrawLeave(id);
    }


    // =========================================================
    // DELETE
    // =========================================================

    public void deleteLeave(Integer id) {

        boolean deleted =
                leaveRepository.deleteLeave(id);

        if (!deleted) {

            throw new ResourceNotFoundException(
                    "Leave not found with id: " + id
            );
        }
    }


    // =========================================================
    // GET BY EMPLOYEE
    // =========================================================

    public List<Leave> getLeavesByEmployee(
            Integer employeeId) {

        return leaveRepository
                .findByEmployeeEmployeeId(employeeId);
    }


    // =========================================================
    // GET BY STATUS
    // =========================================================

    public List<Leave> getLeavesByStatus(
            String status) {

        return leaveRepository
                .findByStatusIgnoreCase(status);
    }


    // =========================================================
    // GET EMPLOYEE LEAVES BY STATUS
    // =========================================================

    public List<Leave> getEmployeeLeavesByStatus(
            Integer employeeId,
            String status) {

        return leaveRepository
                .findByEmployeeEmployeeIdAndStatusIgnoreCase(
                        employeeId,
                        status
                );
    }


    // =========================================================
    // GET BY FROM DATE
    // =========================================================

    public List<Leave> getLeavesByFromDate(
            LocalDate fromDate) {

        return leaveRepository
                .findByFromDate(fromDate);
    }


    // =========================================================
    // GET BY TO DATE
    // =========================================================

    public List<Leave> getLeavesByToDate(
            LocalDate toDate) {

        return leaveRepository
                .findByToDate(toDate);
    }


    // =========================================================
    // GET BETWEEN DATES
    // =========================================================

    public List<Leave> getLeavesBetweenDates(
            LocalDate startDate,
            LocalDate endDate) {

        return leaveRepository
                .findByFromDateBetween(
                        startDate,
                        endDate
                );
    }


    // =========================================================
    // GET LOGGED-IN EMPLOYEE'S LEAVES
    // =========================================================

    public List<Leave> getMyLeaves() {

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
                                        "User not found: "
                                                + username
                                ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found"
            );
        }

        Integer employeeId =
                user.getEmployee().getEmployeeId();

        return leaveRepository
                .findByEmployeeEmployeeId(employeeId);
    }

    // =========================================================
// GET MANAGER LEAVE REQUESTS
// =========================================================

    public List<Leave> getManagerLeaveRequests() {

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
                                        "User not found: " + username
                                ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found for manager: " + username
            );
        }

        Integer managerId =
                user.getEmployee().getEmployeeId();

        return leaveRepository
                .getManagerLeaveRequests(managerId);
    }

    // =========================================================
    // UPDATE MANAGER LEAVE STATUS
    // =========================================================

    public Leave updateManagerLeaveStatus(
            Integer leaveId,
            String status) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();


        // Find logged-in manager
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: " + username
                                ));


        // Make sure manager has an employee record
        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not found for manager: " + username
            );
        }


        // Logged-in manager's employee ID
        Integer managerId =
                user.getEmployee().getEmployeeId();


        // Only APPROVED or REJECTED
        if (!"APPROVED".equalsIgnoreCase(status) &&
                !"REJECTED".equalsIgnoreCase(status)) {

            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED"
            );
        }


        return leaveRepository.updateManagerLeaveStatus(
                leaveId,
                managerId,
                status.toUpperCase()
        );
    }

    // =========================================================
// GET LOGGED-IN HR'S EMPLOYEE LEAVES
// =========================================================

    public List<Leave> getHrLeaves() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "HR employee profile not found"
            );
        }

        Integer hrId = user.getEmployee().getEmployeeId();

        return leaveRepository.getLeavesByHr(hrId);
    }


// =========================================================
// DELETE LEAVE BELONGING TO LOGGED-IN HR'S EMPLOYEE
// =========================================================

    public void deleteHrLeave(Integer leaveId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "HR employee profile not found"
            );
        }

        Integer hrId = user.getEmployee().getEmployeeId();

        boolean deleted =
                leaveRepository.deleteLeaveByHr(leaveId, hrId);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Leave not found or employee is not assigned to this HR"
            );
        }
    }

    // =========================================================
// HR - APPROVE / REJECT LEAVE
// =========================================================

    public Leave updateHrLeaveStatus(
            Integer leaveId,
            String status) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "HR employee profile not found"
            );
        }

        Integer hrId = user.getEmployee().getEmployeeId();

        if (status == null ||
                (!"APPROVED".equalsIgnoreCase(status) &&
                        !"REJECTED".equalsIgnoreCase(status))) {

            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED"
            );
        }

        return leaveRepository.updateHrLeaveStatus(
                leaveId,
                hrId,
                status.toUpperCase()
        );
    }
}