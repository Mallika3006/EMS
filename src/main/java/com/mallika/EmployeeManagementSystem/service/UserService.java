package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import com.mallika.EmployeeManagementSystem.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("A password is required for a new account");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.createUser(user);
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public User getUserById(Integer id) {
        return userRepository.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    public User updateUser(Integer id, User userDetails) {
        User existingUser = getUserById(id);

        // The Admin form intentionally allows an empty password to mean
        // "keep the existing password". Never replace it with BCrypt(empty).
        if (userDetails.getPassword() == null || userDetails.getPassword().isBlank()) {
            userDetails.setPassword(existingUser.getPassword());
        } else {
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.updateUser(id, userDetails);
    }

    public void deleteUser(Integer id) {
        getUserById(id);
        userRepository.deleteUser(id);
    }

    public List<User> searchByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username));
    }

    public List<User> getUsersByActiveStatus(Boolean isActive) {
        return userRepository.findByIsActive(isActive);
    }

    public User getUserByEmployee(Integer employeeId) {
        return userRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for employee id: " + employeeId));
    }

    public List<User> getUsersByRole(Integer roleId) {
        return userRepository.findByRoleRoleId(roleId);
    }

    public List<User> getUsersByRoleAndStatus(Integer roleId, Boolean isActive) {
        return userRepository.findByRoleRoleIdAndIsActive(roleId, isActive);
    }

    public List<User> sortUsers(String field, String direction) {
        return userRepository.sortUsers(field, direction);
    }
}
