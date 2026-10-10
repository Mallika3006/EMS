package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Leave;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class LeaveRepository {

    private final DataSource dataSource;

    public LeaveRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // =========================================================
    // CREATE
    // =========================================================

    public Leave createLeave(Leave leave) {

        String sql =
                "SELECT * FROM create_leave(?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            // FROM DATE
            if (leave.getFromDate() != null) {
                statement.setDate(
                        1,
                        Date.valueOf(leave.getFromDate())
                );
            } else {
                statement.setNull(
                        1,
                        java.sql.Types.DATE
                );
            }

            // TO DATE
            if (leave.getToDate() != null) {
                statement.setDate(
                        2,
                        Date.valueOf(leave.getToDate())
                );
            } else {
                statement.setNull(
                        2,
                        java.sql.Types.DATE
                );
            }

            // REASON
            statement.setString(
                    3,
                    leave.getReason()
            );

            // STATUS
            statement.setString(
                    4,
                    leave.getStatus()
            );

            // EMPLOYEE ID
            if (leave.getEmployee() != null) {
                statement.setInt(
                        5,
                        leave.getEmployee().getEmployeeId()
                );
            } else {
                statement.setNull(
                        5,
                        java.sql.Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapLeave(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error creating leave",
                    e
            );
        }

        throw new RuntimeException(
                "Leave could not be created"
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Leave> getAllLeaves() {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_all_leaves()";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                leaves.add(mapLeave(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leaves",
                    e
            );
        }

        return leaves;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<Leave> getLeaveById(Integer id) {

        String sql =
                "SELECT * FROM get_leave_by_id(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapLeave(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leave with id: " + id,
                    e
            );
        }

        return Optional.empty();
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Leave updateLeave(
            Integer id,
            Leave leaveDetails) {

        String sql =
                "SELECT * FROM update_leave(?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            // LEAVE ID
            statement.setInt(1, id);

            // FROM DATE
            if (leaveDetails.getFromDate() != null) {
                statement.setDate(
                        2,
                        Date.valueOf(
                                leaveDetails.getFromDate()
                        )
                );
            } else {
                statement.setNull(
                        2,
                        java.sql.Types.DATE
                );
            }

            // TO DATE
            if (leaveDetails.getToDate() != null) {
                statement.setDate(
                        3,
                        Date.valueOf(
                                leaveDetails.getToDate()
                        )
                );
            } else {
                statement.setNull(
                        3,
                        java.sql.Types.DATE
                );
            }

            // REASON
            statement.setString(
                    4,
                    leaveDetails.getReason()
            );

            // STATUS
            statement.setString(
                    5,
                    leaveDetails.getStatus()
            );

            // EMPLOYEE
            if (leaveDetails.getEmployee() != null) {
                statement.setInt(
                        6,
                        leaveDetails
                                .getEmployee()
                                .getEmployeeId()
                );
            } else {
                statement.setNull(
                        6,
                        java.sql.Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapLeave(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating leave with id: " + id,
                    e
            );
        }

        throw new RuntimeException(
                "Leave not found with id: " + id
        );
    }

    // =========================================================
// WITHDRAW LEAVE
// =========================================================

    public Leave withdrawLeave(Integer id) {

        String sql =
                "SELECT * FROM withdraw_leave(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapLeave(resultSet);
                }
            }

        } catch (SQLException e) {

            // TEMPORARY DEBUG
            throw new RuntimeException(
                    "Database error while withdrawing leave " +
                            id + ": " +
                            e.getMessage(),
                    e
            );
        }

        throw new RuntimeException(
                "Leave could not be withdrawn. " +
                        "Leave may not exist or may not be PENDING."
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    public boolean deleteLeave(Integer id) {

        String sql =
                "SELECT delete_leave(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error deleting leave with id: " + id,
                    e
            );
        }

        return false;
    }


    // =========================================================
    // BY EMPLOYEE
    // =========================================================

    public List<Leave> findByEmployeeEmployeeId(
            Integer employeeId) {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_leaves_by_employee(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leaves by employee",
                    e
            );
        }

        return leaves;
    }


    // =========================================================
    // BY STATUS
    // =========================================================

    public List<Leave> findByStatusIgnoreCase(
            String status) {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_leaves_by_status(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leaves by status",
                    e
            );
        }

        return leaves;
    }


    // =========================================================
    // EMPLOYEE + STATUS
    // =========================================================

    public List<Leave>
    findByEmployeeEmployeeIdAndStatusIgnoreCase(
            Integer employeeId,
            String status) {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_employee_leaves_by_status(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setString(2, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee leaves by status",
                    e
            );
        }

        return leaves;
    }


    // =========================================================
    // BY FROM DATE
    // =========================================================

    public List<Leave> findByFromDate(
            java.time.LocalDate fromDate) {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_leaves_by_from_date(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(fromDate)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leaves by from date",
                    e
            );
        }

        return leaves;
    }


    // =========================================================
    // BY TO DATE
    // =========================================================

    public List<Leave> findByToDate(
            java.time.LocalDate toDate) {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_leaves_by_to_date(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(toDate)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leaves by to date",
                    e
            );
        }

        return leaves;
    }


    // =========================================================
    // BETWEEN DATES
    // =========================================================

    public List<Leave> findByFromDateBetween(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {

        List<Leave> leaves = new ArrayList<>();

        String sql =
                "SELECT * FROM get_leaves_between_dates(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(startDate)
            );

            statement.setDate(
                    2,
                    Date.valueOf(endDate)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching leaves between dates",
                    e
            );
        }

        return leaves;
    }

    // =========================================================
