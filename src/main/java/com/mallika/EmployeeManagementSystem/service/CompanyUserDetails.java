package com.mallika.EmployeeManagementSystem.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CompanyUserDetails implements UserDetails {

    private final UserDetails userDetails;
    private final Long companyId;

    public CompanyUserDetails(
            UserDetails userDetails,
            Long companyId) {

        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException(
                    "A valid company ID is required for login"
            );
        }

        this.userDetails = userDetails;
        this.companyId = companyId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    @Override
    public String getPassword() {
        return userDetails.getPassword();
    }

    @Override
    public String getUsername() {
        return userDetails.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userDetails.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userDetails.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userDetails.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return userDetails.isEnabled();
    }
}

