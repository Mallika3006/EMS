package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeResponseDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    // =========================
    // CREATE
    // =========================

    @PostMapping
    public ResponseEntity<Employee> createEmployee(
            @RequestBody Employee employee) {

        Employee savedEmployee =
                employeeService.createEmployee(employee);

        return new ResponseEntity<>(
                savedEmployee,
                HttpStatus.CREATED
        );
    }


    // =========================
    // GET ALL
    // =========================

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {

        return ResponseEntity.ok(
                employeeService.getAllEmployees()
        );
    }


    // =========================
    // GET LOGGED-IN EMPLOYEE
    // =========================

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDTO> getMyProfile(
            Authentication authentication) {

        String username = authentication.getName();

        Employee employee =
                employeeService.getMyProfile(username);

        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setEmployeeId(
                employee.getEmployeeId().longValue()
        );

        dto.setFirstName(
                employee.getFirstName()
        );

        dto.setLastName(
                employee.getLastName()
        );

        dto.setEmail(
                employee.getEmail()
        );

        dto.setPhone(
                employee.getPhone()
        );

        dto.setDateOfBirth(
                employee.getDateOfBirth()
        );

        dto.setHireDate(
                employee.getHireDate()
        );

        dto.setAddress(
                employee.getAddress()
        );

        dto.setProfilePhoto(
                employee.getProfilePhoto()
        );


        // Department
        if (employee.getDepartment() != null) {

            dto.setDepartmentId(
                    employee.getDepartment()
                            .getDepartmentId()
                            .longValue()
            );
        }


        // Designation
        if (employee.getDesignation() != null) {

            dto.setDesignationId(
                    employee.getDesignation()
                            .getDesignationId()
                            .longValue()
            );
        }


        // Manager
        dto.setManagerId(
                employee.getManagerId() != null
                        ? employee.getManagerId().longValue()
                        : null
        );


        // HR
        dto.setHrId(
                employee.getHrId() != null
                        ? employee.getHrId().longValue()
                        : null
        );

        return ResponseEntity.ok(dto);
    }

    // =========================
    // GET LOGGED-IN EMPLOYEE PROFILE DETAILS
    // =========================

    @GetMapping("/me/profile-details")
    public ResponseEntity<EmployeeProfileDTO> getMyProfileDetails(
            Authentication authentication) {

        String username = authentication.getName();

        EmployeeProfileDTO profile =
                employeeService.getMyProfileDetails(username);

        return ResponseEntity.ok(profile);
    }

    // =========================
