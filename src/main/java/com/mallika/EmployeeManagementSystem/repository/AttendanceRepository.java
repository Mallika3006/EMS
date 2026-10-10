package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Attendance;
import com.mallika.EmployeeManagementSystem.model.Employee;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRepository {

    private final DataSource dataSource;

    public AttendanceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    // =========================================================
    // CREATE
    // =========================================================

    public Attendance createAttendance(
            Attendance attendance) {

        String sql =
                "SELECT * FROM create_attendance(?, ?, ?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            // ATT DATE
            if (attendance.getAttDate() != null) {
                statement.setDate(
                        1,
                        Date.valueOf(
                                attendance.getAttDate()
                        )
                );
            } else {
                statement.setNull(
                        1,
                        Types.DATE
                );
            }

            // STATUS
            statement.setString(
                    2,
                    attendance.getStatus()
            );

            // CHECK IN TIME
            if (attendance.getCheckInTime() != null) {
                statement.setTime(
                        3,
                        Time.valueOf(
                                attendance.getCheckInTime()
                        )
                );
            } else {
                statement.setNull(
                        3,
                        Types.TIME
                );
            }

            // CHECK OUT TIME
            if (attendance.getCheckOutTime() != null) {
                statement.setTime(
                        4,
                        Time.valueOf(
                                attendance.getCheckOutTime()
                        )
                );
            } else {
                statement.setNull(
                        4,
                        Types.TIME
                );
            }

            // EMPLOYEE
            if (attendance.getEmployee() != null) {
                statement.setInt(
                        5,
                        attendance.getEmployee()
                                .getEmployeeId()
                );
            } else {
                statement.setNull(
                        5,
                        Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAttendance(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error creating attendance",
                    e
            );
        }

        throw new RuntimeException(
                "Attendance could not be created"
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Attendance> getAllAttendance() {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_all_attendance()";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                attendanceList.add(
                        mapAttendance(resultSet)
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching attendance",
                    e
            );
        }

        return attendanceList;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<Attendance> getAttendanceById(
            Integer id) {

        String sql =
                "SELECT * FROM get_attendance_by_id(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching attendance with id: "
                            + id,
                    e
            );
        }

        return Optional.empty();
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Attendance updateAttendance(
            Integer id,
            Attendance attendanceDetails) {

        String sql =
                "SELECT * FROM update_attendance(?, ?, ?, ?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            // ATTENDANCE ID
            statement.setInt(1, id);

            // DATE
            if (attendanceDetails.getAttDate() != null) {
                statement.setDate(
                        2,
                        Date.valueOf(
                                attendanceDetails.getAttDate()
                        )
                );
            } else {
                statement.setNull(
                        2,
                        Types.DATE
                );
            }

            // STATUS
            statement.setString(
                    3,
                    attendanceDetails.getStatus()
            );

            // CHECK IN
            if (attendanceDetails.getCheckInTime() != null) {
                statement.setTime(
                        4,
                        Time.valueOf(
                                attendanceDetails
                                        .getCheckInTime()
                        )
                );
            } else {
                statement.setNull(
                        4,
                        Types.TIME
                );
            }

            // CHECK OUT
            if (attendanceDetails.getCheckOutTime() != null) {
                statement.setTime(
                        5,
                        Time.valueOf(
                                attendanceDetails
                                        .getCheckOutTime()
                        )
                );
            } else {
                statement.setNull(
                        5,
                        Types.TIME
                );
            }

            // EMPLOYEE
            if (attendanceDetails.getEmployee() != null) {
                statement.setInt(
                        6,
                        attendanceDetails
                                .getEmployee()
                                .getEmployeeId()
                );
            } else {
                statement.setNull(
                        6,
                        Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAttendance(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating attendance with id: "
                            + id,
                    e
            );
        }

        throw new RuntimeException(
                "Attendance not found with id: " + id
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    public boolean deleteAttendance(Integer id) {

        String sql =
                "SELECT delete_attendance(?)";

        try (Connection connection =
                     dataSource.getConnection();
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
                    "Error deleting attendance with id: "
                            + id,
                    e
            );
        }

        return false;
    }


    // =========================================================
    // BY EMPLOYEE
    // =========================================================

    public List<Attendance> findByEmployeeEmployeeId(
            Integer employeeId) {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_attendance_by_employee(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    attendanceList.add(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching attendance by employee",
                    e
            );
        }

        return attendanceList;
    }


    // =========================================================
    // BY DATE
    // =========================================================

    public List<Attendance> findByAttDate(
            LocalDate date) {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_attendance_by_date(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(date)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    attendanceList.add(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching attendance by date",
                    e
            );
        }

        return attendanceList;
    }


    // =========================================================
    // EMPLOYEE + DATE
    // =========================================================

    public List<Attendance>
    findByEmployeeEmployeeIdAndAttDate(
            Integer employeeId,
            LocalDate date) {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_employee_attendance_by_date(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            statement.setDate(
                    2,
                    Date.valueOf(date)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    attendanceList.add(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee attendance by date",
                    e
            );
        }

        return attendanceList;
    }


    // =========================================================
    // BY STATUS
    // =========================================================

    public List<Attendance> findByStatusIgnoreCase(
            String status) {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_attendance_by_status(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    attendanceList.add(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching attendance by status",
                    e
            );
        }

        return attendanceList;
    }

// =========================================================
// GET TEAM ATTENDANCE BY MANAGER
// =========================================================

    public List<Attendance> findTeamAttendanceByManager(
            Integer managerId) {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_team_attendance(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, managerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    attendanceList.add(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching team attendance",
                    e
            );
        }

        return attendanceList;
    }

    // =========================================================
    // EMPLOYEE + STATUS
    // =========================================================

    public List<Attendance>
    findByEmployeeEmployeeIdAndStatusIgnoreCase(
            Integer employeeId,
            String status) {

        List<Attendance> attendanceList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_employee_attendance_by_status(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            statement.setString(2, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    attendanceList.add(
                            mapAttendance(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee attendance by status",
                    e
            );
        }

        return attendanceList;
    }

    public List<Attendance> getAttendanceByHr(Integer hrId) {

        List<Attendance> attendanceList = new ArrayList<>();

        String sql = "{call get_attendance_by_hr(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {
            statement.setInt(1, hrId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    attendanceList.add(mapAttendance(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching attendance by HR", e
            );
        }

        return attendanceList;
    }


    // =========================================================
    // MAP RESULT SET
    // =========================================================

    private Attendance mapAttendance(
            ResultSet rs) throws SQLException {

        Attendance attendance =
                new Attendance();

        attendance.setAttendanceId(
                rs.getInt("attendance_id")
        );

        Date attDate =
                rs.getDate("att_date");

        if (attDate != null) {
            attendance.setAttDate(
                    attDate.toLocalDate()
            );
        }

        attendance.setStatus(
                rs.getString("status")
        );

        Time checkIn =
                rs.getTime("check_in_time");

        if (checkIn != null) {
            attendance.setCheckInTime(
                    checkIn.toLocalTime()
            );
        }

        Time checkOut =
                rs.getTime("check_out_time");

        if (checkOut != null) {
            attendance.setCheckOutTime(
                    checkOut.toLocalTime()
            );
        }

        int employeeId =
                rs.getInt("employee_id");

        if (!rs.wasNull()) {

            Employee employee =
                    new Employee();

            employee.setEmployeeId(employeeId);

            attendance.setEmployee(employee);
        }

        return attendance;
    }
}