
package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Department;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.DepartmentRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            UserRepository userRepository) {

        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // GET COMPANY ID FROM THE AUTHENTICATED USER
    private Long getCurrentCompanyId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new SecurityException("User is not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found"));

        if (user.getCompanyId() == null) {
            throw new SecurityException(
                    "No company is assigned to the authenticated user");
        }

        return user.getCompanyId();
    }

    // Validate department names within the authenticated company's tenant.
    private void validateDepartmentName(String name, Integer currentDepartmentId, Long companyId) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Department name is required");
        }

        String normalizedName = name.trim();
        if (normalizedName.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Department name must be 100 characters or fewer");
        }

        departmentRepository.findByDepartmentNameIgnoreCase(normalizedName, companyId)
                .filter(existing -> !existing.getDepartmentId().equals(currentDepartmentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A department with this name already exists in your company");
                });
    }

    // CREATE
    public Department createDepartment(Department department) {

        Long companyId = getCurrentCompanyId();

        if (department == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Department details are required");
        }

        // Never trust company ID supplied in the request body.
        department.setCompanyId(companyId);
        department.setDepartmentName(department.getDepartmentName().trim());
        validateDepartmentName(department.getDepartmentName(), null, companyId);

        return departmentRepository.createDepartment(
                department, companyId);
    }

    // GET ALL
    public List<Department> getAllDepartments() {

        return departmentRepository.getAllDepartments(
                getCurrentCompanyId());
    }

    // GET BY ID
    public Department getDepartmentById(Integer id) {

        Long companyId = getCurrentCompanyId();

        return departmentRepository.getDepartmentById(id, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: " + id));
    }

    // UPDATE
    public Department updateDepartment(
            Integer id,
            Department departmentDetails) {

        Long companyId = getCurrentCompanyId();

        if (departmentDetails == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Department details are required");
        }
        departmentDetails.setDepartmentName(
                departmentDetails.getDepartmentName() == null
                        ? null : departmentDetails.getDepartmentName().trim());
        validateDepartmentName(departmentDetails.getDepartmentName(), id, companyId);

        Department updated = departmentRepository.updateDepartment(
                id, departmentDetails, companyId);

        if (updated == null) {
            throw new ResourceNotFoundException(
                    "Department not found with id: " + id);
        }

        return updated;
    }

    // DELETE
    public void deleteDepartment(Integer id) {

        Long companyId = getCurrentCompanyId();

        boolean deleted =
                departmentRepository.deleteDepartment(id, companyId);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Department not found with id: " + id);
        }
    }

    // SEARCH BY NAME
    public List<Department> searchByName(String name) {

        return departmentRepository
                .findByDepartmentNameContainingIgnoreCase(
                        name, getCurrentCompanyId());
    }

    // GET BY EXACT NAME
    public Department getDepartmentByName(String name) {

        return departmentRepository
                .findByDepartmentNameIgnoreCase(
                        name, getCurrentCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with name: " + name));
    }

    // SEARCH BY LOCATION
    public List<Department> getDepartmentsByLocation(String location) {

        return departmentRepository
                .findByLocationContainingIgnoreCase(
                        location, getCurrentCompanyId());
    }

    // GET LOGGED-IN EMPLOYEE'S OWN DEPARTMENT
    public Department getDepartmentByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with username: " + username));

        if (user.getCompanyId() == null) {
            throw new SecurityException(
                    "No company is assigned to the user");
        }

        if (user.getEmployee() == null
                || user.getEmployee().getEmployeeId() == null) {
            throw new ResourceNotFoundException(
                    "Employee not assigned to this user");
        }

        Department department = user.getEmployee().getDepartment();

        if (department == null || department.getDepartmentId() == null) {
            throw new ResourceNotFoundException(
                    "Department not assigned to this employee");
        }

        return departmentRepository.getDepartmentById(
                        department.getDepartmentId(),
                        user.getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + department.getDepartmentId()));
    }
}
