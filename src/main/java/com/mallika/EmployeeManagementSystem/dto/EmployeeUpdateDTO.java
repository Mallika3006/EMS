package com.mallika.EmployeeManagementSystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeUpdateDTO {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    private String address;

    private String profilePhoto;

    private Integer departmentId;

    private Integer managerId;

    private Integer hrId;
}