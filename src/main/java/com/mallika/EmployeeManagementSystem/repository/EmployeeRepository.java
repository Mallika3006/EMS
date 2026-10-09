package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Department;
import com.mallika.EmployeeManagementSystem.model.Employee;
import org.springframework.stereotype.Repository;
import com.mallika.EmployeeManagementSystem.model.Designation;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final DataSource dataSource;

    public EmployeeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    // =========================
    // GET ALL EMPLOYEES
    // =========================

    public List<Employee> getAllEmployees() {

        List<Employee> employees = new ArrayList<>();

        String sql = "{call get_all_employees()}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                employees.add(mapEmployee(resultSet));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employees", e
            );
        }

        return employees;
    }


    // =========================
    // GET EMPLOYEE BY ID
    // =========================

    public Optional<Employee> getEmployeeById(Integer id) {

        String sql = "{call get_employee_by_id(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapEmployee(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employee with id: " + id,
                    e
            );
        }

        return Optional.empty();
    }


    // =========================
    // SEARCH BY NAME
    // =========================

    public List<Employee> searchByName(String name) {

        List<Employee> employees = new ArrayList<>();

        String sql =
                "{call search_employee_by_name(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setString(1, name);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    employees.add(
                            mapEmployee(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error searching employees", e
            );
        }

        return employees;
    }


    // =========================
    // SEARCH BY EMAIL
    // =========================

    public Optional<Employee> getEmployeeByEmail(
            String email) {

        String sql =
                "{call get_employee_by_email(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setString(1, email);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapEmployee(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employee by email",
                    e
            );
        }

        return Optional.empty();
    }


    // =========================
    // FILTER BY DESIGNATION
    // =========================

    public List<Employee> getEmployeesByDesignation(
            Integer designationId) {

        List<Employee> employees =
                new ArrayList<>();

        String sql =
                "{call get_employees_by_designation(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(1, designationId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    employees.add(
                            mapEmployee(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employees by designation",
                    e
            );
        }

        return employees;
    }


    // =========================
    // MAP RESULTSET TO EMPLOYEE
    // =========================

    private Employee mapEmployee(ResultSet rs)
            throws SQLException {

        Employee employee = new Employee();

        employee.setEmployeeId(
                rs.getInt("employee_id")
        );

        employee.setFirstName(
                rs.getString("first_name")
        );

        employee.setLastName(
                rs.getString("last_name")
        );

        employee.setEmail(
                rs.getString("email")
        );

        employee.setPhone(
                rs.getString("phone")
        );

        if (rs.getDate("date_of_birth") != null) {

            employee.setDateOfBirth(
                    rs.getDate("date_of_birth")
                            .toLocalDate()
            );
        }

        if (rs.getDate("hire_date") != null) {

            employee.setHireDate(
                    rs.getDate("hire_date")
                            .toLocalDate()
            );
        }

        employee.setAddress(
                rs.getString("address")
        );

        employee.setProfilePhoto(
                rs.getString("profile_photo")
        );


        // =========================
        // DESIGNATION
        // =========================

        Integer designationId =
                rs.getObject("designation_id", Integer.class);

        if (designationId != null) {

            Designation designation = new Designation();

            designation.setDesignationId(designationId);

            String designationSql =
                    "SELECT designation_title FROM designation WHERE designation_id = ?";

            try (Connection connection = dataSource.getConnection();
                 java.sql.PreparedStatement statement =
                         connection.prepareStatement(designationSql)) {

                statement.setInt(1, designationId);

                try (ResultSet designationResult = statement.executeQuery()) {

                    if (designationResult.next()) {
                        designation.setDesignationTitle(
                                designationResult.getString("designation_title")
                        );
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Error fetching designation title", e
                );
            }

            employee.setDesignation(designation);
        }

        // =========================
        // DEPARTMENT
        // =========================

        Integer departmentId =
                rs.getObject("department_id", Integer.class);

        if (departmentId != null) {

            Department department = new Department();

            department.setDepartmentId(departmentId);

            String departmentSql =
                    "SELECT department_name FROM department WHERE department_id = ?";

            try (Connection connection = dataSource.getConnection();
                 java.sql.PreparedStatement statement =
                         connection.prepareStatement(departmentSql)) {

                statement.setInt(1, departmentId);

                try (ResultSet departmentResult = statement.executeQuery()) {

                    if (departmentResult.next()) {
                        department.setDepartmentName(
                                departmentResult.getString("department_name")
                        );
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Error fetching department name", e
                );
            }

            employee.setDepartment(department);
        }


        // =========================
        // MANAGER
        // =========================

        employee.setManagerId(
                rs.getObject(
                        "manager_id",
                        Integer.class
                )
        );


        // =========================
        // HR
        // =========================

        employee.setHrId(
                rs.getObject(
                        "hr_id",
                        Integer.class
                )
        );

        return employee;
    }


    // =========================
    // UPDATE MY PROFILE
    // =========================

    public Employee updateMyProfile(
            Integer employeeId,
            EmployeeUpdateDTO updateDetails) {

        String sql =
                "{call update_employee_profile(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(
                    1,
                    employeeId
            );

            statement.setString(
                    2,
                    updateDetails.getFirstName()
            );

            statement.setString(
                    3,
                    updateDetails.getLastName()
            );

            statement.setString(
                    4,
                    updateDetails.getEmail()
            );

            statement.setString(
                    5,
                    updateDetails.getPhone()
            );

            if (updateDetails.getDateOfBirth() != null) {

                statement.setDate(
                        6,
                        java.sql.Date.valueOf(
                                updateDetails.getDateOfBirth()
                        )
                );

            } else {

                statement.setNull(
                        6,
                        java.sql.Types.DATE
                );
            }

            statement.setString(
                    7,
                    updateDetails.getAddress()
            );

            statement.setString(
                    8,
                    updateDetails.getProfilePhoto()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapEmployee(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating employee profile",
                    e
            );
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: " + employeeId
        );
    }


    // =========================
    // UPDATE PROFILE PHOTO
    // =========================

    public Employee updateProfilePhoto(
            Integer employeeId,
            String profilePhoto) {

        Employee employee = getEmployeeById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: "
                                        + employeeId
                        )
                );

        String sql =
                "{call update_employee_profile(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(
                    1,
                    employeeId
            );

            statement.setString(
                    2,
                    employee.getFirstName()
            );

            statement.setString(
                    3,
                    employee.getLastName()
            );

            statement.setString(
                    4,
                    employee.getEmail()
            );

            statement.setString(
                    5,
                    employee.getPhone()
            );

            if (employee.getDateOfBirth() != null) {

                statement.setDate(
                        6,
                        java.sql.Date.valueOf(
                                employee.getDateOfBirth()
                        )
                );

            } else {

                statement.setNull(
                        6,
                        java.sql.Types.DATE
                );
            }

            statement.setString(
                    7,
                    employee.getAddress()
            );

            statement.setString(
                    8,
                    profilePhoto
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapEmployee(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating profile photo",
                    e
            );
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: " + employeeId
        );
    }


    // =========================
    // ASSIGN MANAGER
    // =========================

    public Employee assignManager(
            Integer employeeId,
            Integer managerId) {

        String sql =
                "{call assign_employee_manager(?, ?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(
                    1,
                    employeeId
            );

            if (managerId != null) {

                statement.setInt(
                        2,
                        managerId
                );

            } else {

                statement.setNull(
                        2,
                        java.sql.Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapEmployee(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error assigning manager to employee",
                    e
            );
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: "
                        + employeeId
        );
    }


    // =========================
    // ASSIGN HR
    // =========================

    public Employee assignHr(
            Integer employeeId,
            Integer hrId) {

        String sql =
                "{call assign_employee_hr(?, ?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(
                    1,
                    employeeId
            );

            if (hrId != null) {

                statement.setInt(
                        2,
                        hrId
                );

            } else {

                statement.setNull(
                        2,
                        java.sql.Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapEmployee(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error assigning HR to employee",
                    e
            );
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: "
                        + employeeId
        );
    }


    // =========================
    // GET EMPLOYEES BY MANAGER
    // =========================

    public List<Employee> getEmployeesByManager(
            Integer managerId) {

        List<Employee> employees =
                new ArrayList<>();

        String sql =
                "{call get_employees_by_manager(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(
                    1,
                    managerId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    employees.add(
                            mapEmployee(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employees by manager",
                    e
            );
        }

        return employees;
    }


    // =========================
    // GET EMPLOYEES BY HR
    // =========================

    public List<Employee> getEmployeesByHr(
            Integer hrId) {

        List<Employee> employees =
                new ArrayList<>();

        String sql =
                "{call get_employees_by_hr(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(
                    1,
                    hrId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    employees.add(
                            mapEmployee(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employees by HR",
                    e
            );
        }

        return employees;
    }

    // =========================
