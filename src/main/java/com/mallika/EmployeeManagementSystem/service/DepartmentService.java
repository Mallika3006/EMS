package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Department;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.DepartmentRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
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


    // =========================
    // CREATE
    // =========================

    public Department createDepartment(
            Department department) {

        return departmentRepository.createDepartment(
                department
        );
    }


    // =========================
    // GET ALL
    // =========================

    public List<Department> getAllDepartments() {

        return departmentRepository.getAllDepartments();
    }


    // =========================
    // GET BY ID
    // =========================

    public Department getDepartmentById(
            Integer id) {

        return departmentRepository
                .getDepartmentById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + id
                        ));
    }


    // =========================
    // UPDATE
    // =========================

    public Department updateDepartment(
            Integer id,
            Department departmentDetails) {

        getDepartmentById(id);

        Department updated =
                departmentRepository.updateDepartment(
                        id,
                        departmentDetails
                );

        if (updated == null) {

            throw new ResourceNotFoundException(
                    "Department not found with id: " + id
            );
        }

        return updated;
    }


    // =========================
    // DELETE
    // =========================

    public void deleteDepartment(
            Integer id) {

        getDepartmentById(id);

        boolean deleted =
                departmentRepository.deleteDepartment(id);

        if (!deleted) {

            throw new ResourceNotFoundException(
                    "Department not found with id: " + id
            );
        }
    }


    // =========================
    // SEARCH BY NAME
    // =========================

    public List<Department> searchByName(
            String name) {

        return departmentRepository
                .findByDepartmentNameContainingIgnoreCase(
                        name
                );
    }


    // =========================
    // GET BY EXACT NAME
    // =========================

    public Department getDepartmentByName(
            String name) {

        return departmentRepository
                .findByDepartmentNameIgnoreCase(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with name: "
                                        + name
                        ));
    }


    // =========================
    // SEARCH BY LOCATION
    // =========================

    public List<Department> getDepartmentsByLocation(
            String location) {

        return departmentRepository
                .findByLocationContainingIgnoreCase(
                        location
                );
    }


    // =========================
    // GET EMPLOYEE'S OWN DEPARTMENT
    // =========================

    public Department getDepartmentByUsername(
            String username) {

        // 1. Find logged-in user
        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with username: "
                                                + username
                                ));

        // 2. Make sure employee is linked
        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not assigned to this user"
            );
        }

        // 3. Get department directly from employee
        Department department =
                user.getEmployee().getDepartment();

        if (department == null ||
                department.getDepartmentId() == null) {

            throw new ResourceNotFoundException(
                    "Department not assigned to this employee"
            );
        }

        // 4. Fetch complete department using JDBC
        return departmentRepository
                .getDepartmentById(
                        department.getDepartmentId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + department
                                        .getDepartmentId()
                        ));
    }
}