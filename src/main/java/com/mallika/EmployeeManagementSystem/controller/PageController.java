package com.mallika.EmployeeManagementSystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

@Controller
public class PageController {

    // ==========================================
    // DASHBOARDS
    // ==========================================

    @GetMapping("/employee-dashboard")
    public String employeeDashboard() {
        return "dashboard/employee";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "dashboard/admin";
    }

    @GetMapping("/hr-dashboard")
    public String hrDashboard() {
        return "dashboard/hr";
    }

    @GetMapping("/manager-dashboard")
    public String managerDashboard() {
        return "dashboard/manager";
    }


    // ==========================================
// MANAGER FEATURES
// ==========================================

    @GetMapping("/manager/profile")
    public String managerProfile() {
        return "manager/profile";
    }

    @GetMapping("/manager/profile/edit")
    public String editManagerProfile() {
        return "manager/edit-profile";
    }

    @GetMapping("/manager/my-attendance")
    public String managerAttendance() {
        return "manager/my-attendance";
    }

    @GetMapping("/manager/my-leaves")
    public String managerLeaves() {
        return "manager/my-leaves";
    }

    @GetMapping("/manager/my-payroll")
    public String managerPayroll() {
        return "manager/my-payroll";
    }

    // ==========================================
    // EMPLOYEE FEATURES
    // ==========================================

    @GetMapping("/my-profile")
    public String myProfile() {
        return "employee/my-profile";
    }

    @GetMapping("/my-attendance")
    public String myAttendance() {
        return "employee/my-attendance";
    }

    @GetMapping("/my-leaves")
    public String myLeaves() {
        return "employee/my-leaves";
    }

    @GetMapping("/my-payroll")
    public String myPayroll() {
        return "employee/my-payroll";
    }

    @GetMapping("/my-projects")
    public String myProjects() {
        return "employee/my-projects";
    }

    @GetMapping("/my-tasks")
    public String myTasks() {
        return "employee/my-tasks";
    }

    @GetMapping("/my-department")
    public String myDepartment() {
        return "employee/my-department";
    }


    // ==========================================
    // EDIT PROFILE
    // ==========================================

    @GetMapping("/employee-profile/edit")
    public String editEmployeeProfile() {
        return "employee/employee-profile-edit";
    }

    // ==========================================
// HR FEATURES
// ==========================================

    @GetMapping("/hr/profile")
    public String hrProfile() {
        return "hr/profile";
    }

    @GetMapping("/hr/attendance")
    public String hrAttendance() {
        return "hr/attendance";
    }

    @GetMapping("/hr/leaves")
    public String hrLeaves() {
        return "hr/leaves";
    }

    @GetMapping("/hr/payroll")
    public String hrPayroll() {
        return "hr/payroll";
    }

    @GetMapping("/hr/employees")
    public String hrEmployees() {
        return "hr/employees";
    }

    @GetMapping("/hr/employees/{id}")
    public String hrEmployeeDetails() {
        return "hr/employee-details";
    }

    @GetMapping("/hr/employees/{id}/edit")
    public String editHrEmployee() {
        return "hr/employee-edit";
    }

    @GetMapping("/hr/departments")
    public String hrDepartments() {
        return "hr/departments";
    }

    @GetMapping("/hr/departments/{id}")
    public String hrDepartmentDetails() {
        return "hr/department-details";
    }

    @GetMapping("/hr/departments/{id}/edit")
    public String editHrDepartment() {
        return "hr/department-edit";
    }

    @GetMapping("/hr/designations")
    public String hrDesignations() {
        return "hr/designations";
    }

    @GetMapping("/hr/designations/{id}")
    public String hrDesignationDetails() {
        return "hr/designation-details";
    }

    @GetMapping("/hr/designations/{id}/edit")
    public String editHrDesignation() {
        return "hr/designation-edit";
    }

    @GetMapping("/hr/teams")
    public String hrTeams() {
        return "hr/teams";
    }

    @GetMapping("/hr/projects")
    public String hrProjects() {
        return "hr/projects";
    }

    @GetMapping("/hr/projects/{id}")
    public String hrProjectDetails(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute("projectId", id);

        return "hr/project-details";
    }

    @GetMapping("/hr/projects/{id}/edit")
    public String editHrProject(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute("projectId", id);

        return "hr/project-edit";
    }

    @GetMapping("/hr/tasks")
    public String hrTasks() {
        return "hr/tasks";
    }

    @GetMapping("/hr/tasks/{id}")
    public String hrTaskDetails(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute("taskId", id);

        return "hr/task-details";
    }

    @GetMapping("/hr/tasks/{id}/edit")
    public String editHrTask(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute("taskId", id);

        return "hr/task-edit";
    }

// HR - EMPLOYEE ATTENDANCE
    @GetMapping("/hr/employee-attendance")
    public String hrEmployeeAttendance() {
        return "hr/employee-attendance";
    }

// HR - EMPLOYEE LEAVES
    @GetMapping("/hr/employee-leaves")
    public String hrEmployeeLeaves() {
        return "hr/employee-leaves";
    }

// HR - EMPLOYEE PAYROLL
    @GetMapping("/hr/employee-payroll")
    public String hrEmployeePayroll() {
        return "hr/employee-payroll";
    }
}