package com.mallika.EmployeeManagementSystem.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Boolean isActive;

    @Column(name = "company_id")
    private Long companyId;

    @OneToOne
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