// UPDATE MANAGER LEAVE STATUS
// =========================================================

    public Leave updateManagerLeaveStatus(
            Integer leaveId,
            Integer managerId,
            String status) {

        String sql =
                "SELECT * FROM update_manager_leave_status(?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, leaveId);
            statement.setInt(2, managerId);
            statement.setString(3, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapLeave(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating manager leave status",
                    e
            );
        }

        throw new RuntimeException(
                "Leave request not found, not pending, " +
                        "or does not belong to this manager's team"
        );
    }


    // =========================================================
    // MAP RESULT SET TO LEAVE
    // =========================================================

    private Leave mapLeave(
            ResultSet rs) throws SQLException {

        Leave leave = new Leave();

        leave.setLeaveId(
                rs.getInt("leave_id")
        );

        Date fromDate =
                rs.getDate("from_date");

        if (fromDate != null) {
            leave.setFromDate(
                    fromDate.toLocalDate()
            );
        }

        Date toDate =
                rs.getDate("to_date");

        if (toDate != null) {
            leave.setToDate(
                    toDate.toLocalDate()
            );
        }

        leave.setReason(
                rs.getString("reason")
        );

        leave.setStatus(
                rs.getString("status")
        );

        // Employee relationship
        int employeeId =
                rs.getInt("employee_id");

        if (!rs.wasNull()) {

            Employee employee =
                    new Employee();

            employee.setEmployeeId(employeeId);

            leave.setEmployee(employee);
        }

        return leave;
    }

    public List<Leave> getManagerLeaveRequests(Integer managerId) {

        List<Leave> leaves = new ArrayList<>();

        String sql = "SELECT * FROM get_manager_leave_requests(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, managerId);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                leaves.add(mapLeave(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching manager leave requests", e);
        }

        return leaves;
    }

    // =========================================================
// GET LEAVES BY HR
// =========================================================

    public List<Leave> getLeavesByHr(Integer hrId) {

        List<Leave> leaves = new ArrayList<>();

        String sql = "SELECT * FROM get_leaves_by_hr(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, hrId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching HR employee leaves", e);
        }

        return leaves;
    }


// =========================================================
// DELETE LEAVE BY HR
// =========================================================

    public boolean deleteLeaveByHr(Integer leaveId, Integer hrId) {

        String sql = "SELECT delete_leave_by_hr(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, leaveId);
            statement.setInt(2, hrId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting HR employee leave", e);
        }

        return false;
    }

    // =========================================================
// UPDATE HR LEAVE STATUS
// =========================================================

    public Leave updateHrLeaveStatus(
            Integer leaveId,
            Integer hrId,
            String status) {

        String sql =
                "SELECT * FROM update_hr_leave_status(?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, leaveId);
            statement.setInt(2, hrId);
            statement.setString(3, status);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapLeave(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating HR leave status",
                    e
            );
        }

        throw new RuntimeException(
                "Leave request not found, not pending, " +
                        "or not assigned to this HR"
        );
    }
}