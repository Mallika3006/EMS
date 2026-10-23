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

    public EmployeeRepository(DataSource dataSource,
                              CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }

    private Long companyId() {
        return companyContext.getCompanyId();
    }

    public List<Employee> getAllEmployees(Long companyId) {
        return queryEmployees(
                "SELECT * FROM public.get_all_employees(?)",
                ps -> ps.setLong(1, companyId));
    }

    public Optional<Employee> getEmployeeById(Integer id, Long companyId) {
        return queryOneEmployee(
                "SELECT * FROM public.get_employee_by_id_company(?, ?)",
                ps -> {
                    ps.setInt(1, id);
                    ps.setLong(2, companyId);
                });
    }

    public List<Employee> searchByName(String name, Long companyId) {
        return queryEmployees(
                "SELECT * FROM public.search_employee_by_name_company(?, ?)",
                ps -> {
                    ps.setString(1, name);
                    ps.setLong(2, companyId);
                });
    }

    public Optional<Employee> getEmployeeByEmail(
            String email, Long companyId) {
        return queryOneEmployee(
                "SELECT * FROM public.get_employee_by_email_company(?, ?)",
                ps -> {
                    ps.setString(1, email);
                    ps.setLong(2, companyId);
                });
    }

    public List<Employee> getEmployeesByDesignation(
            Integer designationId, Long companyId) {
        return queryEmployees(
                "SELECT * FROM public.get_employees_by_designation_company(?, ?)",
                ps -> {
                    ps.setInt(1, designationId);
                    ps.setLong(2, companyId);
                });
    }

    public List<Employee> getEmployeesByManager(
            Integer managerId, Long companyId) {
        return queryEmployees(
                "SELECT * FROM public.get_employees_by_manager_company(?, ?)",
                ps -> {
                    ps.setInt(1, managerId);
                    ps.setLong(2, companyId);
                });
    }

    public List<Employee> getEmployeesByHr(Integer hrId, Long companyId) {
        return queryEmployees(
                "SELECT * FROM public.get_employees_by_hr_company(?, ?)",
                ps -> {
                    ps.setInt(1, hrId);
                    ps.setLong(2, companyId);
                });
    }

    private List<Employee> queryEmployees(
            String sql, StatementBinder binder) {
        List<Employee> employees = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }
            return employees;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    private Optional<Employee> queryOneEmployee(
            String sql, StatementBinder binder) {
        List<Employee> employees = queryEmployees(sql, binder);
        return employees.stream().findFirst();
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();

        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));
        employee.setProfilePhoto(rs.getString("profile_photo"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            employee.setDateOfBirth(dob.toLocalDate());
        }

        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }

        Long companyId = rs.getObject("company_id", Long.class);

        Integer departmentId =
                rs.getObject("department_id", Integer.class);
        if (departmentId != null) {
            Department department = new Department();
            department.setDepartmentId(departmentId);

            if (companyId != null) {
                String sql = """
                    SELECT department_name
                    FROM public.department
                    WHERE department_id = ? AND company_id = ?
                    """;
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, departmentId);
                    ps.setLong(2, companyId);
                    try (ResultSet result = ps.executeQuery()) {
                        if (result.next()) {
                            department.setDepartmentName(
                                    result.getString("department_name"));
                        }
                    }
                }
            }
            employee.setDepartment(department);
        }

        Integer designationId =
                rs.getObject("designation_id", Integer.class);
        if (designationId != null) {
            Designation designation = new Designation();
            designation.setDesignationId(designationId);

            if (companyId != null) {
                String sql = """
                    SELECT designation_title
                    FROM public.designation
                    WHERE designation_id = ? AND company_id = ?
                    """;
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, designationId);
                    ps.setLong(2, companyId);
                    try (ResultSet result = ps.executeQuery()) {
                        if (result.next()) {
                            designation.setDesignationTitle(
                                    result.getString("designation_title"));
                        }
                    }
                }
            }
            employee.setDesignation(designation);
        }

        employee.setManagerId(
                rs.getObject("manager_id", Integer.class));
        employee.setHrId(rs.getObject("hr_id", Integer.class));

        return employee;
    }

    public Employee updateMyProfile(
            Integer employeeId, EmployeeUpdateDTO details) {
        Long companyId = companyId();

        validateDepartmentInCompany(
                details.getDepartmentId(), companyId);
        validateDesignationInCompany(details.getDesignationId(), companyId);

        validateEmployeeInCompany(
                details.getManagerId(), companyId, "Manager");

        validateEmployeeInCompany(
                details.getHrId(), companyId, "HR");

        String sql = """
            UPDATE public.employees
            SET first_name = ?,
                last_name = ?,
                email = ?,
                phone = ?,
                date_of_birth = ?,
                hire_date = ?,
                address = ?,
                profile_photo = ?,
                department_id = ?,
                designation_id = ?,
                manager_id = ?,
                hr_id = ?
            WHERE employee_id = ? AND company_id = ?
            RETURNING *
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, details.getFirstName());
            ps.setString(2, details.getLastName());
            ps.setString(3, details.getEmail());
            ps.setString(4, details.getPhone());
            setDate(ps, 5, details.getDateOfBirth());
            setDate(ps, 6, details.getHireDate());
            ps.setString(7, details.getAddress());
            ps.setString(8, details.getProfilePhoto());
            setNullableInteger(ps, 9, details.getDepartmentId());
            setNullableInteger(ps, 10, details.getDesignationId());
            setNullableInteger(ps, 11, details.getManagerId());
            setNullableInteger(ps, 12, details.getHrId());
            ps.setInt(13, employeeId);
            ps.setLong(14, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee", e);
        }

        throw new ResourceNotFoundException(
                "Employee not found in your company: " + employeeId);
    }

    public Employee updateProfilePhoto(
            Integer employeeId, String profilePhoto) {
        Employee employee = getEmployeeById(employeeId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setHireDate(employee.getHireDate());
        dto.setAddress(employee.getAddress());
        dto.setProfilePhoto(profilePhoto);
        dto.setDepartmentId(employee.getDepartment() == null
                ? null : employee.getDepartment().getDepartmentId());
        dto.setDesignationId(employee.getDesignation() == null
                ? null : employee.getDesignation().getDesignationId());
        dto.setManagerId(employee.getManagerId());
        dto.setHrId(employee.getHrId());

        return updateMyProfile(employeeId, dto);
    }

    public Employee assignManager(Integer employeeId, Integer managerId) {
        return assignRelationship(
                employeeId, managerId, "manager_id", "MANAGER");
    }

    public Employee assignHr(Integer employeeId, Integer hrId) {
        return assignRelationship(employeeId, hrId, "hr_id", "HR");
    }

    private Employee assignRelationship(
            Integer employeeId, Integer relatedId,
            String column, String roleName) {
        // Column names are internal constants, never request values.
        Long companyId = companyId();

        String sql = """
            UPDATE public.employees e
            SET %s = ?
            WHERE e.employee_id = ? AND e.company_id = ?
              AND (
                ? IS NULL OR EXISTS (
                    SELECT 1
                    FROM public.employees related
                    JOIN public.users u
                      ON u.employee_id = related.employee_id
                    JOIN public.roles r ON r.role_id = u.role_id
                    WHERE related.employee_id = ?
                      AND related.company_id = ?
                      AND u.company_id = ?
                      AND u.is_active = TRUE
                      AND r.role_name = ?
                )
              )
            RETURNING e.*
            """.formatted(column);

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setNullableInteger(ps, 1, relatedId);
            ps.setInt(2, employeeId);
            ps.setLong(3, companyId);
            setNullableInteger(ps, 4, relatedId);
            setNullableInteger(ps, 5, relatedId);
            ps.setLong(6, companyId);
            ps.setLong(7, companyId);
            ps.setString(8, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to assign " + roleName, e);
        }

        throw new ResourceNotFoundException(
                "Employee or assigned " + roleName
                        + " not found in your company");
    }

    public Optional<EmployeeProfileDTO> getEmployeeProfileDetails(
            Integer employeeId) {
        Long companyId = companyId();

        String sql = """
            SELECT e.employee_id, e.first_name, e.last_name, e.email,
                   e.phone, e.date_of_birth, e.hire_date, e.address,
                   e.profile_photo, e.department_id, d.department_name,
                   e.designation_id, des.designation_title,
                   e.manager_id,
                   concat(m.first_name, ' ', m.last_name) AS manager_name,
                   m.email AS manager_email, m.phone AS manager_phone,
                   e.hr_id,
                   concat(h.first_name, ' ', h.last_name) AS hr_name,
                   h.email AS hr_email, h.phone AS hr_phone
            FROM public.employees e
            LEFT JOIN public.department d
              ON d.department_id = e.department_id
             AND d.company_id = e.company_id
            LEFT JOIN public.designation des
              ON des.designation_id = e.designation_id
             AND des.company_id = e.company_id
            LEFT JOIN public.employees m
              ON m.employee_id = e.manager_id
             AND m.company_id = e.company_id
            LEFT JOIN public.employees h
              ON h.employee_id = e.hr_id
             AND h.company_id = e.company_id
            WHERE e.employee_id = ? AND e.company_id = ?
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setLong(2, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                EmployeeProfileDTO p = new EmployeeProfileDTO();
                p.setEmployeeId(rs.getInt("employee_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));

                Date dob = rs.getDate("date_of_birth");
                if (dob != null) p.setDateOfBirth(dob.toLocalDate());

                Date hireDate = rs.getDate("hire_date");
                if (hireDate != null) p.setHireDate(hireDate.toLocalDate());

                p.setAddress(rs.getString("address"));
                p.setProfilePhoto(rs.getString("profile_photo"));
                p.setDepartmentId(rs.getObject("department_id", Integer.class));
                p.setDepartmentName(rs.getString("department_name"));
                p.setDesignationId(rs.getObject("designation_id", Integer.class));
                p.setDesignationTitle(rs.getString("designation_title"));
                p.setManagerId(rs.getObject("manager_id", Integer.class));
                p.setManagerName(rs.getString("manager_name"));
                p.setManagerEmail(rs.getString("manager_email"));
                p.setManagerPhone(rs.getString("manager_phone"));
                p.setHrId(rs.getObject("hr_id", Integer.class));
                p.setHrName(rs.getString("hr_name"));
                p.setHrEmail(rs.getString("hr_email"));
                p.setHrPhone(rs.getString("hr_phone"));

                return Optional.of(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch employee profile details", e);
        }
    }

    public Employee createEmployeeWithUser(
            EmployeeCreateDTO dto, String encodedPassword) {
        Long companyId = companyId();

        String employeeSql = """
            INSERT INTO public.employees
              (first_name, last_name, email, phone, date_of_birth,
               hire_date, address, department_id, designation_id,
               manager_id, hr_id, company_id)
            SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            WHERE (? IS NULL OR EXISTS (
                SELECT 1 FROM public.department
                WHERE department_id = ? AND company_id = ?
            ))
              AND (? IS NULL OR EXISTS (
                SELECT 1 FROM public.designation
                WHERE designation_id = ? AND company_id = ?
            ))
              AND (? IS NULL OR EXISTS (
                SELECT 1 FROM public.employees
                WHERE employee_id = ? AND company_id = ?
            ))
              AND (? IS NULL OR EXISTS (
                SELECT 1 FROM public.employees
                WHERE employee_id = ? AND company_id = ?
            ))
            RETURNING *
            """;

        String userSql = """
            INSERT INTO public.users
              (username, password, is_active, employee_id, role_id, company_id)
            VALUES (?, ?, TRUE, ?, ?, ?)
            """;

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                Employee employee;

                try (PreparedStatement ps =
                             con.prepareStatement(employeeSql)) {
                    ps.setString(1, dto.getFirstName());
                    ps.setString(2, dto.getLastName());
                    ps.setString(3, dto.getEmail());
                    ps.setString(4, dto.getPhone());
                    setDate(ps, 5, dto.getDateOfBirth());
                    setDate(ps, 6, dto.getHireDate());
                    ps.setString(7, dto.getAddress());
                    setNullableInteger(ps, 8, dto.getDepartmentId());
                    setNullableInteger(ps, 9, dto.getDesignationId());
                    setNullableInteger(ps, 10, dto.getManagerId());
                    setNullableInteger(ps, 11, dto.getHrId());
                    ps.setLong(12, companyId);

                    setNullableInteger(ps, 13, dto.getDepartmentId());
                    setNullableInteger(ps, 14, dto.getDepartmentId());
                    ps.setLong(15, companyId);
                    setNullableInteger(ps, 16, dto.getDesignationId());
                    setNullableInteger(ps, 17, dto.getDesignationId());
                    ps.setLong(18, companyId);
                    setNullableInteger(ps, 19, dto.getManagerId());
                    setNullableInteger(ps, 20, dto.getManagerId());
                    ps.setLong(21, companyId);
                    setNullableInteger(ps, 22, dto.getHrId());
                    setNullableInteger(ps, 23, dto.getHrId());
                    ps.setLong(24, companyId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Department, designation, manager or HR "
                                            + "must belong to your company");
                        }
                        employee = mapEmployee(rs);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(userSql)) {
                    ps.setString(1, dto.getUsername());
                    ps.setString(2, encodedPassword);
                    ps.setInt(3, employee.getEmployeeId());
                    ps.setInt(4, dto.getRoleId());
                    ps.setLong(5, companyId);
                    ps.executeUpdate();
                }

                con.commit();
                return employee;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to create employee and login", e);
        }
    }

    public void deleteEmployee(Integer employeeId) {
        Long companyId = companyId();

        String deleteUser = """
            DELETE FROM public.users
            WHERE employee_id = ? AND company_id = ?
            """;
        String deleteEmployee = """
            DELETE FROM public.employees
            WHERE employee_id = ? AND company_id = ?
            """;

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(deleteUser)) {
                    ps.setInt(1, employeeId);
                    ps.setLong(2, companyId);
                    ps.executeUpdate();
                }

                int deleted;
                try (PreparedStatement ps =
                             con.prepareStatement(deleteEmployee)) {
                    ps.setInt(1, employeeId);
                    ps.setLong(2, companyId);
                    deleted = ps.executeUpdate();
                }

                if (deleted == 0) {
                    throw new ResourceNotFoundException(
                            "Employee not found in your company: " + employeeId);
                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete employee. Check dependent records.", e);
        }
    }


    private void validateDepartmentInCompany(
            Integer departmentId, Long companyId) {

        if (departmentId == null) {
            return;
        }

        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM public.department
            WHERE department_id = ?
              AND company_id = ?
        )
        """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            ps.setLong(2, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getBoolean(1)) {
                    throw new IllegalArgumentException(
                            "Department does not belong to your company");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to validate department", e);
        }
    }

    private void validateDesignationInCompany(
            Integer designationId, Long companyId) {
        if (designationId == null) {
            return;
        }
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM public.designation
                WHERE designation_id = ? AND company_id = ?
            )
            """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, designationId);
            ps.setLong(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new IllegalArgumentException(
                            "Designation does not belong to your company");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate designation", e);
        }
    }

    private void validateEmployeeInCompany(
            Integer relatedEmployeeId,
            Long companyId,
            String relationship) {

        if (relatedEmployeeId == null) {
            return;
        }

        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM public.employees
            WHERE employee_id = ?
              AND company_id = ?
        )
        """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, relatedEmployeeId);
            ps.setLong(2, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new IllegalArgumentException(
                            relationship
                                    + " does not belong to your company");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to validate " + relationship, e);
        }
    }


    private void setDate(PreparedStatement ps, int index,
                         java.time.LocalDate value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(value));
    }

    private void setNullableInteger(PreparedStatement ps, int index,
                                    Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
