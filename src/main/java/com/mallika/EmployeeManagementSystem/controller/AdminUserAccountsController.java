package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Role;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.RoleRepository;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;
import com.mallika.EmployeeManagementSystem.service.EmployeeService;
import com.mallika.EmployeeManagementSystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-only account management API. Responses deliberately omit password hashes.
 * All reads and writes are scoped by the authenticated CompanyContext.
 */
@RestController
@RequestMapping("/admin/api/users")
public class AdminUserAccountsController {
    private final UserService userService;
    private final EmployeeService employeeService;
    private final RoleRepository roleRepository;
    private final CompanyContext companyContext;

    public AdminUserAccountsController(UserService userService,
                                       EmployeeService employeeService,
                                       RoleRepository roleRepository,
                                       CompanyContext companyContext) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.roleRepository = roleRepository;
        this.companyContext = companyContext;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return userService.getAllUsers().stream().map(this::view).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Integer id) {
        return view(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(view(created));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id,
                                      @RequestBody User user) {
        User existing = userService.getUserById(id);
        protectAdministrator(existing);
        return view(userService.updateUser(id, user));
    }

    @PatchMapping("/{id}/status")
    public Map<String, Object> setStatus(@PathVariable Integer id,
                                         @RequestParam boolean active) {
        User existing = userService.getUserById(id);
        protectAdministrator(existing);
        if (existing.getUsername().equalsIgnoreCase(companyContext.getUsername()) && !active) {
            throw new IllegalArgumentException("You cannot deactivate your own account while signed in.");
        }
        existing.setIsActive(active);
        return view(userService.updateUser(id, existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        User existing = userService.getUserById(id);
        protectAdministrator(existing);
        if (existing.getUsername().equalsIgnoreCase(companyContext.getUsername())) {
            throw new IllegalArgumentException("You cannot delete your own signed-in account.");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private void protectAdministrator(User user) {
        if (user.getRole() != null && Integer.valueOf(1).equals(user.getRole().getRoleId())) {
            throw new IllegalArgumentException("Built-in administrator accounts are protected from account-management changes.");
        }
    }

    private Map<String, Object> view(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("isActive", Boolean.TRUE.equals(user.getIsActive()));
        result.put("companyId", user.getCompanyId());

        if (user.getRole() != null && user.getRole().getRoleId() != null) {
            Role role = roleRepository.getRoleById(user.getRole().getRoleId()).orElse(null);
            if (role != null) {
                Map<String, Object> roleView = new LinkedHashMap<>();
                roleView.put("roleId", role.getRoleId());
                roleView.put("roleName", role.getRoleName());
                result.put("role", roleView);
                result.put("roleId", role.getRoleId());
            }
        }

        if (user.getEmployee() != null && user.getEmployee().getEmployeeId() != null) {
            try {
                Employee employee = employeeService.getEmployeeById(user.getEmployee().getEmployeeId());
                Map<String, Object> employeeView = new LinkedHashMap<>();
                employeeView.put("employeeId", employee.getEmployeeId());
                employeeView.put("firstName", employee.getFirstName());
                employeeView.put("lastName", employee.getLastName());
                employeeView.put("email", employee.getEmail());
                result.put("employee", employeeView);
                result.put("employeeId", employee.getEmployeeId());
            } catch (RuntimeException ignored) {
                // Never leak a cross-company employee; keep only the opaque relation ID out of the response.
            }
        }
        return result;
    }
}
