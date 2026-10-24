package com.mallika.EmployeeManagementSystem.dto;

/**
 * Minimal project-member view; deliberately excludes private employee details.
 */
public record ProjectMemberSummary(
        Integer employeeId,
        String firstName,
        String lastName
) {
}
