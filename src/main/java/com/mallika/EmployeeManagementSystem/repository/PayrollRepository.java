package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Payroll;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PayrollRepository {

    private final DataSource dataSource;
    private final CompanyContext companyContext;

    public PayrollRepository(DataSource dataSource, CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }


    // =========================================================
    // CREATE
    // =========================================================

    public Payroll createPayroll(Payroll payroll) {
        Integer employeeId = payroll.getEmployee() == null
                ? null : payroll.getEmployee().getEmployeeId();
        validateEmployeeInCompany(employeeId);

        String sql =
                "SELECT * FROM public.create_payroll(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(
                    1,
                    payroll.getPayMonth()
            );

            statement.setBigDecimal(
                    2,
                    payroll.getBasicSalary()
            );

            statement.setBigDecimal(
                    3,
                    payroll.getAllowances()
            );

            statement.setBigDecimal(
                    4,
                    payroll.getDeductions()
            );

            statement.setBigDecimal(
                    5,
                    payroll.getNetSalary()
            );

            if (payroll.getPaymentDate() != null) {
                statement.setDate(
                        6,
                        Date.valueOf(
                                payroll.getPaymentDate()
                        )
                );
            } else {
                statement.setNull(
                        6,
                        Types.DATE
                );
            }

            if (payroll.getEmployee() != null) {
                statement.setInt(
                        7,
                        payroll.getEmployee()
                                .getEmployeeId()
                );
            } else {
                statement.setNull(
                        7,
                        Types.INTEGER
                );
            }

            statement.setLong(8, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPayroll(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error creating payroll",
                    e
            );
        }

        throw new RuntimeException(
                "Payroll could not be created"
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Payroll> getAllPayrolls() {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_all_payrolls(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {
            statement.setLong(1, companyContext.getCompanyId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payrolls.add(mapPayroll(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching payrolls",
                    e
            );
        }

        return filterPayrollsByCompany(payrolls);
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<Payroll> getPayrollById(
            Integer id) {

        String sql =
                "SELECT * FROM public.get_payroll_by_id(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            statement.setLong(2, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    Payroll payroll = mapPayroll(resultSet);
                    if (isEmployeeInCompany(
                            payroll.getEmployee() == null ? null : payroll.getEmployee().getEmployeeId(),
                            companyContext.getCompanyId())) {
                        return Optional.of(payroll);
                    }
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching payroll with id: "
                            + id,
                    e
            );
        }

        return Optional.empty();
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Payroll updatePayroll(
            Integer id,
            Payroll payrollDetails) {
        ensurePayrollInCompany(id);
        Integer employeeId = payrollDetails.getEmployee() == null
                ? null : payrollDetails.getEmployee().getEmployeeId();
        validateEmployeeInCompany(employeeId);

        String sql =
                "SELECT * FROM public.update_payroll(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            statement.setString(
                    2,
                    payrollDetails.getPayMonth()
            );

            statement.setBigDecimal(
                    3,
                    payrollDetails.getBasicSalary()
            );

            statement.setBigDecimal(
                    4,
                    payrollDetails.getAllowances()
            );

            statement.setBigDecimal(
                    5,
                    payrollDetails.getDeductions()
            );

            statement.setBigDecimal(
                    6,
                    payrollDetails.getNetSalary()
            );

            if (payrollDetails.getPaymentDate() != null) {
                statement.setDate(
                        7,
                        Date.valueOf(
                                payrollDetails
                                        .getPaymentDate()
                        )
                );
            } else {
                statement.setNull(
                        7,
                        Types.DATE
                );
            }

            if (payrollDetails.getEmployee() != null) {
                statement.setInt(
                        8,
                        payrollDetails
                                .getEmployee()
                                .getEmployeeId()
                );
            } else {
                statement.setNull(
                        8,
                        Types.INTEGER
                );
            }

            statement.setLong(9, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPayroll(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating payroll with id: "
                            + id,
                    e
            );
        }

        throw new RuntimeException(
                "Payroll not found with id: " + id
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    public boolean deletePayroll(Integer id) {
        ensurePayrollInCompany(id);

        String sql =
                "SELECT public.delete_payroll(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
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
                    "Error deleting payroll with id: "
                            + id,
                    e
            );
        }

        return false;
    }


    // =========================================================
    // BY EMPLOYEE
    // =========================================================

    public List<Payroll> findByEmployeeEmployeeId(
            Integer employeeId) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_payrolls_by_employee(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            statement.setLong(2, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    payrolls.add(
                            mapPayroll(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching payrolls by employee",
                    e
            );
        }

        return filterPayrollsByCompany(payrolls);
    }


    // =========================================================
    // BY MONTH
    // =========================================================

    public List<Payroll> findByPayMonthIgnoreCase(
            String payMonth) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_payrolls_by_month(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, payMonth);

            statement.setLong(2, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    payrolls.add(
                            mapPayroll(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching payrolls by month",
                    e
            );
        }

        return filterPayrollsByCompany(payrolls);
    }


    // =========================================================
    // EMPLOYEE + MONTH
    // =========================================================

    public List<Payroll>
    findByEmployeeEmployeeIdAndPayMonthIgnoreCase(
            Integer employeeId,
            String payMonth) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_employee_payroll_by_month(?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setString(2, payMonth);

            statement.setLong(3, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    payrolls.add(
                            mapPayroll(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee payroll by month",
                    e
            );
        }

        return filterPayrollsByCompany(payrolls);
    }


    // =========================================================
    // BY PAYMENT DATE
    // =========================================================

    public List<Payroll> findByPaymentDate(
            java.time.LocalDate paymentDate) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_payrolls_by_payment_date(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(paymentDate)
            );

            statement.setLong(2, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    payrolls.add(
                            mapPayroll(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching payrolls by payment date",
                    e
            );
        }

        return filterPayrollsByCompany(payrolls);
    }


    // =========================================================
    // EMPLOYEE + PAYMENT DATE
    // =========================================================

    public List<Payroll>
    findByEmployeeEmployeeIdAndPaymentDate(
            Integer employeeId,
            java.time.LocalDate paymentDate) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM public.get_employee_payroll_by_payment_date(?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            statement.setDate(
                    2,
                    Date.valueOf(paymentDate)
            );

            statement.setLong(3, companyContext.getCompanyId());
            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    payrolls.add(
                            mapPayroll(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee payroll by payment date",
                    e
            );
        }

        return filterPayrollsByCompany(payrolls);
    }


    // =========================================================
    // MAP RESULT SET
    // =========================================================

    private Payroll mapPayroll(
            ResultSet rs) throws SQLException {

        Payroll payroll =
                new Payroll();

        payroll.setPayrollId(
                rs.getInt("payroll_id")
        );

        payroll.setPayMonth(
                rs.getString("pay_month")
        );

        BigDecimal basicSalary =
                rs.getBigDecimal("basic_salary");

        payroll.setBasicSalary(basicSalary);

        BigDecimal allowances =
                rs.getBigDecimal("allowances");

        payroll.setAllowances(allowances);

        BigDecimal deductions =
                rs.getBigDecimal("deductions");

        payroll.setDeductions(deductions);

        BigDecimal netSalary =
                rs.getBigDecimal("net_salary");

        payroll.setNetSalary(netSalary);

        Date paymentDate =
                rs.getDate("payment_date");

        if (paymentDate != null) {
            payroll.setPaymentDate(
                    paymentDate.toLocalDate()
            );
        }

        int employeeId =
                rs.getInt("employee_id");

        if (!rs.wasNull()) {

            Employee employee =
                    new Employee();

            employee.setEmployeeId(
                    employeeId
            );

            payroll.setEmployee(employee);
        }

        return payroll;
    }


    public List<Payroll> getPayrollsByHr(Integer hrId) {

        List<Payroll> payrolls = new ArrayList<>();

        String sql = "SELECT * FROM public.get_payrolls_by_hr(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, hrId);

            statement.setLong(2, companyContext.getCompanyId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payrolls.add(mapPayroll(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching HR employee payrolls", e);
        }

        return filterPayrollsByCompany(payrolls);
    }


    // Payroll records are tenant-owned through their employee relationship.
    private List<Payroll> filterPayrollsByCompany(List<Payroll> payrolls) {
        Long companyId = companyContext.getCompanyId();
        List<Payroll> scoped = new ArrayList<>();
        for (Payroll payroll : payrolls) {
            Integer employeeId = payroll.getEmployee() == null
                    ? null : payroll.getEmployee().getEmployeeId();
            if (employeeId != null && isEmployeeInCompany(employeeId, companyId)) {
                scoped.add(payroll);
            }
        }
        return scoped;
    }

    private void ensurePayrollInCompany(Integer payrollId) {
        String sql = """
            SELECT 1 FROM public.payroll p
            JOIN public.employees e ON e.employee_id = p.employee_id
            WHERE p.payroll_id = ? AND e.company_id = ?
            """;
        try (Connection con = dataSource.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, payrollId);
            ps.setLong(2, companyContext.getCompanyId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Payroll does not belong to your company");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate payroll company", e);
        }
    }

    private void validateEmployeeInCompany(Integer employeeId) {
        if (employeeId == null || !isEmployeeInCompany(employeeId, companyContext.getCompanyId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Employee does not belong to your company");
        }
    }

    private boolean isEmployeeInCompany(Integer employeeId, Long companyId) {
        if (employeeId == null) return false;
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