package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Leave;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;
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
    private final CompanyContext companyContext;

    public LeaveRepository(DataSource dataSource, CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }

    // =========================================================
    // CREATE
    // =========================================================

    public Leave createLeave(Leave leave) {
        Integer employeeId = leave.getEmployee() == null
                ? null : leave.getEmployee().getEmployeeId();
        validateEmployeeInCompany(employeeId);

        String sql =
                "SELECT * FROM public.create_leave(?, ?, ?, ?, ?, ?)";

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

            statement.setLong(6, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_all_leaves(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {
            statement.setLong(1, companyContext.getCompanyId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    leaves.add(mapLeave(resultSet));
                }
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
                "SELECT * FROM public.get_leave_by_id(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            statement.setLong(2, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    Leave leave = mapLeave(resultSet);
                    Integer employeeId = leave.getEmployee() == null
                            ? null : leave.getEmployee().getEmployeeId();
                    if (employeeId != null
                            && isEmployeeInCompany(employeeId, companyContext.getCompanyId())) {
                        return Optional.of(leave);
                    }
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
        ensureLeaveInCompany(id);
        Integer employeeId = leaveDetails.getEmployee() == null
                ? null : leaveDetails.getEmployee().getEmployeeId();
        if (employeeId != null) validateEmployeeInCompany(employeeId);

        String sql =
                "SELECT * FROM public.update_leave(?, ?, ?, ?, ?, ?, ?)";

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

            statement.setLong(7, companyContext.getCompanyId());
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
        ensureLeaveInCompany(id);

        String sql =
                "SELECT * FROM public.withdraw_leave(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            statement.setLong(2, companyContext.getCompanyId());
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
        ensureLeaveInCompany(id);

        String sql =
                "SELECT public.delete_leave(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            statement.setLong(2, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_leaves_by_employee(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            statement.setLong(2, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_leaves_by_status(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, status);

            statement.setLong(2, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_employee_leaves_by_status(?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setString(2, status);

            statement.setLong(3, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_leaves_by_from_date(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(fromDate)
            );

            statement.setLong(2, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_leaves_by_to_date(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(toDate)
            );

            statement.setLong(2, companyContext.getCompanyId());
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
                "SELECT * FROM public.get_leaves_between_dates(?, ?, ?)";

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

            statement.setLong(3, companyContext.getCompanyId());
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
        ensureLeaveInCompany(leaveId);
        validateEmployeeInCompany(managerId);

        String sql =
                "SELECT * FROM public.update_manager_leave_status(?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, leaveId);
            statement.setInt(2, managerId);
            statement.setString(3, status);

            statement.setLong(4, companyContext.getCompanyId());
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
        int employeeId = rs.getInt("employee_id");

        if (!rs.wasNull()) {
            Employee employee = new Employee();

            employee.setEmployeeId(employeeId);
            // Legacy SETOF leaves functions do not all return employee name columns.
            try {
                employee.setFirstName(rs.getString("employee_first_name"));
            } catch (SQLException ignored) {
                // Name is optional for this result shape.
            }
            try {
                employee.setLastName(rs.getString("employee_last_name"));
            } catch (SQLException ignored) {
                // Name is optional for this result shape.
            }

            leave.setEmployee(employee);
        }

        return leave;
    }

    public List<Leave> getManagerLeaveRequests(Integer managerId) {
        validateEmployeeInCompany(managerId);

        List<Leave> leaves = new ArrayList<>();

        String sql = "SELECT * FROM public.get_manager_leave_requests(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, managerId);

            statement.setLong(2, companyContext.getCompanyId());
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
        validateEmployeeInCompany(hrId);

        List<Leave> leaves = new ArrayList<>();
        long companyId = companyContext.getCompanyId();

        // Query the tenant-scoped records directly instead of depending on a
        // PostgreSQL function that may not yet exist in an already-created DB.
        // Only active employees assigned to this HR in this company are returned.
        String sql = """
            SELECT l.leave_id,
                   l.from_date,
                   l.to_date,
                   l.reason,
                   l.status,
                   l.employee_id,
                   e.first_name AS employee_first_name,
                   e.last_name AS employee_last_name,
                   d.department_id,
                   d.department_name
            FROM public.leaves l
            JOIN public.employees e
              ON e.employee_id = l.employee_id
             AND e.company_id = l.company_id
            JOIN public.users u
              ON u.employee_id = e.employee_id
             AND u.company_id = e.company_id
             AND u.is_active = TRUE
            LEFT JOIN public.department d
              ON d.department_id = e.department_id
             AND d.company_id = e.company_id
            WHERE e.hr_id = ?
              AND e.company_id = ?
              AND l.company_id = ?
            ORDER BY l.leave_id DESC
            """;

        try (Connection connection = dataSource.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, hrId);
            statement.setLong(2, companyId);
            statement.setLong(3, companyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Leave leave = mapLeave(resultSet);
                    int departmentId = resultSet.getInt("department_id");
                    if (!resultSet.wasNull() && leave.getEmployee() != null) {
                        com.mallika.EmployeeManagementSystem.model.Department department =
                                new com.mallika.EmployeeManagementSystem.model.Department();
                        department.setDepartmentId(departmentId);
                        department.setDepartmentName(resultSet.getString("department_name"));
                        leave.getEmployee().setDepartment(department);
                    }
                    leaves.add(leave);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching HR employee leaves: " + e.getMessage(), e);
        }

        return leaves;
    }

// =========================================================
// DELETE LEAVE BY HR
// =========================================================

    public boolean deleteLeaveByHr(Integer leaveId, Integer hrId) {
        ensureLeaveInCompany(leaveId);
        validateEmployeeInCompany(hrId);

        String sql = "SELECT public.delete_leave_by_hr(?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, leaveId);
            statement.setInt(2, hrId);

            statement.setLong(3, companyContext.getCompanyId());
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
        ensureLeaveInCompany(leaveId);
        validateEmployeeInCompany(hrId);

        String sql = """
            UPDATE public.leaves AS l
               SET status = UPPER(?)
              FROM public.employees AS e
              JOIN public.users AS u
                ON u.employee_id = e.employee_id
               AND u.company_id = e.company_id
               AND u.is_active = TRUE
             WHERE l.employee_id = e.employee_id
               AND l.leave_id = ?
               AND e.hr_id = ?
               AND e.company_id = ?
               AND l.company_id = ?
               AND UPPER(l.status) = 'PENDING'
            RETURNING l.*
            """;

        try (Connection connection = dataSource.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, leaveId);
            statement.setInt(3, hrId);
            statement.setLong(4, companyContext.getCompanyId());
            statement.setLong(5, companyContext.getCompanyId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapLeave(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating HR leave status: " + e.getMessage(),
                    e
            );
        }

        throw new RuntimeException(
                "Leave request not found, not pending, or not assigned to this HR"
        );
    }

    private void ensureLeaveInCompany(Integer leaveId) {
        String sql = """
            SELECT 1
            FROM public.leaves l
            JOIN public.employees e ON e.employee_id = l.employee_id
            WHERE l.leave_id = ? AND e.company_id = ?
            """;
        try (Connection con = dataSource.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, leaveId);
            ps.setLong(2, companyContext.getCompanyId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Leave does not belong to your company");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate leave company", e);
        }
    }

    private void validateEmployeeInCompany(Integer employeeId) {
        if (employeeId == null || !isEmployeeInCompany(employeeId, companyContext.getCompanyId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Employee does not belong to your company");
        }
    }

    private boolean isEmployeeInCompany(Integer employeeId, Long companyId) {
        String sql = "SELECT 1 FROM public.employees WHERE employee_id = ? AND company_id = ?";
        try (Connection con = dataSource.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setLong(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate employee company", e);
        }
    }

}