// GET MY TEAM
// =========================

    @GetMapping("/manager/my-team")
    public ResponseEntity<List<Employee>> getMyTeam(
            Authentication authentication) {

        String username =
                authentication.getName();

        Employee manager =
                employeeService.getMyProfile(username);

        List<Employee> teamMembers =
                employeeService.getEmployeesByManager(
                        manager.getEmployeeId()
                );

        return ResponseEntity.ok(teamMembers);
    }


    // =========================
    // GET BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                employeeService.getEmployeeById(id)
        );
    }


    // =========================
    // UPDATE LOGGED-IN EMPLOYEE
    // =========================

    @PutMapping("/me")
    public ResponseEntity<EmployeeResponseDTO> updateMyProfile(
            Authentication authentication,
            @RequestBody EmployeeUpdateDTO updateDetails) {

        String username = authentication.getName();

        Employee employee =
                employeeService.updateMyProfile(
                        username,
                        updateDetails
                );

        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setEmployeeId(
                employee.getEmployeeId().longValue()
        );

        dto.setFirstName(
                employee.getFirstName()
        );

        dto.setLastName(
                employee.getLastName()
        );

        dto.setEmail(
                employee.getEmail()
        );

        dto.setPhone(
                employee.getPhone()
        );

        dto.setDateOfBirth(
                employee.getDateOfBirth()
        );

        dto.setHireDate(
                employee.getHireDate()
        );

        dto.setAddress(
                employee.getAddress()
        );

        dto.setProfilePhoto(
                employee.getProfilePhoto()
        );


        // Department
        if (employee.getDepartment() != null) {

            dto.setDepartmentId(
                    employee.getDepartment()
                            .getDepartmentId()
                            .longValue()
            );
        }


        // Designation
        if (employee.getDesignation() != null) {

            dto.setDesignationId(
                    employee.getDesignation()
                            .getDesignationId()
                            .longValue()
            );
        }


        // Manager
        dto.setManagerId(
                employee.getManagerId() != null
                        ? employee.getManagerId().longValue()
                        : null
        );


        // HR
        dto.setHrId(
                employee.getHrId() != null
                        ? employee.getHrId().longValue()
                        : null
        );

        return ResponseEntity.ok(dto);
    }


    // =========================
    // UPLOAD PROFILE PHOTO
    // =========================

    @PostMapping("/me/photo")
    public ResponseEntity<EmployeeResponseDTO> uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("photo") MultipartFile photo) {

        System.out.println("PHOTO ENDPOINT CALLED");

        String username = authentication.getName();

        Employee employee =
                employeeService.updateProfilePhoto(
                        username,
                        photo
                );

        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setEmployeeId(
                employee.getEmployeeId().longValue()
        );

        dto.setFirstName(
                employee.getFirstName()
        );

        dto.setLastName(
                employee.getLastName()
        );

        dto.setEmail(
                employee.getEmail()
        );

        dto.setPhone(
                employee.getPhone()
        );

        dto.setDateOfBirth(
                employee.getDateOfBirth()
        );

        dto.setHireDate(
                employee.getHireDate()
        );

        dto.setAddress(
                employee.getAddress()
        );

        dto.setProfilePhoto(
                employee.getProfilePhoto()
        );


        // Department
        if (employee.getDepartment() != null) {

            dto.setDepartmentId(
                    employee.getDepartment()
                            .getDepartmentId()
                            .longValue()
            );
        }


        // Designation
        if (employee.getDesignation() != null) {

            dto.setDesignationId(
                    employee.getDesignation()
                            .getDesignationId()
                            .longValue()
            );
        }


        // Manager
        dto.setManagerId(
                employee.getManagerId() != null
                        ? employee.getManagerId().longValue()
                        : null
        );


        // HR
        dto.setHrId(
                employee.getHrId() != null
                        ? employee.getHrId().longValue()
                        : null
        );

        return ResponseEntity.ok(dto);
    }


    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Integer id,
            @RequestBody Employee employee) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(id, employee)
        );
    }


    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Integer id) {

        employeeService.deleteEmployee(id);

        return ResponseEntity.ok(
                "Employee deleted successfully"
        );
    }


    // =========================
    // SEARCH BY NAME
    // =========================

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployee(
            @RequestParam String name) {

        return ResponseEntity.ok(
                employeeService.searchByName(name)
        );
    }


    // =========================
    // SEARCH BY EMAIL
    // =========================

    @GetMapping("/email")
    public ResponseEntity<Employee> getEmployeeByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(
                employeeService.getEmployeeByEmail(email)
        );
    }


    // =========================
    // FILTER BY DESIGNATION
    // =========================

    @GetMapping("/designation/{designationId}")
    public ResponseEntity<List<Employee>> getEmployeesByDesignation(
            @PathVariable Integer designationId) {

        return ResponseEntity.ok(
                employeeService.getEmployeesByDesignation(
                        designationId
                )
        );
    }


    // =========================
    // SORT
    // =========================

    @GetMapping("/sort")
    public ResponseEntity<List<Employee>> getEmployeesSorted(
            @RequestParam(defaultValue = "firstName") String field,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(
                employeeService.getEmployeesSorted(
                        field,
                        direction
                )
        );
    }


    // =========================
    // ASSIGN MANAGER
    // =========================

    @PutMapping("/{employeeId}/manager/{managerId}")
    public ResponseEntity<Employee> assignManager(
            @PathVariable Integer employeeId,
            @PathVariable Integer managerId) {

        return ResponseEntity.ok(
                employeeService.assignManager(
                        employeeId,
                        managerId
                )
        );
    }


    // =========================
    // ASSIGN HR
    // =========================

    @PutMapping("/{employeeId}/hr/{hrId}")
    public ResponseEntity<Employee> assignHr(
            @PathVariable Integer employeeId,
            @PathVariable Integer hrId) {

        return ResponseEntity.ok(
                employeeService.assignHr(
                        employeeId,
                        hrId
                )
        );
    }


    // =========================
    // GET EMPLOYEES BY MANAGER
    // =========================

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<Employee>> getEmployeesByManager(
            @PathVariable Integer managerId) {

        return ResponseEntity.ok(
                employeeService.getEmployeesByManager(
                        managerId
                )
        );
    }


    // =========================
    // GET EMPLOYEES BY HR
    // =========================

    @GetMapping("/hr/{hrId}")
    public ResponseEntity<List<Employee>> getEmployeesByHr(
            @PathVariable Integer hrId) {

        return ResponseEntity.ok(
                employeeService.getEmployeesByHr(hrId)
        );
    }
}