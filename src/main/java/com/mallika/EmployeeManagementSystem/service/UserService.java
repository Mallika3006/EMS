package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.RoleRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyContext companyContext;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, CompanyContext companyContext) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.companyContext = companyContext;
    }

    public User createUser(User user) {
        validateAccount(user, null, true);
        if (user.getPassword() == null || user.getPassword().isBlank())
            throw new IllegalArgumentException("A password is required for a new account");
        if (user.getPassword().length() < 8)
            throw new IllegalArgumentException("Password must contain at least 8 characters");
        user.setUsername(user.getUsername().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsActive(user.getIsActive() == null || user.getIsActive());
        return userRepository.createUser(user);
    }

    private void validateAccount(User user, Integer currentId, boolean creating) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank())
            throw new IllegalArgumentException("Username is required");
        String username = user.getUsername().trim();
        if (username.length() < 3 || username.length() > 50
                || !username.matches("^[A-Za-z0-9._-]+$"))
            throw new IllegalArgumentException("Username must be 3-50 characters and use only letters, numbers, dots, underscores or hyphens");
        if (user.getRole() == null || user.getRole().getRoleId() == null)
            throw new IllegalArgumentException("A role is required");
        Integer roleId = user.getRole().getRoleId();
        if (roleId < 2 || roleId > 4)
            throw new IllegalArgumentException("Only HR, Manager or Employee roles can be assigned through User Accounts");
        if (roleRepository.getRoleById(roleId).isEmpty())
            throw new IllegalArgumentException("Selected role does not exist");
        if (user.getEmployee() == null || user.getEmployee().getEmployeeId() == null)
            throw new IllegalArgumentException("Every non-administrator login account must be linked to an employee");
        if (userRepository.usernameExistsGlobally(username, currentId))
            throw new IllegalArgumentException("Username is already registered. Usernames must be unique across all companies.");
        var linkedAccount = userRepository.findByEmployeeEmployeeId(user.getEmployee().getEmployeeId());
        if (linkedAccount.isPresent()
                && (creating || !linkedAccount.get().getUserId().equals(currentId)))
            throw new IllegalArgumentException("This employee already has a login account");
    }

    public List<User> getAllUsers() { return userRepository.getAllUsers(); }

    public User getUserById(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid user account ID");
        return userRepository.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in your company: " + id));
    }

    public User updateUser(Integer id, User userDetails) {
        User existingUser = getUserById(id);
        validateAccount(userDetails, id, false);
        if (userDetails.getPassword() == null || userDetails.getPassword().isBlank()) {
            userDetails.setPassword(existingUser.getPassword());
        } else {
            if (userDetails.getPassword().length() < 8)
                throw new IllegalArgumentException("New password must contain at least 8 characters");
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        userDetails.setUsername(userDetails.getUsername().trim());
        userDetails.setIsActive(userDetails.getIsActive() == null
                ? existingUser.getIsActive() : userDetails.getIsActive());
        return userRepository.updateUser(id, userDetails);
    }

    public User setActiveStatus(Integer id, boolean active) {
        User user = getUserById(id);
        if (user.getEmployee() == null || user.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("Only employee-linked accounts can be activated or deactivated here");
        }
        userRepository.setUserActiveStatus(user.getEmployee().getEmployeeId(), active, user.getCompanyId());
        return getUserById(id);
    }

    public void deleteUser(Integer id) {
        User existing = getUserById(id);
        if (existing.getRole() != null && Integer.valueOf(1).equals(existing.getRole().getRoleId()))
            throw new IllegalArgumentException("Built-in administrator accounts cannot be deleted");
        if (existing.getUsername().equalsIgnoreCase(companyContext.getUsername()))
            throw new IllegalArgumentException("You cannot delete your own signed-in account");
        userRepository.deleteUser(id);
    }
    public List<User> searchByUsername(String username) { return userRepository.findByUsernameContainingIgnoreCase(username); }
    public User getByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
    public List<User> getUsersByActiveStatus(Boolean isActive) { return userRepository.findByIsActive(isActive); }
    public User getUserByEmployee(Integer employeeId) {
        return userRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for employee id: " + employeeId));
    }
    public List<User> getUsersByRole(Integer roleId) { return userRepository.findByRoleRoleId(roleId); }
    public List<User> getUsersByRoleAndStatus(Integer roleId, Boolean isActive) {
        return userRepository.findByRoleRoleIdAndIsActive(roleId, isActive);
    }
    public List<User> sortUsers(String field, String direction) { return userRepository.sortUsers(field, direction); }
}
