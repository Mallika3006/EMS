package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Payroll;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.PayrollRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;

    public PayrollService(
            PayrollRepository payrollRepository,
            UserRepository userRepository) {

        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // CREATE
    // =========================================================

    public Payroll createPayroll(
            Payroll payroll) {

        return payrollRepository
                .createPayroll(payroll);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Payroll> getAllPayrolls() {

        return payrollRepository
                .getAllPayrolls();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Payroll getPayrollById(
            Integer id) {

        return payrollRepository
                .getPayrollById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payroll not found with id: "
                                        + id
                        ));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Payroll updatePayroll(
            Integer id,
            Payroll payrollDetails) {

        return payrollRepository
                .updatePayroll(
                        id,
                        payrollDetails
                );
    }


    // =========================================================
    // DELETE
    // =========================================================

    public void deletePayroll(Integer id) {

        boolean deleted =
                payrollRepository
                        .deletePayroll(id);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Payroll not found with id: "
                            + id
            );
        }
    }


    // =========================================================
    // GET BY EMPLOYEE
    // =========================================================

    public List<Payroll> getPayrollsByEmployee(
            Integer employeeId) {

        return payrollRepository
                .findByEmployeeEmployeeId(
                        employeeId
                );
    }


    // =========================================================
    // GET BY MONTH
    // =========================================================

    public List<Payroll> getPayrollsByMonth(
            String payMonth) {

        return payrollRepository
                .findByPayMonthIgnoreCase(
                        payMonth
                );
    }


    // =========================================================
    // EMPLOYEE + MONTH
    // =========================================================

    public List<Payroll>
    getEmployeePayrollByMonth(
            Integer employeeId,
            String payMonth) {

        return payrollRepository
                .findByEmployeeEmployeeIdAndPayMonthIgnoreCase(
                        employeeId,
                        payMonth
                );
    }


    // =========================================================
    // BY PAYMENT DATE
    // =========================================================

    public List<Payroll>
    getPayrollsByPaymentDate(
            LocalDate paymentDate) {

        return payrollRepository
                .findByPaymentDate(
                        paymentDate
                );
    }


    // =========================================================
    // EMPLOYEE + PAYMENT DATE
    // =========================================================

    public List<Payroll>
    getEmployeePayrollByPaymentDate(
            Integer employeeId,
            LocalDate paymentDate) {

        return payrollRepository
                .findByEmployeeEmployeeIdAndPaymentDate(
                        employeeId,
                        paymentDate
                );
    }


    // =========================================================
    // LOGGED-IN EMPLOYEE'S PAYROLL
    // =========================================================

    public List<Payroll> getMyPayrolls() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException(
                    "Employee not found"
            );
        }

        Integer employeeId =
                user.getEmployee()
                        .getEmployeeId();

        return payrollRepository
                .findByEmployeeEmployeeId(
                        employeeId
                );
    }

    public List<Payroll> getPayrollsForCurrentHr() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("HR user not found"));

        if (user.getEmployee() == null) {
            throw new RuntimeException("HR employee profile not found");
        }

        Integer hrId = user.getEmployee().getEmployeeId();

        return payrollRepository.getPayrollsByHr(hrId);
    }
}