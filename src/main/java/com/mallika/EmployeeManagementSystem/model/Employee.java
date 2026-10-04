package com.mallika.EmployeeManagementSystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mallika.EmployeeManagementSystem.model.Leave;
import com.mallika.EmployeeManagementSystem.model.Payroll;
import com.mallika.EmployeeManagementSystem.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeId;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    private String address;

    private String profilePhoto;


    // =========================
    // DEPARTMENT
    // =========================

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;


    // =========================
    // DESIGNATION
    // =========================

    @ManyToOne
    @JoinColumn(name = "designation_id")
    private Designation designation;


    // =========================
    // MANAGER
    // =========================

    @Column(name = "manager_id")
    private Integer managerId;


    // =========================
    // HR
    // =========================

    @Column(name = "hr_id")
    private Integer hrId;


    // =========================
    // PROJECTS
    // =========================

    @ManyToMany
    @JoinTable(
            name = "employee_project",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    @JsonIgnore
    private List<Project> projects;


    // =========================
    // ATTENDANCE
    // =========================

    @OneToMany(mappedBy = "employee")
    @JsonIgnore
    private List<Attendance> attendanceList;


    // =========================
    // PAYROLL
    // =========================

    @OneToMany(mappedBy = "employee")
    @JsonIgnore
    private List<Payroll> payrolls;


    // =========================
    // LEAVES
    // =========================

    @OneToMany(mappedBy = "employee")
    @JsonIgnore
    private List<Leave> leaves;


    // =========================
    // USER / LOGIN
    // =========================

    @OneToOne(mappedBy = "employee")
    @JsonIgnore
    private User user;
}