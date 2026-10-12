package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.dto.EmployeeCreateDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeResponseDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(
            @RequestBody EmployeeCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(dto));
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDTO> getMyProfile(
            Authentication authentication) {
        return ResponseEntity.ok(toResponse(
                employeeService.getMyProfile(authentication.getName())));
    }

    @GetMapping("/me/profile-details")
    public ResponseEntity<EmployeeProfileDTO> getMyProfileDetails(
            Authentication authentication) {
        return ResponseEntity.ok(
                employeeService.getMyProfileDetails(authentication.getName()));
    }

    @GetMapping("/manager/my-team")
    public ResponseEntity<List<Employee>> getMyTeam(
            Authentication authentication) {
        Employee manager =
                employeeService.getMyProfile(authentication.getName());

        return ResponseEntity.ok(
                employeeService.getEmployeesByManager(
                        manager.getEmployeeId()));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeDetails(
            @PathVariable Integer id) {
        Employee employee = employeeService.getEmployeeById(id);
        EmployeeResponseDTO dto = toResponse(employee);

        if (employee.getManagerId() != null) {
            Employee manager =
                    employeeService.getEmployeeById(employee.getManagerId());
            dto.setManagerName(
                    manager.getFirstName() + " " + manager.getLastName());
        }

        if (employee.getHrId() != null) {
            Employee hr = employeeService.getEmployeeById(employee.getHrId());
            dto.setHrName(hr.getFirstName() + " " + hr.getLastName());
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<EmployeeResponseDTO> updateMyProfile(
            Authentication authentication,
            @RequestBody EmployeeUpdateDTO details) {
        return ResponseEntity.ok(toResponse(
                employeeService.updateMyProfile(
                        authentication.getName(), details)));
    }

    @PostMapping("/me/photo")
    public ResponseEntity<EmployeeResponseDTO> uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("photo") MultipartFile photo) {
        return ResponseEntity.ok(toResponse(
                employeeService.updateProfilePhoto(
                        authentication.getName(), photo)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Integer id,
            @RequestBody Employee employee) {
        return ResponseEntity.ok(
                toResponse(employeeService.updateEmployee(id, employee)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployee(
            @RequestParam String name) {
        return ResponseEntity.ok(employeeService.searchByName(name));
    }

    @GetMapping("/email")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(toResponse(
                employeeService.getEmployeeByEmail(email)));
    }

    @GetMapping("/designation/{designationId}")
    public ResponseEntity<List<Employee>> getEmployeesByDesignation(
            @PathVariable Integer designationId) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByDesignation(designationId));
    }

    @GetMapping("/sort")
    public ResponseEntity<List<Employee>> getEmployeesSorted(
            @RequestParam(defaultValue = "firstName") String field,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(
                employeeService.getEmployeesSorted(field, direction));
    }

    @PutMapping("/{employeeId}/manager/{managerId}")
    public ResponseEntity<EmployeeResponseDTO> assignManager(
            @PathVariable Integer employeeId,
            @PathVariable Integer managerId) {
        return ResponseEntity.ok(toResponse(
                employeeService.assignManager(employeeId, managerId)));
    }

    @PutMapping("/{employeeId}/hr/{hrId}")
    public ResponseEntity<EmployeeResponseDTO> assignHr(
            @PathVariable Integer employeeId,
            @PathVariable Integer hrId) {
        return ResponseEntity.ok(toResponse(
                employeeService.assignHr(employeeId, hrId)));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<Employee>> getEmployeesByManager(
            @PathVariable Integer managerId) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByManager(managerId));
    }

    @GetMapping("/hr/{hrId}")
    public ResponseEntity<List<Employee>> getEmployeesByHr(
            @PathVariable Integer hrId) {
        return ResponseEntity.ok(employeeService.getEmployeesByHr(hrId));
    }

    @GetMapping("/hr/my-employees")
    public ResponseEntity<List<Employee>> getMyHrEmployees(
            Authentication authentication) {
        Employee hr = employeeService.getMyProfile(authentication.getName());
        return ResponseEntity.ok(
                employeeService.getEmployeesByHr(hr.getEmployeeId()));
    }

    @GetMapping("/managers")
    public ResponseEntity<List<Employee>> getManagers() {
        return ResponseEntity.ok(employeeService.getEmployeesByRole(3));
    }

    @GetMapping("/hr-options")
    public ResponseEntity<List<Employee>> getHrOptions() {
        return ResponseEntity.ok(employeeService.getEmployeesByRole(2));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateEmployeeStatus(
            @PathVariable Integer id,
            @RequestParam boolean active) {
        boolean updated =
                employeeService.setEmployeeActiveStatus(id, active);

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(active
                ? "Employee activated successfully"
                : "Employee deactivated successfully");
    }

    private EmployeeResponseDTO toResponse(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setEmployeeId(employee.getEmployeeId().longValue());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setHireDate(employee.getHireDate());
        dto.setAddress(employee.getAddress());
        dto.setProfilePhoto(employee.getProfilePhoto());

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(
                    employee.getDepartment().getDepartmentId().longValue());
            dto.setDepartmentName(
                    employee.getDepartment().getDepartmentName());
        }

        if (employee.getDesignation() != null) {
            dto.setDesignationId(
                    employee.getDesignation().getDesignationId().longValue());
            dto.setDesignationName(
                    employee.getDesignation().getDesignationTitle());
        }

        if (employee.getManagerId() != null) {
            dto.setManagerId(employee.getManagerId().longValue());
        }

        if (employee.getHrId() != null) {
            dto.setHrId(employee.getHrId().longValue());
        }

        return dto;
    }
}