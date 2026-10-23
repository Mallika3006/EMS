package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Attendance;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRepository {

    private final DataSource dataSource;
    private final CompanyContext companyContext;

    public AttendanceRepository(
            DataSource dataSource,
            CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }

    private Long companyId() {
        Long id = companyContext.getCompanyId();

        if (id == null) {
            throw new IllegalStateException(
                    "No company is associated with the authenticated user");
        }

        return id;
    }

    private static final String SELECT_ATTENDANCE = """
        SELECT a.attendance_id,
               a.att_date,
               a.status,
               a.check_in_time,
               a.check_out_time,
               a.employee_id,
               e.first_name AS employee_first_name,
               e.last_name AS employee_last_name
        FROM attendance a
        JOIN employees e ON e.employee_id = a.employee_id AND a.company_id = e.company_id
        WHERE e.company_id = ?
        """;

    // CREATE
    public Attendance createAttendance(Attendance attendance) {
        if (attendance == null
                || attendance.getEmployee() == null
                || attendance.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee is required");
        }

        Integer employeeId =
                attendance.getEmployee().getEmployeeId();

        String sql = """
            INSERT INTO attendance
                (att_date, status, check_in_time, check_out_time, employee_id, company_id)
            SELECT ?, ?, ?, ?, e.employee_id, e.company_id
            FROM employees e
            JOIN users u ON u.employee_id = e.employee_id
                       AND u.company_id = e.company_id
            WHERE e.employee_id = ?
              AND e.company_id = ?
              AND u.is_active = TRUE
              AND NOT EXISTS (
                  SELECT 1 FROM attendance existing
                  WHERE existing.employee_id = e.employee_id
                    AND existing.att_date = ?
                    AND existing.company_id = e.company_id
              )
            RETURNING attendance_id
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setAttendanceFields(ps, attendance, 1);
            ps.setInt(5, employeeId);
            ps.setLong(6, companyId());
            ps.setDate(7, Date.valueOf(attendance.getAttDate()));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "An attendance record may already exist for this employee and date, or the employee is inactive/outside your company");
                }

                int attendanceId = rs.getInt("attendance_id");
                return getAttendanceById(attendanceId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Created attendance could not be retrieved"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating attendance", e);
        }
    }

    // Employee self-service: create today's record or complete an existing open record.
    public Attendance checkIn(Integer employeeId) {
        Long currentCompanyId = companyId();
        int attendanceId = -1;
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                String findSql = """
                    SELECT a.attendance_id, a.status, a.check_in_time
                    FROM attendance a
                    JOIN employees e ON e.employee_id = a.employee_id
                    JOIN users u ON u.employee_id = e.employee_id AND u.company_id = e.company_id
                    WHERE a.employee_id = ? AND a.att_date = CURRENT_DATE
                      AND e.company_id = ? AND u.is_active = TRUE
                    ORDER BY a.attendance_id DESC LIMIT 1 FOR UPDATE OF a
                    """;
                try (PreparedStatement ps = con.prepareStatement(findSql)) {
                    ps.setInt(1, employeeId);
                    ps.setLong(2, currentCompanyId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            attendanceId = rs.getInt("attendance_id");
                            String status = rs.getString("status");
                            if (rs.getTime("check_in_time") != null) {
                                throw new IllegalArgumentException("You have already checked in today");
                            }
                            if ("leave".equalsIgnoreCase(status)) {
                                throw new IllegalArgumentException("Attendance cannot be started on a leave record");
                            }
                        }
                    }
                }
                if (attendanceId > 0) {
                    String updateSql = """
                        UPDATE attendance
                        SET check_in_time = CURRENT_TIME,
                            status = CASE WHEN LOWER(status) = 'absent' THEN 'Present' ELSE status END
                        WHERE attendance_id = ? AND company_id = ?
                        """;
                    try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                        ps.setInt(1, attendanceId);
                        ps.setLong(2, currentCompanyId);
                        if (ps.executeUpdate() != 1) throw new IllegalStateException("Could not record check-in");
                    }
                } else {
                    String insertSql = """
                        INSERT INTO attendance (att_date, status, check_in_time, check_out_time, employee_id, company_id)
                        SELECT CURRENT_DATE, 'Present', CURRENT_TIME, NULL, e.employee_id, e.company_id
                        FROM employees e
                        JOIN users u ON u.employee_id = e.employee_id AND u.company_id = e.company_id
                        WHERE e.employee_id = ? AND e.company_id = ? AND u.is_active = TRUE
                        RETURNING attendance_id
                        """;
                    try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                        ps.setInt(1, employeeId);
                        ps.setLong(2, currentCompanyId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new IllegalArgumentException(
                                    "Your employee account is inactive or not assigned to this company");
                            attendanceId = rs.getInt("attendance_id");
                        }
                    }
                }
                con.commit();
            } catch (RuntimeException | SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error recording check-in", e);
        }
        return getAttendanceById(attendanceId).orElseThrow(
                () -> new IllegalStateException("Checked-in attendance could not be retrieved"));
    }

    // Employee self-service: check out only from an open check-in for today.
    public Attendance checkOut(Integer employeeId) {
        Long currentCompanyId = companyId();
        int attendanceId;
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                String findSql = """
                    SELECT a.attendance_id
                    FROM attendance a
                    JOIN employees e ON e.employee_id = a.employee_id
                    JOIN users u ON u.employee_id = e.employee_id AND u.company_id = e.company_id
                    WHERE a.employee_id = ? AND a.att_date = CURRENT_DATE
                      AND a.company_id = ? AND e.company_id = ? AND u.is_active = TRUE
                      AND a.check_in_time IS NOT NULL AND a.check_out_time IS NULL
                    ORDER BY a.attendance_id DESC LIMIT 1 FOR UPDATE OF a
                    """;
                try (PreparedStatement ps = con.prepareStatement(findSql)) {
                    ps.setInt(1, employeeId);
                    ps.setLong(2, currentCompanyId);
                    ps.setLong(3, currentCompanyId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException(
                                "No open check-in was found for today");
                        attendanceId = rs.getInt("attendance_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE attendance SET check_out_time = CURRENT_TIME WHERE attendance_id = ? AND company_id = ? AND check_out_time IS NULL")) {
                    ps.setInt(1, attendanceId);
                    ps.setLong(2, currentCompanyId);
                    if (ps.executeUpdate() != 1) throw new IllegalArgumentException(
                            "You have already checked out today");
                }
                con.commit();
            } catch (RuntimeException | SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error recording check-out", e);
        }
        return getAttendanceById(attendanceId).orElseThrow(
                () -> new IllegalStateException("Checked-out attendance could not be retrieved"));
    }

    // GET ALL ATTENDANCE IN THE CURRENT COMPANY
    public List<Attendance> getAllAttendance() {
        return query(
                SELECT_ATTENDANCE + " ORDER BY a.att_date DESC, a.attendance_id DESC",
                companyId());
    }

    // GET BY ID, SCOPED TO COMPANY
    public Optional<Attendance> getAttendanceById(Integer id) {
        List<Attendance> rows = query(
                SELECT_ATTENDANCE + " AND a.attendance_id = ?",
                companyId(), id);

        return rows.stream().findFirst();
    }

    // UPDATE
    public Attendance updateAttendance(
            Integer id,
            Attendance details) {

        if (details == null
                || details.getEmployee() == null
                || details.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee is required");
        }

        String sql = """
            UPDATE attendance a
            SET att_date = ?,
                status = ?,
                check_in_time = ?,
                check_out_time = ?,
                employee_id = ?,
                company_id = ?
            WHERE a.attendance_id = ?
              AND EXISTS (
                  SELECT 1
                  FROM employees old_e
                  WHERE old_e.employee_id = a.employee_id
                    AND old_e.company_id = ?
              )
              AND EXISTS (
                  SELECT 1
                  FROM employees new_e
                  JOIN users u ON u.employee_id = new_e.employee_id
                             AND u.company_id = new_e.company_id
                  WHERE new_e.employee_id = ?
                    AND new_e.company_id = ?
                    AND u.is_active = TRUE
              )
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setAttendanceFields(ps, details, 1);
            ps.setInt(5, details.getEmployee().getEmployeeId());
            ps.setLong(6, companyId());
            ps.setInt(7, id);
            ps.setLong(8, companyId());
            ps.setInt(9, details.getEmployee().getEmployeeId());
            ps.setLong(10, companyId());

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Attendance or employee not found in your company");
            }

            return getAttendanceById(id)
                    .orElseThrow(() -> new IllegalStateException(
                            "Updated attendance could not be retrieved"));

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating attendance with id: " + id, e);
        }
    }

    // DELETE
    public boolean deleteAttendance(Integer id) {
        String sql = """
            DELETE FROM attendance a
            WHERE a.attendance_id = ?
              AND EXISTS (
                  SELECT 1
                  FROM employees e
                  WHERE e.employee_id = a.employee_id
                    AND e.company_id = ?
              )
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setLong(2, companyId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error deleting attendance with id: " + id, e);
        }
    }

    // BY EMPLOYEE
    public List<Attendance> findByEmployeeEmployeeId(Integer employeeId) {
        return query(
                SELECT_ATTENDANCE + " AND a.employee_id = ?"
                        + " ORDER BY a.att_date DESC, a.attendance_id DESC",
                companyId(), employeeId);
    }

    // BY DATE
    public List<Attendance> findByAttDate(LocalDate date) {
        return query(
                SELECT_ATTENDANCE + " AND a.att_date = ?"
                        + " ORDER BY a.attendance_id DESC",
                companyId(), date);
    }

    // BY EMPLOYEE AND DATE
    public List<Attendance> findByEmployeeEmployeeIdAndAttDate(
            Integer employeeId, LocalDate date) {
        return query(
                SELECT_ATTENDANCE
                        + " AND a.employee_id = ? AND a.att_date = ?"
                        + " ORDER BY a.attendance_id DESC",
                companyId(), employeeId, date);
    }

    // BY STATUS
    public List<Attendance> findByStatusIgnoreCase(String status) {
        return query(
                SELECT_ATTENDANCE + " AND LOWER(a.status) = LOWER(?)"
                        + " ORDER BY a.att_date DESC, a.attendance_id DESC",
                companyId(), status);
    }

    // BY EMPLOYEE AND STATUS
    public List<Attendance> findByEmployeeEmployeeIdAndStatusIgnoreCase(
            Integer employeeId, String status) {
        return query(
                SELECT_ATTENDANCE
                        + " AND a.employee_id = ? AND LOWER(a.status) = LOWER(?)"
                        + " ORDER BY a.att_date DESC, a.attendance_id DESC",
                companyId(), employeeId, status);
    }

    // MANAGER: TEAM ATTENDANCE
    public List<Attendance> findTeamAttendanceByManager(Integer managerId) {
        return query(
                SELECT_ATTENDANCE
                        + """
                          AND e.manager_id = ?
                          AND EXISTS (
                              SELECT 1
                              FROM users u
                              WHERE u.employee_id = e.employee_id
                                AND u.company_id = e.company_id
                                AND u.is_active = TRUE
                          )
                          ORDER BY a.att_date DESC, a.attendance_id DESC
                          """,
                companyId(), managerId);
    }

    // HR: ATTENDANCE OF ASSIGNED ACTIVE EMPLOYEES
    public List<Attendance> getAttendanceByHr(Integer hrId) {
        return query(
                SELECT_ATTENDANCE
                        + """
                          AND e.hr_id = ?
                          AND EXISTS (
                              SELECT 1
                              FROM users u
                              WHERE u.employee_id = e.employee_id
                                AND u.company_id = e.company_id
                                AND u.is_active = TRUE
                          )
                          ORDER BY a.att_date DESC, a.attendance_id DESC
                          """,
                companyId(), hrId);
    }

    private List<Attendance> query(String sql, Object... parameters) {
        List<Attendance> results = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bind(ps, parameters);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapAttendance(rs));
                }
            }

            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching attendance", e);
        }
    }

    private void bind(PreparedStatement ps, Object... parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object value = parameters[i];

            if (value == null) {
                ps.setNull(i + 1, Types.NULL);
            } else if (value instanceof LocalDate date) {
                ps.setDate(i + 1, Date.valueOf(date));
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    private void setAttendanceFields(
            PreparedStatement ps,
            Attendance attendance,
            int startIndex) throws SQLException {

        if (attendance.getAttDate() != null) {
            ps.setDate(startIndex, Date.valueOf(attendance.getAttDate()));
        } else {
            ps.setNull(startIndex, Types.DATE);
        }

        ps.setString(startIndex + 1, attendance.getStatus());

        if (attendance.getCheckInTime() != null) {
            ps.setTime(startIndex + 2,
                    Time.valueOf(attendance.getCheckInTime()));
        } else {
            ps.setNull(startIndex + 2, Types.TIME);
        }

        if (attendance.getCheckOutTime() != null) {
            ps.setTime(startIndex + 3,
                    Time.valueOf(attendance.getCheckOutTime()));
        } else {
            ps.setNull(startIndex + 3, Types.TIME);
        }
    }

    private Attendance mapAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();

        attendance.setAttendanceId(rs.getInt("attendance_id"));

        Date date = rs.getDate("att_date");
        if (date != null) {
            attendance.setAttDate(date.toLocalDate());
        }

        attendance.setStatus(rs.getString("status"));

        Time checkIn = rs.getTime("check_in_time");
        if (checkIn != null) {
            attendance.setCheckInTime(checkIn.toLocalTime());
        }

        Time checkOut = rs.getTime("check_out_time");
        if (checkOut != null) {
            attendance.setCheckOutTime(checkOut.toLocalTime());
        }

        int employeeId = rs.getInt("employee_id");
        if (!rs.wasNull()) {
            Employee employee = new Employee();
            employee.setEmployeeId(employeeId);
            employee.setFirstName(rs.getString("employee_first_name"));
            employee.setLastName(rs.getString("employee_last_name"));
            attendance.setEmployee(employee);
        }

        return attendance;
    }
}