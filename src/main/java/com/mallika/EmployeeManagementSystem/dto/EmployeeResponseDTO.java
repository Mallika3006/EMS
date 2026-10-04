package com.mallika.EmployeeManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeResponseDTO {

    private Long employeeId;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    private String address;

    private Long departmentId;

    private Long designationId;

    private Long managerId;

    private Long hrId;

    private String profilePhoto;
}