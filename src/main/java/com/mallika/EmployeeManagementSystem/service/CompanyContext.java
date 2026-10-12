package com.mallika.EmployeeManagementSystem.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CompanyContext {

    public Long getCompanyId() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof CompanyUserDetails userDetails)) {

            throw new AccessDeniedException(
                    "Authenticated company user is required"
            );
        }

        Long companyId = userDetails.getCompanyId();

        if (companyId == null || companyId <= 0) {
            throw new AccessDeniedException(
                    "Valid company context not found"
            );
        }

        return companyId;
    }

    public String getUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof CompanyUserDetails userDetails)) {

            throw new AccessDeniedException(
                    "Authenticated user is required"
            );
        }

        return userDetails.getUsername();
    }
}

