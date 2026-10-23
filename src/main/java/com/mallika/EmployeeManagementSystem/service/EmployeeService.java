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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyContext companyContext;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CompanyContext companyContext) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyContext = companyContext;
    }

    private Long companyId() {
        return companyContext.getCompanyId();
    }

    private User getCompanyUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCompanyId() == null
                || !user.getCompanyId().equals(companyId())) {
            throw new AccessDeniedException(
                    "User does not belong to the authenticated company");
        }
        return user;
    }

    public Employee createEmployee(EmployeeCreateDTO dto) {
        if (dto == null
                || blank(dto.getFirstName())
                || blank(dto.getLastName())
                || blank(dto.getEmail())
                || blank(dto.getUsername())
                || blank(dto.getPassword())
                || dto.getRoleId() == null) {
            throw new IllegalArgumentException(
                    "First name, last name, email, username, password "
                            + "and role are required");
        }

        if (dto.getRoleId() < 2 || dto.getRoleId() > 4) {
            throw new IllegalArgumentException(
                    "Employee accounts must use the HR, Manager or Employee role");
        }

        if (employeeRepository.getEmployeeByEmail(
                dto.getEmail(), companyId()).isPresent()) {
            throw new IllegalArgumentException(
                    "Email already exists in this company");
        }

        return employeeRepository.createEmployeeWithUser(
                dto, passwordEncoder.encode(dto.getPassword()));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.getAllEmployees(companyId());
    }

    public Employee getEmployeeById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }
        return employeeRepository.getEmployeeById(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found in your company: " + id));
    }

    public Employee getMyProfile(String username) {
        User user = getCompanyUser(username);

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException(
                    "No employee profile linked to user: " + username);
        }
        return getEmployeeById(user.getEmployee().getEmployeeId());
    }

    public Employee updateEmployee(Integer id, Employee details) {
        if (details == null) {
            throw new IllegalArgumentException("Employee details required");
        }

        Employee existing = getEmployeeById(id);
        if (blank(details.getFirstName()) || blank(details.getLastName())
                || blank(details.getEmail())
                || !details.getEmail().matches("^[^\\\\s@]+@[^\\\\s@]+\\\\.[^\\\\s@]+$")) {
            throw new IllegalArgumentException(
                    "First name, last name and a valid email are required");
        }
        employeeRepository.getEmployeeByEmail(details.getEmail().trim(), companyId())
                .filter(found -> !found.getEmployeeId().equals(id))
                .ifPresent(found -> { throw new IllegalArgumentException(
                        "Email already exists in this company"); });
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();

        dto.setFirstName(details.getFirstName().trim());
        dto.setLastName(details.getLastName().trim());
        dto.setEmail(details.getEmail().trim());
        dto.setPhone(details.getPhone());
        dto.setDateOfBirth(details.getDateOfBirth());
        dto.setHireDate(details.getHireDate());
        dto.setAddress(details.getAddress());
        dto.setProfilePhoto(existing.getProfilePhoto());
        dto.setDepartmentId(details.getDepartment() == null
                ? existing.getDepartment() == null ? null
                  : existing.getDepartment().getDepartmentId()
                : details.getDepartment().getDepartmentId());
        dto.setDesignationId(details.getDesignation() == null
                ? existing.getDesignation() == null ? null
                  : existing.getDesignation().getDesignationId()
                : details.getDesignation().getDesignationId());
        dto.setManagerId(details.getManagerId());
        dto.setHrId(details.getHrId());

        validateRelatedEmployee(dto.getManagerId());
        validateRelatedEmployee(dto.getHrId());

        return employeeRepository.updateMyProfile(id, dto);
    }

    public void deleteEmployee(Integer id) {
        getEmployeeById(id);
        employeeRepository.deleteEmployee(id);
    }

    public List<Employee> searchByFirstName(String name) {
        return searchByName(name);
    }

    public List<Employee> searchByLastName(String name) {
        return searchByName(name);
    }

    public List<Employee> searchByName(String name) {
        if (blank(name)) {
            throw new IllegalArgumentException("Search name is required");
        }
        return employeeRepository.searchByName(name.trim(), companyId());
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.getEmployeeByEmail(email, companyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with email: " + email));
    }

    public List<Employee> getEmployeesByDesignation(Integer designationId) {
        return employeeRepository.getEmployeesByDesignation(
                designationId, companyId());
    }

    public List<Employee> getEmployeesSorted(
            String field, String direction) {
        List<Employee> employees = new ArrayList<>(getAllEmployees());

        Comparator<Employee> comparator = switch (
                field == null ? "firstname" : field.toLowerCase()) {
            case "lastname" -> Comparator.comparing(
                    Employee::getLastName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "email" -> Comparator.comparing(
                    Employee::getEmail,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "hiredate" -> Comparator.comparing(
                    Employee::getHireDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "firstname" -> Comparator.comparing(
                    Employee::getFirstName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default -> throw new IllegalArgumentException(
                    "Sort field must be firstName, lastName, email or hireDate");
        };

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        } else if (direction != null
                && !"asc".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException(
                    "Direction must be asc or desc");
        }

        employees.sort(comparator);
        return employees;
    }

    public Employee updateMyProfile(
            String username, EmployeeUpdateDTO details) {
        Employee existing = getMyProfile(username);

        // An employee cannot change their department, manager or HR
        // through the self-service profile endpoint.
        EmployeeUpdateDTO safe = new EmployeeUpdateDTO();
        safe.setFirstName(details.getFirstName());
        safe.setLastName(details.getLastName());
        safe.setEmail(details.getEmail());
        safe.setPhone(details.getPhone());
        safe.setDateOfBirth(details.getDateOfBirth());
        safe.setHireDate(existing.getHireDate());
        safe.setAddress(details.getAddress());
        safe.setProfilePhoto(existing.getProfilePhoto());
        safe.setDepartmentId(existing.getDepartment() == null ? null
                : existing.getDepartment().getDepartmentId());
        safe.setDesignationId(existing.getDesignation() == null ? null
                : existing.getDesignation().getDesignationId());
        safe.setManagerId(existing.getManagerId());
        safe.setHrId(existing.getHrId());

        return employeeRepository.updateMyProfile(
                existing.getEmployeeId(), safe);
    }

    public Employee updateProfilePhoto(
            String username, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a profile photo");
        }

        Employee employee = getMyProfile(username);

        try {
            Path directory = Paths.get("uploads", "profile-photos");
            Files.createDirectories(directory);

            String originalName = Paths.get(
                            photo.getOriginalFilename() == null
                                    ? "profile-photo"
                                    : photo.getOriginalFilename())
                    .getFileName().toString();

            String filename = UUID.randomUUID() + "_" + originalName;
            Files.write(directory.resolve(filename), photo.getBytes());

            return employeeRepository.updateProfilePhoto(
                    employee.getEmployeeId(),
                    "/uploads/profile-photos/" + filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile photo", e);
        }
    }

    private void validateRelatedEmployee(Integer id) {
        if (id != null) {
            getEmployeeById(id);
        }
    }

    public Employee assignManager(Integer employeeId, Integer managerId) {
        getEmployeeById(employeeId);
        validateRelatedEmployee(managerId);
        return employeeRepository.assignManager(employeeId, managerId);
    }

    public Employee assignHr(Integer employeeId, Integer hrId) {
        getEmployeeById(employeeId);
        validateRelatedEmployee(hrId);
        return employeeRepository.assignHr(employeeId, hrId);
    }

    public List<Employee> getEmployeesByManager(Integer managerId) {
        getEmployeeById(managerId);
        return employeeRepository.getEmployeesByManager(
                managerId, companyId());
    }

    public List<Employee> getEmployeesByHr(Integer hrId) {
        getEmployeeById(hrId);
        return employeeRepository.getEmployeesByHr(hrId, companyId());
    }

    public EmployeeProfileDTO getMyProfileDetails(String username) {
        Employee employee = getMyProfile(username);

        return employeeRepository.getEmployeeProfileDetails(
                        employee.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee profile details not found"));
    }

    public List<Employee> getEmployeesByRole(Integer roleId) {
        if (roleId == null || (roleId != 2 && roleId != 3)) {
            throw new IllegalArgumentException(
                    "Only HR and Manager roles are allowed");
        }

        Long companyId = companyId();
        List<Employee> result = new ArrayList<>();

        for (User user :
                userRepository.findByRoleRoleIdAndIsActive(roleId, true)) {
            if (user.getCompanyId() == null
                    || !user.getCompanyId().equals(companyId)
                    || user.getEmployee() == null) {
                continue;
            }

            employeeRepository.getEmployeeById(
                            user.getEmployee().getEmployeeId(), companyId)
                    .ifPresent(result::add);
        }
        return result;
    }

    public boolean setEmployeeActiveStatus(
            Integer employeeId, boolean active) {
        getEmployeeById(employeeId);
        return userRepository.setUserActiveStatus(
                employeeId, active, companyId());
    }
}