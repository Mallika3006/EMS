package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.model.Role;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.RoleRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            RoleRepository roleRepository) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Find user using JDBC UserRepository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username));

        // Make sure the user has a role
        if (user.getRole() == null || user.getRole().getRoleId() == null) {
            throw new UsernameNotFoundException(
                    "No role assigned to user: " + username);
        }

        // Fetch complete role using JDBC RoleRepository
        Role role = roleRepository.getRoleById(
                        user.getRole().getRoleId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Role not found for user: " + username));

        // Make sure role name exists
        if (role.getRoleName() == null ||
                role.getRoleName().isBlank()) {

            throw new UsernameNotFoundException(
                    "Role name not found for user: " + username);
        }


        UserDetails springUser =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(role.getRoleName().toUpperCase(java.util.Locale.ROOT))
                        .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                        .build();

        return new CompanyUserDetails(
                springUser,
                user.getCompanyId()
        );

    }
}