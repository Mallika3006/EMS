package com.mallika.EmployeeManagementSystem.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
public class CompanyRegistrationService {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public CompanyRegistrationService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerCompany(
            String companyName,
            String recoveryEmail,
            String recoveryPhone,
            String adminUsername,
            String password,
            String confirmPassword) {

        String normalizedCompanyName = companyName == null ? "" : companyName.trim();
        String normalizedUsername = adminUsername == null ? "" : adminUsername.trim();
        String normalizedRecoveryEmail = recoveryEmail == null ? "" : recoveryEmail.trim().toLowerCase();
        String normalizedRecoveryPhone = recoveryPhone == null ? "" : recoveryPhone.trim();

        if (!StringUtils.hasText(normalizedCompanyName) || normalizedCompanyName.length() > 200) {
            throw new IllegalArgumentException("Enter a company name between 1 and 200 characters.");
        }
        if (!StringUtils.hasText(normalizedRecoveryEmail)
                || normalizedRecoveryEmail.length() > 320
                || !normalizedRecoveryEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid organization recovery email address.");
        }
        if (normalizedRecoveryPhone.length() > 30) {
            throw new IllegalArgumentException("Recovery phone number must be 30 characters or fewer.");
        }
        if (!USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3–50 characters and use only letters, numbers, dots, underscores, or hyphens.");
        }
        if (password == null || password.length() < 10 || password.length() > 72) {
            throw new IllegalArgumentException("Password must be between 10 and 72 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        Long existingCompany = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE LOWER(company_name) = LOWER(?)",
                Long.class, normalizedCompanyName);
        if (existingCompany != null && existingCompany > 0) {
            throw new RegistrationConflictException("A company with that name is already registered.");
        }

        Long existingUsername = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)",
                Long.class, normalizedUsername);
        if (existingUsername != null && existingUsername > 0) {
            throw new RegistrationConflictException(
                    "That username is already in use. Choose another username.");
        }

        Integer adminRoleId = jdbcTemplate.query(
                "SELECT role_id FROM roles WHERE UPPER(role_name) = 'ADMIN' ORDER BY role_id LIMIT 1",
                rs -> rs.next() ? rs.getInt(1) : null);
        if (adminRoleId == null) {
            throw new IllegalStateException(
                    "The ADMIN role is missing. Ask the system administrator to run the EMS database setup.");
        }

        Long companyId = jdbcTemplate.queryForObject(
                "INSERT INTO companies (company_name, recovery_email, recovery_phone) VALUES (?, ?, ?)",
                Long.class, normalizedCompanyName, normalizedRecoveryEmail, normalizedRecoveryPhone.isBlank() ? null : normalizedRecoveryPhone);
        if (companyId == null) {
            throw new IllegalStateException("Could not create the company.");
        }

        jdbcTemplate.update("""
                INSERT INTO users
                    (username, password, is_active, employee_id, role_id, company_id)
                VALUES (?, ?, TRUE, NULL, ?, ?)
                """,
                normalizedUsername,
                passwordEncoder.encode(password),
                adminRoleId,
                companyId);
    }

    public static class RegistrationConflictException extends RuntimeException {
        public RegistrationConflictException(String message) {
            super(message);
        }
    }
}
