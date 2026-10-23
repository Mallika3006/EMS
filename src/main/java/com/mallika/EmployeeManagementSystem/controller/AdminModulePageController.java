package com.mallika.EmployeeManagementSystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@Controller
public class AdminModulePageController {

    private static final Set<String> MODULES = Set.of(
            "departments", "designations", "attendance",
            "leaves", "payroll", "projects", "tasks", "users"
    );

    /**
     * Serves the frontend shell for Admin modules whose data APIs already exist.
     * Business operations remain in the existing REST controllers and services.
     */
    @GetMapping("/admin/{module}")
    public String modulePage(@PathVariable String module, Model model) {
        if (!MODULES.contains(module)) {
            return "redirect:/admin-dashboard";
        }

        model.addAttribute("module", module);
        return "admin/module";
    }
}