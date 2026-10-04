package com.mallika.EmployeeManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeProfileDTO {

    // =========================
    // EMPLOYEE
    // =========================

    private Integer employeeId;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    private String address;

    private String profilePhoto;


    // =========================
    // DEPARTMENT
    // =========================

    private Integer departmentId;

    private String departmentName;


    // =========================
    // DESIGNATION
    // =========================

    private Integer designationId;

    private String designationTitle;


    // =========================
    // MANAGER
    // =========================

    private Integer managerId;

    private String managerName;

    private String managerEmail;

    private String managerPhone;


    // =========================
    // HR
    // =========================

    private Integer hrId;

    private String hrName;

    private String hrEmail;

    private String hrPhone;
}