
package com.mallika.EmployeeManagementSystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer departmentId;

    private String departmentName;

    private String location;

    private String description;

    @Column(name = "company_id")
    private Long companyId;

    @OneToMany(mappedBy = "department")
    @JsonIgnore
    private List<Employee> employees;
}
