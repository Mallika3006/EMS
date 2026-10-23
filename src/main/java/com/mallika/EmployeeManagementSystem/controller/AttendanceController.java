package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Attendance;
import com.mallika.EmployeeManagementSystem.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyRole('EMPLOYEE','HR','MANAGER')")
    @PostMapping("/check-in")
    public ResponseEntity<Attendance> checkIn() {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn());
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','HR','MANAGER')")
    @PostMapping("/check-out")
    public ResponseEntity<Attendance> checkOut() {
        return ResponseEntity.ok(attendanceService.checkOut());
    }

    // Company-wide management is reserved for Admin.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Attendance> createAttendance(@RequestBody Attendance attendance) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createAttendance(attendance));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','HR','MANAGER')")
    @GetMapping("/me")
    public ResponseEntity<List<Attendance>> getMyAttendance() {
        return ResponseEntity.ok(attendanceService.getMyAttendance());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Integer id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(@PathVariable Integer id, @RequestBody Attendance attendance) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, attendance));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAttendance(@PathVariable Integer id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok("Attendance deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceByEmployee(@PathVariable Integer employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/date")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{employeeId}/date")
    public ResponseEntity<List<Attendance>> getEmployeeAttendanceByDate(
            @PathVariable Integer employeeId, @RequestParam LocalDate date) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceByDate(employeeId, date));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<List<Attendance>> getAttendanceByStatus(@RequestParam String status) {
        return ResponseEntity.ok(attendanceService.getAttendanceByStatus(status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{employeeId}/status")
    public ResponseEntity<List<Attendance>> getEmployeeAttendanceByStatus(
            @PathVariable Integer employeeId, @RequestParam String status) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceByStatus(employeeId, status));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/manager/team")
    public ResponseEntity<List<Attendance>> getMyTeamAttendance() {
        return ResponseEntity.ok(attendanceService.getMyTeamAttendance());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/manager")
    public ResponseEntity<Attendance> createTeamAttendance(@RequestBody Attendance attendance) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createTeamAttendance(attendance));
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/hr")
    public ResponseEntity<List<Attendance>> getHrEmployeeAttendance() {
        return ResponseEntity.ok(attendanceService.getHrEmployeeAttendance());
    }
}
