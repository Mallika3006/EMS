
package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.dto.EmployeeCreateDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.EmployeeRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyContext companyContext;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CompanyContext companyContext) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyContext = companyContext;
    }

    // =========================
    // COMPANY CONTEXT
    // =========================

    private Long getCompanyId() {
        return companyContext.getCompanyId();
    }

    private User getCompanyUser(String username) {

        Long companyId = getCompanyId();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCompanyId() == null
                || !user.getCompanyId().equals(companyId)) {
            throw new AccessDeniedException(
                    "User does not belong to the authenticated company");
        }

        return user;
    }

    // =========================
    // CREATE EMPLOYEE
    // =========================

    public Employee createEmployee(EmployeeCreateDTO dto) {

        if (dto == null
                || dto.getFirstName() == null
                || dto.getFirstName().isBlank()
                || dto.getLastName() == null
                || dto.getLastName().isBlank()
                || dto.getEmail() == null
                || dto.getEmail().isBlank()
                || dto.getUsername() == null
                || dto.getUsername().isBlank()
                || dto.getPassword() == null
                || dto.getPassword().isBlank()
                || dto.getRoleId() == null) {

            throw new IllegalArgumentException(
                    "Required employee and login fields must be provided");
        }

        if (dto.getRoleId() < 1 || dto.getRoleId() > 4) {
            throw new IllegalArgumentException("Invalid role ID");
        }

        /*
         * Do not call the old create function here.
         * It does not accept company_id, so it cannot safely
         * create an employee for the authenticated company.
         *
         * We will enable this operation after adding the
         * company-scoped SQL function and repository method.
         */
        throw new UnsupportedOperationException(
                "Employee creation is temporarily disabled until "
                        + "the company-scoped creation function is added");
    }

    // =========================
    // GET ALL EMPLOYEES
    // =========================

    public List<Employee> getAllEmployees() {
        return employeeRepository.getAllEmployees(getCompanyId());
    }

    // =========================
    // GET EMPLOYEE BY ID
    // =========================

    public Employee getEmployeeById(Integer id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }

        return employeeRepository
                .getEmployeeById(id, getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id));
    }

    // =========================
    // GET LOGGED-IN EMPLOYEE
    // =========================

    public Employee getMyProfile(String username) {

        User user = getCompanyUser(username);

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException(
                    "No employee profile linked to user: " + username);
        }

        Integer employeeId = user.getEmployee().getEmployeeId();

        return employeeRepository
                .getEmployeeById(employeeId, getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee profile not found"));
    }

    // =========================
    // UPDATE EMPLOYEE
    // =========================

    public Employee updateEmployee(
            Integer id,
            Employee employeeDetails) {

        if (employeeDetails == null) {
            throw new IllegalArgumentException(
                    "Employee details are required");
        }

        // Verify that the employee belongs to this company first.
        Employee existingEmployee = getEmployeeById(id);

        EmployeeUpdateDTO updateDetails = new EmployeeUpdateDTO();

        updateDetails.setFirstName(employeeDetails.getFirstName());
        updateDetails.setLastName(employeeDetails.getLastName());
        updateDetails.setEmail(employeeDetails.getEmail());
        updateDetails.setPhone(employeeDetails.getPhone());
        updateDetails.setDateOfBirth(employeeDetails.getDateOfBirth());
        updateDetails.setAddress(employeeDetails.getAddress());
        updateDetails.setProfilePhoto(existingEmployee.getProfilePhoto());

        if (employeeDetails.getDepartment() != null) {
            updateDetails.setDepartmentId(
                    employeeDetails.getDepartment().getDepartmentId());
        }

        updateDetails.setManagerId(employeeDetails.getManagerId());
        updateDetails.setHrId(employeeDetails.getHrId());

        /*
         * The target employee is company-validated above.
         * The update SQL itself still needs a company_id parameter;
         * that repository migration is a remaining step.
         */
        return employeeRepository.updateMyProfile(id, updateDetails);
    }

    // =========================
    // DELETE EMPLOYEE
    // =========================

    public void deleteEmployee(Integer id) {
        // Keep deletion disabled until its SQL function is company-scoped.
        getEmployeeById(id);

        throw new UnsupportedOperationException(
                "Delete employee is not yet implemented with company scoping");
    }

    // =========================
    // SEARCH BY FIRST NAME
    // =========================

    public List<Employee> searchByFirstName(String firstName) {
        return employeeRepository.searchByName(firstName, getCompanyId());
    }

    // =========================
    // SEARCH BY LAST NAME
    // =========================

    public List<Employee> searchByLastName(String lastName) {
        return employeeRepository.searchByName(lastName, getCompanyId());
    }

    // =========================
    // SEARCH BY NAME
    // =========================

    public List<Employee> searchByName(String name) {
        return employeeRepository.searchByName(name, getCompanyId());
    }

    // =========================
    // SEARCH BY EMAIL
    // =========================

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository
                .getEmployeeByEmail(email, getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with email: " + email));
    }

    // =========================
    // FILTER BY DESIGNATION
    // =========================

    public List<Employee> getEmployeesByDesignation(Integer designationId) {
        return employeeRepository.getEmployeesByDesignation(
                designationId, getCompanyId());
    }

    // =========================
    // SORT
    // =========================

    public List<Employee> getEmployeesSorted(
            String field,
            String direction) {

        throw new UnsupportedOperationException(
                "Sorting requires a company-scoped SQL function");
    }

    // =========================
    // UPDATE MY PROFILE
    // =========================

    public Employee updateMyProfile(
            String username,
            EmployeeUpdateDTO updateDetails) {

        Employee employee = getMyProfile(username);

        return employeeRepository.updateMyProfile(
                employee.getEmployeeId(),
                updateDetails);
    }

    // =========================
    // UPDATE PROFILE PHOTO
    // =========================

    public Employee updateProfilePhoto(
            String username,
            MultipartFile photo) {

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a profile photo");
        }

        Employee employee = getMyProfile(username);

        try {
            String uploadDir = "uploads/profile-photos/";
            Files.createDirectories(Paths.get(uploadDir));

            String originalName = Paths.get(
                    photo.getOriginalFilename() == null
                            ? "profile-photo"
                            : photo.getOriginalFilename()
            ).getFileName().toString();

            String fileName = UUID.randomUUID()
                    + "_" + originalName;

            Path filePath = Paths.get(uploadDir, fileName);

            Files.write(filePath, photo.getBytes());

            String profilePhoto = "/uploads/profile-photos/" + fileName;

            return employeeRepository.updateProfilePhoto(
                    employee.getEmployeeId(),
                    profilePhoto);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload profile photo", e);
        }
    }

    // =========================
    // ASSIGN MANAGER
    // =========================

    public Employee assignManager(
            Integer employeeId,
            Integer managerId) {

        // Both IDs must resolve within the logged-in company.
        getEmployeeById(employeeId);
        getEmployeeById(managerId);

        return employeeRepository.assignManager(employeeId, managerId);
    }

    // =========================
    // ASSIGN HR
    // =========================

    public Employee assignHr(
            Integer employeeId,
            Integer hrId) {

        getEmployeeById(employeeId);
        getEmployeeById(hrId);

        return employeeRepository.assignHr(employeeId, hrId);
    }

    // =========================
    // GET EMPLOYEES BY MANAGER
    // =========================

    public List<Employee> getEmployeesByManager(Integer managerId) {

        getEmployeeById(managerId);

        return employeeRepository.getEmployeesByManager(
                managerId, getCompanyId());
    }

    // =========================
    // GET EMPLOYEES BY HR
    // =========================

    public List<Employee> getEmployeesByHr(Integer hrId) {

        getEmployeeById(hrId);

        return employeeRepository.getEmployeesByHr(
                hrId, getCompanyId());
    }

    // =========================
    // GET MY PROFILE DETAILS
    // =========================

    public EmployeeProfileDTO getMyProfileDetails(String username) {

        User user = getCompanyUser(username);

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException(
                    "No employee profile linked to user: " + username);
        }

        Integer employeeId = user.getEmployee().getEmployeeId();

        // Ensure the employee belongs to the authenticated company.
        getEmployeeById(employeeId);

        return employeeRepository
                .getEmployeeProfileDetails(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee profile not found with id: "
                                        + employeeId));
    }

    // =========================
    // GET EMPLOYEES BY ROLE
    // =========================

    public List<Employee> getEmployeesByRole(Integer roleId) {

        if (roleId == null || (roleId != 2 && roleId != 3)) {
            throw new IllegalArgumentException(
                    "Only HR and Manager roles are allowed");
        }

        Long companyId = getCompanyId();

        List<User> users =
                userRepository.findByRoleRoleIdAndIsActive(roleId, true);

        List<Employee> employees = new ArrayList<>();

        for (User user : users) {

            // Filter users by the authenticated company.
            if (user.getCompanyId() == null
                    || !user.getCompanyId().equals(companyId)
                    || user.getEmployee() == null) {
                continue;
            }

            Integer employeeId = user.getEmployee().getEmployeeId();

            employeeRepository
                    .getEmployeeById(employeeId, companyId)
                    .ifPresent(employees::add);
        }

        return employees;
    }

    // =========================
    // SET EMPLOYEE ACTIVE STATUS
    // =========================

    public boolean setEmployeeActiveStatus(
            Integer employeeId,
            boolean isActive) {

        // Confirm that the employee belongs to this company.
        getEmployeeById(employeeId);

        return userRepository.setUserActiveStatus(
                employeeId,
                isActive,
                getCompanyId());
    }
}