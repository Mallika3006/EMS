package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.EmployeeRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mallika.EmployeeManagementSystem.dto.EmployeeCreateDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =========================
    // CREATE
    // =========================

    public Employee createEmployee(EmployeeCreateDTO dto) {

        if (dto.getFirstName() == null || dto.getFirstName().isBlank()
                || dto.getLastName() == null || dto.getLastName().isBlank()
                || dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()
                || dto.getRoleId() == null) {

            throw new IllegalArgumentException(
                    "Required employee and login fields must be provided");
        }

        if (dto.getRoleId() < 1 || dto.getRoleId() > 4) {
            throw new IllegalArgumentException("Invalid role ID");
        }

        String encodedPassword =
                passwordEncoder.encode(dto.getPassword());

        return employeeRepository.createEmployeeWithUser(
                dto,
                encodedPassword);
    }


    // =========================
    // GET ALL
    // =========================

    public List<Employee> getAllEmployees() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "User is not authenticated"
            );
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Logged-in user not found"));

        Long companyId = user.getCompanyId();

        if (companyId == null) {
            throw new RuntimeException(
                    "Company ID is missing for the logged-in user");
        }

        return employeeRepository.getAllEmployees(companyId);
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

    // =========================
// GET EMPLOYEES BY ROLE
// =========================


    public List<Employee> getEmployeesByRole(Integer roleId) {

        if (roleId == null || (roleId != 2 && roleId != 3)) {
            throw new IllegalArgumentException(
                    "Only HR and Manager roles are allowed"
            );
        }

        List<User> users =
                userRepository.findByRoleRoleIdAndIsActive(roleId, true);

        List<Employee> employees = new ArrayList<>();

        for (User user : users) {

            if (user.getEmployee() == null) {
                continue;
            }

            Integer employeeId = user.getEmployee().getEmployeeId();

            employeeRepository.getEmployeeById(employeeId)
                    .ifPresent(employees::add);
        }

        return employees;
    }

    public boolean setEmployeeActiveStatus(
            Integer employeeId,
            boolean isActive) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "User is not authenticated");
        }

        User loggedInUser = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Logged-in user not found"));

        if (loggedInUser.getCompanyId() == null) {
            throw new IllegalStateException("Company ID is missing");
        }

        return userRepository.setUserActiveStatus(
                employeeId,
                isActive,
                loggedInUser.getCompanyId());
    }
}