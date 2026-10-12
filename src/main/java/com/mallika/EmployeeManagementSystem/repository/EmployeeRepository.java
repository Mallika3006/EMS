
package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.dto.EmployeeCreateDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeProfileDTO;
import com.mallika.EmployeeManagementSystem.dto.EmployeeUpdateDTO;
import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Department;
import com.mallika.EmployeeManagementSystem.model.Designation;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final DataSource dataSource;
    private final CompanyContext companyContext;

    public EmployeeRepository(
            DataSource dataSource,
            CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }

    // =========================
    // GET ALL EMPLOYEES
    // =========================

    public List<Employee> getAllEmployees(Long companyId) {

        String sql = "SELECT * FROM public.get_all_employees(?)";
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }

            return employees;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching employees", e);
        }
    }

    // =========================
    // GET EMPLOYEE BY ID
    // =========================

    public Optional<Employee> getEmployeeById(
            Integer id, Long companyId) {

        String sql =
                "SELECT * FROM public.get_employee_by_id_company(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEmployee(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee with id: " + id, e);
        }
    }

    // =========================
    // SEARCH BY NAME
    // =========================

    public List<Employee> searchByName(
            String name, Long companyId) {

        List<Employee> employees = new ArrayList<>();

        String sql =
                "SELECT * FROM public.search_employee_by_name_company(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }

            return employees;

        } catch (SQLException e) {
            throw new RuntimeException("Error searching employees", e);
        }
    }

    // =========================
    // SEARCH BY EMAIL
    // =========================

    public Optional<Employee> getEmployeeByEmail(
            String email, Long companyId) {

        String sql =
                "SELECT * FROM public.get_employee_by_email_company(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEmployee(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching employee by email", e);
        }
    }

    // =========================
    // FILTER BY DESIGNATION
    // =========================

    public List<Employee> getEmployeesByDesignation(
            Integer designationId, Long companyId) {

        List<Employee> employees = new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_employees_by_designation_company(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, designationId);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }

            return employees;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employees by designation", e);
        }
    }

    // =========================
    // MAP RESULT SET
    // =========================

    private Employee mapEmployee(ResultSet rs) throws SQLException {

        Employee employee = new Employee();

        Long companyId = rs.getObject("company_id", Long.class);

        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));
        employee.setProfilePhoto(rs.getString("profile_photo"));

        if (rs.getDate("date_of_birth") != null) {
            employee.setDateOfBirth(
                    rs.getDate("date_of_birth").toLocalDate());
        }

        if (rs.getDate("hire_date") != null) {
            employee.setHireDate(
                    rs.getDate("hire_date").toLocalDate());
        }

        // Designation lookup is company-scoped.
        Integer designationId =
                rs.getObject("designation_id", Integer.class);

        if (designationId != null) {
            Designation designation = new Designation();
            designation.setDesignationId(designationId);

            String sql =
                    "SELECT designation_title FROM public.designation " +
                            "WHERE designation_id = ? AND company_id = ?";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, designationId);
                statement.setLong(2, companyId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        designation.setDesignationTitle(
                                result.getString("designation_title"));
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Error fetching designation title", e);
            }

            employee.setDesignation(designation);
        }

        // Department lookup is company-scoped.
        Integer departmentId =
                rs.getObject("department_id", Integer.class);

        if (departmentId != null) {
            Department department = new Department();
            department.setDepartmentId(departmentId);

            String sql =
                    "SELECT department_name FROM public.department " +
                            "WHERE department_id = ? AND company_id = ?";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, departmentId);
                statement.setLong(2, companyId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        department.setDepartmentName(
                                result.getString("department_name"));
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Error fetching department name", e);
            }

            employee.setDepartment(department);
        }

        employee.setManagerId(
                rs.getObject("manager_id", Integer.class));

        employee.setHrId(
                rs.getObject("hr_id", Integer.class));

        return employee;
    }

    // =========================
    // UPDATE MY PROFILE
    // =========================

    public Employee updateMyProfile(
            Integer employeeId,
            EmployeeUpdateDTO details) {

        String sql =
                "{call update_employee_profile(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setString(2, details.getFirstName());
            statement.setString(3, details.getLastName());
            statement.setString(4, details.getEmail());
            statement.setString(5, details.getPhone());

            if (details.getDateOfBirth() != null) {
                statement.setDate(
                        6, Date.valueOf(details.getDateOfBirth()));
            } else {
                statement.setNull(6, Types.DATE);
            }

            statement.setString(7, details.getAddress());
            statement.setString(8, details.getProfilePhoto());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error updating employee profile", e);
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: " + employeeId);
    }

    // =========================
    // UPDATE PROFILE PHOTO
    // =========================

    public Employee updateProfilePhoto(
            Integer employeeId,
            String profilePhoto) {

        // FIX: use the authenticated company's ID.
        Long companyId = companyContext.getCompanyId();

        Employee employee = getEmployeeById(employeeId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId));

        EmployeeUpdateDTO details = new EmployeeUpdateDTO();
        details.setFirstName(employee.getFirstName());
        details.setLastName(employee.getLastName());
        details.setEmail(employee.getEmail());
        details.setPhone(employee.getPhone());
        details.setDateOfBirth(employee.getDateOfBirth());
        details.setAddress(employee.getAddress());
        details.setProfilePhoto(profilePhoto);

        /*
         * The existing update_employee_profile function is not yet
         * company-scoped. This verifies the employee belongs to the
         * authenticated company before invoking it, but the SQL function
         * must also be migrated to enforce isolation at the database level.
         */
        return updateMyProfile(employeeId, details);
    }

    // =========================
    // ASSIGN MANAGER
    // =========================

    public Employee assignManager(
            Integer employeeId, Integer managerId) {

        String sql = "{call assign_employee_manager(?, ?)}";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            if (managerId != null) {
                statement.setInt(2, managerId);
            } else {
                statement.setNull(2, Types.INTEGER);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error assigning manager to employee", e);
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: " + employeeId);
    }

    // =========================
    // ASSIGN HR
    // =========================

    public Employee assignHr(Integer employeeId, Integer hrId) {

        String sql = "{call assign_employee_hr(?, ?)}";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            if (hrId != null) {
                statement.setInt(2, hrId);
            } else {
                statement.setNull(2, Types.INTEGER);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error assigning HR to employee", e);
        }

        throw new ResourceNotFoundException(
                "Employee not found with id: " + employeeId);
    }

    // =========================
    // GET EMPLOYEES BY MANAGER
    // =========================

    public List<Employee> getEmployeesByManager(
            Integer managerId, Long companyId) {

        List<Employee> employees = new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_employees_by_manager_company(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, managerId);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }

            return employees;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employees by manager", e);
        }
    }

    // =========================
    // GET EMPLOYEES BY HR
    // =========================

    public List<Employee> getEmployeesByHr(
            Integer hrId, Long companyId) {

        List<Employee> employees = new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_employees_by_hr_company(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, hrId);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }

            return employees;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employees by HR", e);
        }
    }

    // =========================
    // GET EMPLOYEE PROFILE DETAILS
    // =========================

    public Optional<EmployeeProfileDTO> getEmployeeProfileDetails(
            Integer employeeId) {

        String sql = "{call get_employee_profile_details(?)}";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                EmployeeProfileDTO profile = new EmployeeProfileDTO();

                profile.setEmployeeId(rs.getInt("employee_id"));
                profile.setFirstName(rs.getString("first_name"));
                profile.setLastName(rs.getString("last_name"));
                profile.setEmail(rs.getString("email"));
                profile.setPhone(rs.getString("phone"));

                if (rs.getDate("date_of_birth") != null) {
                    profile.setDateOfBirth(
                            rs.getDate("date_of_birth").toLocalDate());
                }

                if (rs.getDate("hire_date") != null) {
                    profile.setHireDate(
                            rs.getDate("hire_date").toLocalDate());
                }

                profile.setAddress(rs.getString("address"));
                profile.setProfilePhoto(rs.getString("profile_photo"));

                profile.setDepartmentId(
                        rs.getObject("department_id", Integer.class));
                profile.setDepartmentName(rs.getString("department_name"));

                profile.setDesignationId(
                        rs.getObject("designation_id", Integer.class));
                profile.setDesignationTitle(
                        rs.getString("designation_title"));

                profile.setManagerId(
                        rs.getObject("manager_id", Integer.class));
                profile.setManagerName(rs.getString("manager_name"));
                profile.setManagerEmail(rs.getString("manager_email"));
                profile.setManagerPhone(rs.getString("manager_phone"));

                profile.setHrId(
                        rs.getObject("hr_id", Integer.class));
                profile.setHrName(rs.getString("hr_name"));
                profile.setHrEmail(rs.getString("hr_email"));
                profile.setHrPhone(rs.getString("hr_phone"));

                return Optional.of(profile);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee profile details", e);
        }
    }

    // =========================
    // CREATE EMPLOYEE WITH USER
    // =========================

    public Employee createEmployeeWithUser(
            EmployeeCreateDTO dto,
            String encodedPassword) {

        /*
         * The old function does not accept company_id.
         * Do not use it for multi-company employee creation.
         * We will replace it with a company-scoped SQL function next.
         */
        throw new UnsupportedOperationException(
                "Employee creation requires a company-scoped SQL function");
    }

    // =========================
    // NULLABLE INTEGER HELPER
    // =========================

    private void setNullableInteger(
            PreparedStatement statement,
            int index,
            Integer value) throws SQLException {

        if (value != null) {
            statement.setInt(index, value);
        } else {
            statement.setNull(index, Types.INTEGER);
        }
    }
}