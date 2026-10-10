package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Leave;
import com.mallika.EmployeeManagementSystem.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Leave> createLeave(
            @RequestBody Leave leave) {

        Leave savedLeave = leaveService.createLeave(leave);

        return new ResponseEntity<>(
                savedLeave,
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Leave>> getAllLeaves() {

        return ResponseEntity.ok(
                leaveService.getAllLeaves()
        );
    }

    // GET LOGGED-IN EMPLOYEE'S LEAVES
    @GetMapping("/me")
    public ResponseEntity<List<Leave>> getMyLeaves() {

        return ResponseEntity.ok(
                leaveService.getMyLeaves()
        );
    }

    // =========================================================
    // MANAGER - LEAVE REQUESTS
    // =========================================================

    @GetMapping("/manager")
    public ResponseEntity<List<Leave>> getManagerLeaveRequests() {

        return ResponseEntity.ok(
                leaveService.getManagerLeaveRequests()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Leave> getLeaveById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                leaveService.getLeaveById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Leave> updateLeave(
            @PathVariable Integer id,
            @RequestBody Leave leave) {

        return ResponseEntity.ok(
                leaveService.updateLeave(id, leave)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLeave(
            @PathVariable Integer id) {

        leaveService.deleteLeave(id);

        return ResponseEntity.ok(
                "Leave deleted successfully"
        );
    }

    // GET BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Leave>> getLeavesByEmployee(
            @PathVariable Integer employeeId) {

        return ResponseEntity.ok(
                leaveService.getLeavesByEmployee(employeeId)
        );
    }

    // GET BY STATUS
    @GetMapping("/status")
    public ResponseEntity<List<Leave>> getLeavesByStatus(
            @RequestParam String status) {

        return ResponseEntity.ok(
                leaveService.getLeavesByStatus(status)
        );
    }

    // GET EMPLOYEE LEAVES BY STATUS
    @GetMapping("/employee/{employeeId}/status")
    public ResponseEntity<List<Leave>> getEmployeeLeavesByStatus(
            @PathVariable Integer employeeId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                leaveService.getEmployeeLeavesByStatus(
                        employeeId,
                        status
                )
        );
    }

    // GET BY FROM DATE
    @GetMapping("/from-date")
    public ResponseEntity<List<Leave>> getLeavesByFromDate(
            @RequestParam LocalDate fromDate) {

        return ResponseEntity.ok(
                leaveService.getLeavesByFromDate(fromDate)
        );
    }

    // GET BY TO DATE
    @GetMapping("/to-date")
    public ResponseEntity<List<Leave>> getLeavesByToDate(
            @RequestParam LocalDate toDate) {

        return ResponseEntity.ok(
                leaveService.getLeavesByToDate(toDate)
        );
    }

    // GET LEAVES BETWEEN DATES
    @GetMapping("/between")
    public ResponseEntity<List<Leave>> getLeavesBetweenDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                leaveService.getLeavesBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // WITHDRAW LEAVE
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Leave> withdrawLeave(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                leaveService.withdrawLeave(id)
        );
    }

    // =========================================================
    // MANAGER - APPROVE / REJECT LEAVE
    // =========================================================

    @PutMapping("/manager/{id}/status")
    public ResponseEntity<Leave> updateManagerLeaveStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                leaveService.updateManagerLeaveStatus(
                        id,
                        status
                )
        );
    }

    // =========================================================
// HR - GET ASSIGNED EMPLOYEES' LEAVES
// =========================================================

    @GetMapping("/hr")
    public ResponseEntity<List<Leave>> getHrLeaves() {

        return ResponseEntity.ok(
                leaveService.getHrLeaves()
        );
    }


// =========================================================
// HR - DELETE ASSIGNED EMPLOYEE'S LEAVE
// =========================================================

    @DeleteMapping("/hr/{id}")
    public ResponseEntity<String> deleteHrLeave(
            @PathVariable Integer id) {

        leaveService.deleteHrLeave(id);

        return ResponseEntity.ok(
                "Leave deleted successfully"
        );
    }

    // =========================================================
// HR - APPROVE / REJECT LEAVE
// =========================================================

    @PatchMapping("/hr/{id}/status")
    public ResponseEntity<Leave> updateHrLeaveStatus(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> request) {

        String status = request.get("status");

        return ResponseEntity.ok(
                leaveService.updateHrLeaveStatus(id, status)
        );
    }
}