// GET EMPLOYEE PROFILE DETAILS
// =========================

    public Optional<EmployeeProfileDTO> getEmployeeProfileDetails(
            Integer employeeId) {

        String sql =
                "{call get_employee_profile_details(?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.setInt(1, employeeId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    EmployeeProfileDTO profile =
                            new EmployeeProfileDTO();

                    // =========================
                    // EMPLOYEE
                    // =========================

                    profile.setEmployeeId(
                            rs.getInt("employee_id")
                    );

                    profile.setFirstName(
                            rs.getString("first_name")
                    );

                    profile.setLastName(
                            rs.getString("last_name")
                    );

                    profile.setEmail(
                            rs.getString("email")
                    );

                    profile.setPhone(
                            rs.getString("phone")
                    );


                    // =========================
                    // DATE OF BIRTH
                    // =========================

                    if (rs.getDate("date_of_birth") != null) {

                        profile.setDateOfBirth(
                                rs.getDate("date_of_birth")
                                        .toLocalDate()
                        );
                    }


                    // =========================
                    // HIRE DATE
                    // =========================

                    if (rs.getDate("hire_date") != null) {

                        profile.setHireDate(
                                rs.getDate("hire_date")
                                        .toLocalDate()
                        );
                    }


                    profile.setAddress(
                            rs.getString("address")
                    );

                    profile.setProfilePhoto(
                            rs.getString("profile_photo")
                    );


                    // =========================
                    // DEPARTMENT
                    // =========================

                    profile.setDepartmentId(
                            rs.getObject(
                                    "department_id",
                                    Integer.class
                            )
                    );

                    profile.setDepartmentName(
                            rs.getString("department_name")
                    );


                    // =========================
                    // DESIGNATION
                    // =========================

                    profile.setDesignationId(
                            rs.getObject(
                                    "designation_id",
                                    Integer.class
                            )
                    );

                    profile.setDesignationTitle(
                            rs.getString("designation_title")
                    );


                    // =========================
                    // MANAGER
                    // =========================

                    profile.setManagerId(
                            rs.getObject(
                                    "manager_id",
                                    Integer.class
                            )
                    );

                    profile.setManagerName(
                            rs.getString("manager_name")
                    );

                    profile.setManagerEmail(
                            rs.getString("manager_email")
                    );

                    profile.setManagerPhone(
                            rs.getString("manager_phone")
                    );


                    // =========================
                    // HR
                    // =========================

                    profile.setHrId(
                            rs.getObject(
                                    "hr_id",
                                    Integer.class
                            )
                    );

                    profile.setHrName(
                            rs.getString("hr_name")
                    );

                    profile.setHrEmail(
                            rs.getString("hr_email")
                    );

                    profile.setHrPhone(
                            rs.getString("hr_phone")
                    );

                    return Optional.of(profile);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employee profile details",
                    e
            );
        }

        return Optional.empty();
    }
}