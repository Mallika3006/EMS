package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.dto.EmployeeCreateDTO;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.service.EmployeeService;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-only employee-management API. The /admin/** security matcher protects
 * these routes, and all employee/user lookups remain scoped by CompanyContext
 * through the existing services and repositories.
 */
@RestController
@RequestMapping("/admin/api/employees")
public class AdminEmployeeManagementController {

    private final EmployeeService employeeService;
    private final UserRepository userRepository;

    public AdminEmployeeManagementController(EmployeeService employeeService,
                                             UserRepository userRepository) {
        this.employeeService = employeeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listEmployees() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Employee employee : employeeService.getAllEmployees()) {
            result.add(toSafeMap(employee));
        }
        return result;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmployee(
            @RequestBody EmployeeCreateDTO details) {
        Employee created = employeeService.createEmployee(details);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSafeMap(created));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getEmployee(@PathVariable Integer id) {
        return toSafeMap(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateEmployee(@PathVariable Integer id,
                                               @RequestBody Employee employee) {
        return toSafeMap(employeeService.updateEmployee(id, employee));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> setStatus(
            @PathVariable Integer id,
            @RequestParam boolean active) {
        boolean updated = employeeService.setEmployeeActiveStatus(id, active);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("employeeId", id);
        response.put("isActive", active);
        response.put("message", active
                ? "Employee account activated."
                : "Employee account deactivated.");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSafeMap(Employee employee) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employeeId", employee.getEmployeeId());
        result.put("firstName", employee.getFirstName());
        result.put("lastName", employee.getLastName());
        result.put("email", employee.getEmail());
        result.put("phone", employee.getPhone());
        result.put("dateOfBirth", employee.getDateOfBirth());
        result.put("hireDate", employee.getHireDate());
        result.put("address", employee.getAddress());
        result.put("profilePhoto", employee.getProfilePhoto());
        result.put("departmentId", employee.getDepartment() == null
                ? null : employee.getDepartment().getDepartmentId());
        result.put("departmentName", employee.getDepartment() == null
                ? null : employee.getDepartment().getDepartmentName());
        result.put("designationId", employee.getDesignation() == null
                ? null : employee.getDesignation().getDesignationId());
        result.put("designationName", employee.getDesignation() == null
                ? null : employee.getDesignation().getDesignationTitle());
        result.put("managerId", employee.getManagerId());
        result.put("hrId", employee.getHrId());

        User account = userRepository
                .findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElse(null);
        result.put("hasLoginAccount", account != null);
        result.put("username", account == null ? null : account.getUsername());
        result.put("roleId", account == null || account.getRole() == null
                ? null : account.getRole().getRoleId());
        result.put("isActive", account != null
                && Boolean.TRUE.equals(account.getIsActive()));
        return result;
    }
}
