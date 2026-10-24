package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.service.CompanyRegistrationService;
import com.mallika.EmployeeManagementSystem.service.CompanyRegistrationService.RegistrationConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class CompanyRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(CompanyRegistrationController.class);

    private final CompanyRegistrationService registrationService;

    public CompanyRegistrationController(CompanyRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register-company")
    public String registrationPage() {
        return "login/register-company";
    }

    @PostMapping(value = "/register-company", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> registerCompany(
            @RequestParam String companyName,
            @RequestParam String recoveryEmail,
            @RequestParam(required = false) String recoveryPhone,
            @RequestParam String adminUsername,
            @RequestParam String password,
            @RequestParam String confirmPassword) {
        try {
            registrationService.registerCompany(
                    companyName, recoveryEmail, recoveryPhone, adminUsername, password, confirmPassword);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Company registered successfully. You can now sign in with your Admin account.",
                    "redirect", "/login?registered"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (RegistrationConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            log.warn("Company registration failed because of a database integrity constraint.", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "The company name or username is already registered. Please choose different details."
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
