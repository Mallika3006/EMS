package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Payroll;
import com.mallika.EmployeeManagementSystem.service.PayrollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Payroll> createPayroll(
            @RequestBody Payroll payroll) {

        Payroll savedPayroll =
                payrollService.createPayroll(payroll);

        return new ResponseEntity<>(
                savedPayroll,
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Payroll>> getAllPayrolls() {

        return ResponseEntity.ok(
                payrollService.getAllPayrolls()
        );
    }

    // GET LOGGED-IN EMPLOYEE'S PAYROLL
    @GetMapping("/me")
    public ResponseEntity<List<Payroll>> getMyPayrolls() {

        return ResponseEntity.ok(
                payrollService.getMyPayrolls()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Payroll> getPayrollById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                payrollService.getPayrollById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Payroll> updatePayroll(
            @PathVariable Integer id,
            @RequestBody Payroll payroll) {

        return ResponseEntity.ok(
                payrollService.updatePayroll(id, payroll)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayroll(
            @PathVariable Integer id) {

        payrollService.deletePayroll(id);

        return ResponseEntity.ok(
                "Payroll deleted successfully"
        );
    }

    // GET BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Payroll>> getPayrollsByEmployee(
            @PathVariable Integer employeeId) {

        return ResponseEntity.ok(
                payrollService.getPayrollsByEmployee(employeeId)
        );
    }

    // GET BY MONTH
    @GetMapping("/month")
    public ResponseEntity<List<Payroll>> getPayrollsByMonth(
            @RequestParam String payMonth) {

        return ResponseEntity.ok(
                payrollService.getPayrollsByMonth(payMonth)
        );
    }

    // GET BY EMPLOYEE AND MONTH
    @GetMapping("/employee/{employeeId}/month")
    public ResponseEntity<List<Payroll>> getEmployeePayrollByMonth(
            @PathVariable Integer employeeId,
            @RequestParam String payMonth) {

        return ResponseEntity.ok(
                payrollService.getEmployeePayrollByMonth(
                        employeeId,
                        payMonth
                )
        );
    }

    // GET BY PAYMENT DATE
    @GetMapping("/payment-date")
    public ResponseEntity<List<Payroll>> getPayrollsByPaymentDate(
            @RequestParam LocalDate paymentDate) {

        return ResponseEntity.ok(
                payrollService.getPayrollsByPaymentDate(paymentDate)
        );
    }

    // GET BY EMPLOYEE AND PAYMENT DATE
    @GetMapping("/employee/{employeeId}/payment-date")
    public ResponseEntity<List<Payroll>> getEmployeePayrollByPaymentDate(
            @PathVariable Integer employeeId,
            @RequestParam LocalDate paymentDate) {

        return ResponseEntity.ok(
                payrollService.getEmployeePayrollByPaymentDate(
                        employeeId,
                        paymentDate
                )
        );
    }

    @GetMapping("/hr/my-employees")
    public List<Payroll> getPayrollsForCurrentHr() {
        return payrollService.getPayrollsForCurrentHr();
    }
}
