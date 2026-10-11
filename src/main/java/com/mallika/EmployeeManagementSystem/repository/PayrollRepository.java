package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Payroll;
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

    public PayrollRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    // =========================================================
    // CREATE
    // =========================================================

    public Payroll createPayroll(Payroll payroll) {

        String sql =
                "SELECT * FROM create_payroll(?, ?, ?, ?, ?, ?, ?)";

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
                "SELECT * FROM get_all_payrolls()";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                payrolls.add(
                        mapPayroll(resultSet)
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching payrolls",
                    e
            );
        }

        return payrolls;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<Payroll> getPayrollById(
            Integer id) {

        String sql =
                "SELECT * FROM get_payroll_by_id(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapPayroll(resultSet)
                    );
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

        String sql =
                "SELECT * FROM update_payroll(?, ?, ?, ?, ?, ?, ?, ?)";

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

        String sql =
                "SELECT delete_payroll(?)";

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
                "SELECT * FROM get_payrolls_by_employee(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

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

        return payrolls;
    }


    // =========================================================
    // BY MONTH
    // =========================================================

    public List<Payroll> findByPayMonthIgnoreCase(
            String payMonth) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_payrolls_by_month(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, payMonth);

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

        return payrolls;
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
                "SELECT * FROM get_employee_payroll_by_month(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setString(2, payMonth);

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

        return payrolls;
    }


    // =========================================================
    // BY PAYMENT DATE
    // =========================================================

    public List<Payroll> findByPaymentDate(
            java.time.LocalDate paymentDate) {

        List<Payroll> payrolls =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_payrolls_by_payment_date(?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(paymentDate)
            );

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

        return payrolls;
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
                "SELECT * FROM get_employee_payroll_by_payment_date(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            statement.setDate(
                    2,
                    Date.valueOf(paymentDate)
            );

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

        return payrolls;
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

        String sql = "SELECT * FROM get_payrolls_by_hr(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, hrId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payrolls.add(mapPayroll(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching HR employee payrolls", e);
        }

        return payrolls;
    }

}