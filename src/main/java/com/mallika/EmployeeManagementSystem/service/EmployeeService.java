package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.EmployeeRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }


    // =========================
    // CREATE
    // =========================

    public Employee createEmployee(Employee employee) {

        // Will be converted to CallableStatement later.
        throw new UnsupportedOperationException(
                "Create employee will be converted to stored procedure"
        );
    }


    // =========================
    // GET ALL
    // =========================

    public List<Employee> getAllEmployees() {

        return employeeRepository.getAllEmployees();
    }


    // =========================
    // GET BY ID
    // =========================

    public Employee getEmployeeById(Integer id) {

        return employeeRepository.getEmployeeById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        ));
    }


    // =========================
    // GET LOGGED-IN EMPLOYEE
    // =========================

    public Employee getMyProfile(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        ));

        if (user.getEmployee() == null) {

            throw new ResourceNotFoundException(
                    "No employee profile linked to user: "
                            + username
            );
        }

        Integer employeeId =
                user.getEmployee().getEmployeeId();

        return employeeRepository.getEmployeeById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: "
                                        + employeeId
                        ));
    }


    // =========================
// UPDATE EMPLOYEE
// =========================

    public Employee updateEmployee(
            Integer id,
            Employee employeeDetails) {

        Employee existingEmployee =
                getEmployeeById(id);

        EmployeeUpdateDTO updateDetails =
                new EmployeeUpdateDTO();

        updateDetails.setFirstName(
                employeeDetails.getFirstName()
        );

        updateDetails.setLastName(
                employeeDetails.getLastName()
        );

        updateDetails.setEmail(
                employeeDetails.getEmail()
        );

        updateDetails.setPhone(
                employeeDetails.getPhone()
        );

        updateDetails.setDateOfBirth(
                employeeDetails.getDateOfBirth()
        );

        updateDetails.setAddress(
                employeeDetails.getAddress()
        );

        // Keep existing profile photo
        updateDetails.setProfilePhoto(
                existingEmployee.getProfilePhoto()
        );

        // Department
        if (employeeDetails.getDepartment() != null) {

            updateDetails.setDepartmentId(
                    employeeDetails
                            .getDepartment()
                            .getDepartmentId()
            );
        }

        // Manager
        updateDetails.setManagerId(
                employeeDetails.getManagerId()
        );

        // HR
        updateDetails.setHrId(
                employeeDetails.getHrId()
        );

        return employeeRepository.updateMyProfile(
                id,
                updateDetails
        );
    }


    // =========================
    // DELETE
    // =========================

    public void deleteEmployee(Integer id) {

        // Will be converted to CallableStatement later.
        throw new UnsupportedOperationException(
                "Delete employee will be converted to stored procedure"
        );
    }


    // =========================
    // SEARCH BY FIRST NAME
    // =========================

    public List<Employee> searchByFirstName(
            String firstName) {

        return employeeRepository.searchByName(firstName);
    }


    // =========================
    // SEARCH BY LAST NAME
    // =========================

    public List<Employee> searchByLastName(
            String lastName) {

        return employeeRepository.searchByName(lastName);
    }


    // =========================
    // SEARCH BY NAME
    // =========================

    public List<Employee> searchByName(String name) {

        return employeeRepository.searchByName(name);
    }


    // =========================
    // SEARCH BY EMAIL
    // =========================

    public Employee getEmployeeByEmail(String email) {

        return employeeRepository
                .getEmployeeByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with email: "
                                        + email
                        ));
    }


    // =========================
    // FILTER BY DESIGNATION
    // =========================

    public List<Employee> getEmployeesByDesignation(
            Integer designationId) {

        return employeeRepository
                .getEmployeesByDesignation(designationId);
    }


    // =========================
    // SORT
    // =========================

    public List<Employee> getEmployeesSorted(
            String field,
            String direction) {

        // Sorting will be converted to SQL later.
        throw new UnsupportedOperationException(
                "Sorting will be converted to SQL"
        );
    }


    // =========================
    // UPDATE MY PROFILE
    // =========================

    public Employee updateMyProfile(
            String username,
            EmployeeUpdateDTO updateDetails) {

        Employee employee =
                getMyProfile(username);

        return employeeRepository.updateMyProfile(
                employee.getEmployeeId(),
                updateDetails
        );
    }


    // =========================
    // UPDATE PROFILE PHOTO
    // =========================

    public Employee updateProfilePhoto(
            String username,
            MultipartFile photo) {

        Employee employee =
                getMyProfile(username);

        try {

            String uploadDir =
                    "uploads/profile-photos/";

            Files.createDirectories(
                    Paths.get(uploadDir)
            );

            String fileName =
                    UUID.randomUUID()
                            + "_"
                            + photo.getOriginalFilename();

            Path filePath =
                    Paths.get(uploadDir, fileName);

            Files.write(
                    filePath,
                    photo.getBytes()
            );

            String profilePhoto =
                    "/uploads/profile-photos/" + fileName;

            return employeeRepository.updateProfilePhoto(
                    employee.getEmployeeId(),
                    profilePhoto
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload profile photo",
                    e
            );
        }
    }


    // =========================
    // ASSIGN MANAGER
    // =========================

    public Employee assignManager(
            Integer employeeId,
            Integer managerId) {

        getEmployeeById(employeeId);
        getEmployeeById(managerId);

        return employeeRepository.assignManager(
                employeeId,
                managerId
        );
    }


    // =========================
    // ASSIGN HR
    // =========================

    public Employee assignHr(
            Integer employeeId,
            Integer hrId) {

        getEmployeeById(employeeId);
        getEmployeeById(hrId);

        return employeeRepository.assignHr(
                employeeId,
                hrId
        );
    }


    // =========================
    // GET EMPLOYEES BY MANAGER
    // =========================

    public List<Employee> getEmployeesByManager(
            Integer managerId) {

        getEmployeeById(managerId);

        return employeeRepository
                .getEmployeesByManager(managerId);
    }


    // =========================
    // GET EMPLOYEES BY HR
    // =========================

    public List<Employee> getEmployeesByHr(
            Integer hrId) {

        getEmployeeById(hrId);

        return employeeRepository
                .getEmployeesByHr(hrId);
    }

    // =========================
    // GET MY PROFILE DETAILS
    // =========================

    public EmployeeProfileDTO getMyProfileDetails(
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        )
                );

        if (user.getEmployee() == null) {

            throw new ResourceNotFoundException(
                    "No employee profile linked to user: "
                            + username
            );
        }

        Integer employeeId =
                user.getEmployee().getEmployeeId();

        return employeeRepository
                .getEmployeeProfileDetails(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee profile not found with id: "
                                        + employeeId
                        )
                );
    }
}