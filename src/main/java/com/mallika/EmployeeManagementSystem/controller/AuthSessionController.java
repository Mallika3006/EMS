package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.service.CompanyUserDetails;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Returns the current request's non-secret authentication details.
 * This endpoint is authenticated by SecurityConfig and never returns JWTs,
 * passwords, or other credentials. It helps diagnose role/company mismatches.
 */
@RestController
public class AuthSessionController {

    @GetMapping("/auth/session")
    public ResponseEntity<Map<String, Object>> currentSession(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of("authenticated", false));
        }

        Object principal = authentication.getPrincipal();
        Long companyId = principal instanceof CompanyUserDetails companyUser
                ? companyUser.getCompanyId()
                : null;

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "authenticated", true,
                        "username", authentication.getName(),
                        "authorities", authentication.getAuthorities().stream()
                                .map(authority -> authority.getAuthority())
                                .sorted()
                                .toList(),
                        "companyId", companyId == null ? 0L : companyId
                ));
    }
}
