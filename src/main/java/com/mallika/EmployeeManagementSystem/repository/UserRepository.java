
package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Role;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DataSource dataSource;
    private final CompanyContext companyContext;

    public UserRepository(
            DataSource dataSource,
            CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }

    private Long companyId() {
        return companyContext.getCompanyId();
    }

    // CREATE USER IN THE AUTHENTICATED COMPANY
    public User createUser(User user) {
        Long companyId = companyId();

        String sql = """
            INSERT INTO users
                (username, password, is_active, employee_id, role_id, company_id)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING user_id, username, password, is_active,
                      employee_id, role_id, company_id
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, Boolean.TRUE.equals(user.getIsActive()));

            Integer employeeId = user.getEmployee() == null
                    ? null : user.getEmployee().getEmployeeId();

            Integer roleId = user.getRole() == null
                    ? null : user.getRole().getRoleId();

            if (employeeId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, employeeId);
            }

            if (roleId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, roleId);
            }

            ps.setLong(6, companyId);

            if (employeeId != null) {
                verifyEmployeeCompany(con, employeeId, companyId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }

            throw new SQLException("User creation returned no record.");

        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    // GET ALL USERS IN THIS COMPANY
    public List<User> getAllUsers() {
        return queryUsers("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE company_id = ?
            ORDER BY user_id
            """, ps -> ps.setLong(1, companyId()));
    }

    // GET USER BY ID, RESTRICTED TO THIS COMPANY
    public Optional<User> getUserById(Integer id) {
        return queryOne("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE user_id = ? AND company_id = ?
            """, ps -> {
            ps.setInt(1, id);
            ps.setLong(2, companyId());
        });
    }

    // UPDATE USER ONLY WITHIN THIS COMPANY
    public User updateUser(Integer id, User user) {
        Long companyId = companyId();

        String sql = """
            UPDATE users
            SET username = ?,
                password = ?,
                is_active = ?,
                employee_id = ?,
                role_id = ?
            WHERE user_id = ? AND company_id = ?
            RETURNING user_id, username, password, is_active,
                      employee_id, role_id, company_id
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Integer employeeId = user.getEmployee() == null
                    ? null : user.getEmployee().getEmployeeId();

            if (employeeId != null) {
                verifyEmployeeCompany(con, employeeId, companyId);
            }

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, Boolean.TRUE.equals(user.getIsActive()));

            if (employeeId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, employeeId);
            }

            Integer roleId = user.getRole() == null
                    ? null : user.getRole().getRoleId();

            if (roleId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, roleId);
            }

            ps.setInt(6, id);
            ps.setLong(7, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }

            throw new RuntimeException(
                    "User not found in your company: " + id);

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    // DELETE USER ONLY WITHIN THIS COMPANY
    public void deleteUser(Integer id) {
        String sql = """
            DELETE FROM users
            WHERE user_id = ? AND company_id = ?
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setLong(2, companyId());

            if (ps.executeUpdate() == 0) {
                throw new RuntimeException(
                        "User not found in your company: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    // LOGIN LOOKUP: MUST WORK BEFORE COMPANY CONTEXT EXISTS
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE username = ?
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user for login", e);
        }
    }

    // EXACT USERNAME WITHIN THE CURRENT COMPANY
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return queryOne("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE LOWER(username) = LOWER(?) AND company_id = ?
            """, ps -> {
            ps.setString(1, username);
            ps.setLong(2, companyId());
        });
    }

    // SEARCH USERNAME WITHIN THE CURRENT COMPANY
    public List<User> findByUsernameContainingIgnoreCase(String username) {
        return queryUsers("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE username ILIKE ? AND company_id = ?
            ORDER BY user_id
            """, ps -> {
            ps.setString(1, "%" + username + "%");
            ps.setLong(2, companyId());
        });
    }

    // ACTIVE / INACTIVE USERS
    public List<User> findByIsActive(Boolean isActive) {
        return queryUsers("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE is_active = ? AND company_id = ?
            ORDER BY user_id
            """, ps -> {
            ps.setBoolean(1, Boolean.TRUE.equals(isActive));
            ps.setLong(2, companyId());
        });
    }

    // USER BY EMPLOYEE
    public Optional<User> findByEmployeeEmployeeId(Integer employeeId) {
        return queryOne("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE employee_id = ? AND company_id = ?
            """, ps -> {
            ps.setInt(1, employeeId);
            ps.setLong(2, companyId());
        });
    }

    // USERS BY ROLE
    public List<User> findByRoleRoleId(Integer roleId) {
        return queryUsers("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE role_id = ? AND company_id = ?
            ORDER BY user_id
            """, ps -> {
            ps.setInt(1, roleId);
            ps.setLong(2, companyId());
        });
    }

    // USERS BY ROLE AND ACTIVE STATUS
    public List<User> findByRoleRoleIdAndIsActive(
            Integer roleId, Boolean isActive) {
        return queryUsers("""
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE role_id = ?
              AND is_active = ?
              AND company_id = ?
            ORDER BY user_id
            """, ps -> {
            ps.setInt(1, roleId);
            ps.setBoolean(2, Boolean.TRUE.equals(isActive));
            ps.setLong(3, companyId());
        });
    }

    // SORT USERS USING ALLOWED COLUMN NAMES ONLY
    public List<User> sortUsers(String field, String direction) {
        String column = switch (field == null
                ? "" : field.toLowerCase()) {
            case "userid" -> "user_id";
            case "username" -> "username";
            case "isactive" -> "is_active";
            default -> throw new IllegalArgumentException(
                    "Unsupported user sort field: " + field);
        };

        String order = "desc".equalsIgnoreCase(direction)
                ? "DESC" : "ASC";

        String sql = """
            SELECT user_id, username, password, is_active,
                   employee_id, role_id, company_id
            FROM users
            WHERE company_id = ?
            ORDER BY
            """ + column + " " + order;

        return queryUsers(sql, ps -> ps.setLong(1, companyId()));
    }

    // UPDATE EMPLOYEE ACCOUNT ACTIVE STATUS
    public boolean setUserActiveStatus(
            Integer employeeId, boolean isActive, Long requestedCompanyId) {

        Long authenticatedCompanyId = companyId();

        if (!authenticatedCompanyId.equals(requestedCompanyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot update users outside your company");
        }

        String sql = """
            UPDATE users
            SET is_active = ?
            WHERE employee_id = ? AND company_id = ?
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setInt(2, employeeId);
            ps.setLong(3, authenticatedCompanyId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update employee active status", e);
        }
    }

    // VERIFY EMPLOYEE BELONGS TO THE SAME COMPANY
    private void verifyEmployeeCompany(
            Connection con, Integer employeeId, Long companyId)
            throws SQLException {

        String sql = """
            SELECT 1
            FROM employees
            WHERE employee_id = ? AND company_id = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setLong(2, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Employee does not belong to your company");
                }
            }
        }
    }

    // COMMON QUERY HELPERS
    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<User> queryUsers(String sql, Binder binder) {
        List<User> users = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
    }

    private Optional<User> queryOne(String sql, Binder binder) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user", e);
        }
    }

    // RESULT SET MAPPER
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setIsActive(rs.getBoolean("is_active"));

        long companyId = rs.getLong("company_id");
        user.setCompanyId(rs.wasNull() ? null : companyId);

        int employeeId = rs.getInt("employee_id");
        if (!rs.wasNull()) {
            Employee employee = new Employee();
            employee.setEmployeeId(employeeId);
            user.setEmployee(employee);
        }

        int roleId = rs.getInt("role_id");
        if (!rs.wasNull()) {
            Role role = new Role();
            role.setRoleId(roleId);
            user.setRole(role);
        }

        return user;
    }
}