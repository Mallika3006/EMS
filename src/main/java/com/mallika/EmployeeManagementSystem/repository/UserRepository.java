package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Role;
import com.mallika.EmployeeManagementSystem.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    // CREATE
    public User createUser(User user) {

        String sql = "SELECT * FROM create_user(?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setBoolean(3, user.getIsActive());

            if (user.getEmployee() != null) {
                statement.setInt(
                        4,
                        user.getEmployee().getEmployeeId()
                );
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            if (user.getRole() != null) {
                statement.setInt(
                        5,
                        user.getRole().getRoleId()
                );
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

            throw new RuntimeException("Failed to create user");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error creating user: " + e.getMessage(), e
            );
        }
    }


    // GET ALL
    public List<User> getAllUsers() {

        String sql = "SELECT * FROM get_all_users()";

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching users: " + e.getMessage(), e
            );
        }
    }


    // GET BY ID
    public Optional<User> getUserById(Integer id) {

        String sql = "SELECT * FROM get_user_by_id(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching user by id: " + e.getMessage(), e
            );
        }
    }


    // UPDATE
    public User updateUser(Integer id, User user) {

        String sql = "SELECT * FROM update_user(?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, id);
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setBoolean(4, user.getIsActive());

            if (user.getEmployee() != null) {
                statement.setInt(
                        5,
                        user.getEmployee().getEmployeeId()
                );
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            if (user.getRole() != null) {
                statement.setInt(
                        6,
                        user.getRole().getRoleId()
                );
            } else {
                statement.setNull(6, Types.INTEGER);
            }

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

            throw new RuntimeException(
                    "User not found with id: " + id
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating user: " + e.getMessage(), e
            );
        }
    }


    // DELETE
    public void deleteUser(Integer id) {

        String sql = "SELECT delete_user(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {

                boolean deleted = rs.getBoolean(1);

                if (!deleted) {
                    throw new RuntimeException(
                            "User not found with id: " + id
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error deleting user: " + e.getMessage(), e
            );
        }
    }


    // EXACT USERNAME
    public Optional<User> findByUsernameIgnoreCase(
            String username) {

        String sql = "SELECT * FROM get_user_by_username(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error finding user by username: "
                            + e.getMessage(), e
            );
        }
    }


    // USERNAME
    // Used by CustomUserDetailsService for login
    public Optional<User> findByUsername(
            String username) {

        String sql = "SELECT * FROM find_user_by_username(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error finding user by username: "
                            + e.getMessage(), e
            );
        }
    }


    // SEARCH USERNAME
    public List<User> findByUsernameContainingIgnoreCase(
            String username) {

        String sql = "SELECT * FROM search_users_by_username(?)";

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error searching users: " + e.getMessage(), e
            );
        }
    }


    // ACTIVE / INACTIVE
    public List<User> findByIsActive(
            Boolean isActive) {

        String sql = "SELECT * FROM get_users_by_status(?)";

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setBoolean(1, isActive);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching users by status: "
                            + e.getMessage(), e
            );
        }
    }


    // BY EMPLOYEE
    public Optional<User> findByEmployeeEmployeeId(
            Integer employeeId) {

        String sql = "SELECT * FROM get_user_by_employee(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error finding user by employee: "
                            + e.getMessage(), e
            );
        }
    }


    // BY ROLE
    public List<User> findByRoleRoleId(
            Integer roleId) {

        String sql = "SELECT * FROM get_users_by_role(?)";

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, roleId);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching users by role: "
                            + e.getMessage(), e
            );
        }
    }


    // ROLE + ACTIVE STATUS
    public List<User> findByRoleRoleIdAndIsActive(
            Integer roleId,
            Boolean isActive) {

        String sql =
                "SELECT * FROM get_users_by_role_and_status(?, ?)";

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, roleId);
            statement.setBoolean(2, isActive);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching users by role and status: "
                            + e.getMessage(), e
            );
        }
    }


    // SORT
    public List<User> sortUsers(
            String field,
            String direction) {

        String sql = "SELECT * FROM sort_users(?, ?)";

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, field);
            statement.setString(2, direction);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error sorting users: " + e.getMessage(), e
            );
        }
    }


    // MAPPER
    private User mapUser(ResultSet rs) throws SQLException {

        User user = new User();

        user.setUserId(
                rs.getInt("user_id")
        );

        user.setUsername(
                rs.getString("username")
        );

        user.setPassword(
                rs.getString("password")
        );

        user.setIsActive(
                rs.getBoolean("is_active")
        );

        // Company ID
        long companyId = rs.getLong("company_id");

        if (rs.wasNull()) {
            user.setCompanyId(null);
        } else {
            user.setCompanyId(companyId);
        }

        // Employee relationship
        int employeeId = rs.getInt("employee_id");

        if (!rs.wasNull()) {
            Employee employee = new Employee();
            employee.setEmployeeId(employeeId);
            user.setEmployee(employee);
        }

        // Role relationship
        int roleId = rs.getInt("role_id");

        if (!rs.wasNull()) {
            Role role = new Role();
            role.setRoleId(roleId);
            user.setRole(role);
        }

        return user;
    }

    public boolean setUserActiveStatus(Integer employeeId, boolean isActive, Long companyId) {
        String sql = "SELECT public.set_user_active_status(?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setBoolean(2, isActive);
            statement.setLong(3, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee active status", e);
        }

        return false;
    }
}