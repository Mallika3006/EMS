package com.mallika.EmployeeManagementSystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dedicated MVC route for the Admin User Accounts page.
 * Account data and mutations are handled by AdminUserAccountsController.
 */
@Controller
public class AdminUserAccountsPageController {

    @GetMapping("/admin/users")
    public String userAccountsPage(Model model) {
        model.addAttribute("module", "users");
        return "admin/module";
    }
}
