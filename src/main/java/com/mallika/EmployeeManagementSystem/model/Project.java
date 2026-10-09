package com.mallika.EmployeeManagementSystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Task;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    private String projectName;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(name = "manager_id")
    private Integer managerId;


    // =========================
    // TASKS
    // =========================

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<Task> tasks;


    // =========================
    // EMPLOYEES
    // =========================

    @ManyToMany(mappedBy = "projects")
    @JsonIgnore
    private List<Employee> employees;
}