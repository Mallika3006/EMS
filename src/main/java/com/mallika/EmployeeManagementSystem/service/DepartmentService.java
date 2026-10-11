
package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Department;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.DepartmentRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    // CREATE
    public Department createDepartment(Department department) {

        Long companyId = getCurrentCompanyId();

        // Never trust company ID supplied in the request body.
        department.setCompanyId(companyId);

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
