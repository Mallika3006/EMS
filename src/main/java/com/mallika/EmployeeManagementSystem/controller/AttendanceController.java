package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Attendance;
import com.mallika.EmployeeManagementSystem.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Attendance> createAttendance(
            @RequestBody Attendance attendance) {

        Attendance savedAttendance =
                attendanceService.createAttendance(attendance);

        return new ResponseEntity<>(
                savedAttendance,
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {

        return ResponseEntity.ok(
                attendanceService.getAllAttendance()
        );
    }

    // GET LOGGED-IN EMPLOYEE'S ATTENDANCE
    @GetMapping("/me")
    public ResponseEntity<List<Attendance>> getMyAttendance() {

        return ResponseEntity.ok(
                attendanceService.getMyAttendance()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Integer id,
            @RequestBody Attendance attendance) {

        return ResponseEntity.ok(
                attendanceService.updateAttendance(
                        id,
                        attendance
                )
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAttendance(
            @PathVariable Integer id) {

        attendanceService.deleteAttendance(id);

        return ResponseEntity.ok(
                "Attendance deleted successfully"
        );
    }

    // GET BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceByEmployee(
            @PathVariable Integer employeeId) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByEmployee(employeeId)
        );
    }

    // GET BY DATE
    @GetMapping("/date")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByDate(date)
        );
    }

    // GET BY EMPLOYEE AND DATE
    @GetMapping("/employee/{employeeId}/date")
    public ResponseEntity<List<Attendance>> getEmployeeAttendanceByDate(
            @PathVariable Integer employeeId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                attendanceService.getEmployeeAttendanceByDate(
                        employeeId,
                        date
                )
        );
    }

    // GET BY STATUS
    @GetMapping("/status")
    public ResponseEntity<List<Attendance>> getAttendanceByStatus(
            @RequestParam String status) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByStatus(status)
        );
    }

    // GET EMPLOYEE ATTENDANCE BY STATUS
    @GetMapping("/employee/{employeeId}/status")
    public ResponseEntity<List<Attendance>> getEmployeeAttendanceByStatus(
            @PathVariable Integer employeeId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                attendanceService.getEmployeeAttendanceByStatus(
                        employeeId,
                        status
                )
        );
    }

    // =========================================================
// MANAGER - TEAM ATTENDANCE
// =========================================================

    @GetMapping("/manager/team")
    public ResponseEntity<List<Attendance>>
    getMyTeamAttendance() {

        return ResponseEntity.ok(
                attendanceService
                        .getMyTeamAttendance()
        );
    }

    // =========================================================
// MANAGER - ADD TEAM ATTENDANCE
// =========================================================

    @PostMapping("/manager")
    public ResponseEntity<Attendance> createTeamAttendance(
            @RequestBody Attendance attendance) {

        Attendance savedAttendance =
                attendanceService.createTeamAttendance(attendance);

        return new ResponseEntity<>(
                savedAttendance,
                HttpStatus.CREATED
        );
    }
}