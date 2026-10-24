package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Payroll;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.PayrollRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PayrollService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;

    public PayrollService(PayrollRepository payrollRepository, UserRepository userRepository) {
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
    }

    // Salary arithmetic is enforced on the server; never trust a client-supplied net salary.
    public Payroll createPayroll(Payroll payroll) {
        validateAndCalculate(payroll);
        Integer hrId = currentHrIdOrNull();
        if (hrId != null && !payrollRepository.isEmployeeAssignedToHr(
                payroll.getEmployee().getEmployeeId(), hrId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only manage payroll for active employees assigned to you");
        }
        return payrollRepository.createPayroll(payroll);
    }

    public List<Payroll> getAllPayrolls() {
        Integer hrId = currentHrIdOrNull();
        return hrId == null ? payrollRepository.getAllPayrolls()
                : payrollRepository.getPayrollsByHr(hrId);
    }

    public Payroll getPayrollById(Integer id) {
        Integer hrId = currentHrIdOrNull();
        if (hrId != null && !payrollRepository.isPayrollAssignedToHr(id, hrId)) {
            throw new ResourceNotFoundException("Payroll not found with id: " + id);
        }
        return payrollRepository.getPayrollById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with id: " + id));
    }

    public Payroll updatePayroll(Integer id, Payroll payrollDetails) {
        validateAndCalculate(payrollDetails);
        Integer hrId = currentHrIdOrNull();
        if (hrId != null && (!payrollRepository.isPayrollAssignedToHr(id, hrId)
                || !payrollRepository.isEmployeeAssignedToHr(
                        payrollDetails.getEmployee().getEmployeeId(), hrId))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only update payroll for active employees assigned to you");
        }
        return payrollRepository.updatePayroll(id, payrollDetails);
    }

    public void deletePayroll(Integer id) {
        Integer hrId = currentHrIdOrNull();
        if (hrId != null && !payrollRepository.isPayrollAssignedToHr(id, hrId)) {
            throw new ResourceNotFoundException("Payroll not found with id: " + id);
        }
        boolean deleted = payrollRepository.deletePayroll(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Payroll not found with id: " + id);
        }
    }

    public List<Payroll> getPayrollsByEmployee(Integer employeeId) {
        Integer hrId = currentHrIdOrNull();
        if (hrId != null) {
            if (!payrollRepository.isEmployeeAssignedToHr(employeeId, hrId)) {
                throw new ResourceNotFoundException("Employee payroll not found");
            }
            return payrollRepository.findByEmployeeEmployeeId(employeeId);
        }
        return payrollRepository.findByEmployeeEmployeeId(employeeId);
    }

    public List<Payroll> getPayrollsByMonth(String payMonth) {
        if (payMonth == null || payMonth.isBlank()) {
            throw new IllegalArgumentException("Pay month is required");
        }
        Integer hrId = currentHrIdOrNull();
        if (hrId != null) {
            return payrollRepository.getPayrollsByHr(hrId).stream()
                    .filter(p -> p.getPayMonth() != null
                            && p.getPayMonth().equalsIgnoreCase(payMonth.trim())).toList();
        }
        return payrollRepository.findByPayMonthIgnoreCase(payMonth.trim());
    }

    public List<Payroll> getEmployeePayrollByMonth(Integer employeeId, String payMonth) {
        if (payMonth == null || payMonth.isBlank()) {
            throw new IllegalArgumentException("Pay month is required");
        }
        Integer hrId = currentHrIdOrNull();
        if (hrId != null && !payrollRepository.isEmployeeAssignedToHr(employeeId, hrId)) {
            throw new ResourceNotFoundException("Employee payroll not found");
        }
        return payrollRepository.findByEmployeeEmployeeIdAndPayMonthIgnoreCase(employeeId, payMonth.trim());
    }

    public List<Payroll> getPayrollsByPaymentDate(LocalDate paymentDate) {
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date is required");
        }
        Integer hrId = currentHrIdOrNull();
        if (hrId != null) {
            return payrollRepository.getPayrollsByHr(hrId).stream()
                    .filter(p -> paymentDate.equals(p.getPaymentDate())).toList();
        }
        return payrollRepository.findByPaymentDate(paymentDate);
    }

    public List<Payroll> getEmployeePayrollByPaymentDate(Integer employeeId, LocalDate paymentDate) {
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date is required");
        }
        Integer hrId = currentHrIdOrNull();
        if (hrId != null && !payrollRepository.isEmployeeAssignedToHr(employeeId, hrId)) {
            throw new ResourceNotFoundException("Employee payroll not found");
        }
        return payrollRepository.findByEmployeeEmployeeIdAndPaymentDate(employeeId, paymentDate);
    }

    public List<Payroll> getMyPayrolls() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException("Employee profile not found for the signed-in user");
        }

        return payrollRepository.findByEmployeeEmployeeId(user.getEmployee().getEmployeeId());
    }

    public List<Payroll> getPayrollsForCurrentHr() {
        Integer hrId = currentHrIdOrNull();
        if (hrId == null) {
            throw new org.springframework.security.access.AccessDeniedException("HR access required");
        }
        return payrollRepository.getPayrollsByHr(hrId);
    }

    private Integer currentHrIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_HR".equals(authority.getAuthority()))) {
            return null;
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("HR user not found"));
        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException("HR employee profile not found");
        }
        return user.getEmployee().getEmployeeId();
    }

    private void validateAndCalculate(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll details are required");
        }
        if (payroll.getEmployee() == null || payroll.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("An employee must be selected");
        }
        if (payroll.getPayMonth() == null || payroll.getPayMonth().isBlank()) {
            throw new IllegalArgumentException("Pay month is required");
        }
        payroll.setPayMonth(payroll.getPayMonth().trim());

        BigDecimal basic = requireNonNegative("Basic salary", payroll.getBasicSalary());
        BigDecimal allowances = requireNonNegative("Allowances", payroll.getAllowances());
        BigDecimal deductions = requireNonNegative("Deductions", payroll.getDeductions());

        BigDecimal gross = basic.add(allowances);
        if (deductions.compareTo(gross) > 0) {
            throw new IllegalArgumentException("Deductions cannot exceed basic salary plus allowances");
        }

        payroll.setBasicSalary(basic);
        payroll.setAllowances(allowances);
        payroll.setDeductions(deductions);
        payroll.setNetSalary(gross.subtract(deductions).setScale(2, java.math.RoundingMode.HALF_UP));

    }

    private BigDecimal requireNonNegative(String field, BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (value.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
