
package com.mallika.EmployeeManagementSystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer designationId;

    private String designationTitle;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    @Column(name = "company_id")
    private Long companyId;

    @OneToMany(mappedBy = "designation")
    @JsonIgnore
    private List<Employee> employees;
}
