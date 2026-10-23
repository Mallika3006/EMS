package com.mallika.EmployeeManagementSystem.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CookieOriginValidationFilter cookieOriginValidationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CookieOriginValidationFilter cookieOriginValidationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.cookieOriginValidationFilter = cookieOriginValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Login pages
                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin-login",
                                "/user-login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/login"
                        ).permitAll()

                        // Static resources
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/style.css",
                                "/favicon.ico"
                        ).permitAll()

                        // User creation is company-scoped and requires an Admin.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/users"
                        ).hasRole("ADMIN")

                        // Admin dashboard and pages
                        .requestMatchers("/admin-dashboard")
                        .hasRole("ADMIN")

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // HR and Manager dashboards
                        .requestMatchers("/hr-dashboard")
                        .hasRole("HR")

                        .requestMatchers("/manager-dashboard")
                        .hasRole("MANAGER")

                        .requestMatchers("/employee-dashboard")
                        .hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Manager pages
                        .requestMatchers("/manager/**")
                        .hasRole("MANAGER")

                        // HR pages
                        .requestMatchers("/hr/**")
                        .hasRole("HR")

                        // User management
                        .requestMatchers("/users/**")
                        .hasRole("ADMIN")

                        // Own employee data
                        .requestMatchers(
                                "/employees/me",
                                "/employees/me/profile-details",
                                "/employees/me/photo"
                        ).hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Manager employee endpoints
                        .requestMatchers(
                                "/employees/manager/me",
                                "/employees/manager/my-team"
                        ).hasRole("MANAGER")

                        // HR employee endpoints
                        .requestMatchers("/employees/hr/me")
                        .hasRole("HR")

                        // Own attendance
                        .requestMatchers("/attendance/me")
                        .hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Manager team attendance
                        .requestMatchers("/attendance/manager/team")
                        .hasRole("MANAGER")

                        // Manager adds attendance for team members
                        .requestMatchers(
                                        HttpMethod.POST,
                                        "/attendance/manager"
                                ).hasRole("MANAGER")

                        // Own payroll
                        .requestMatchers("/payrolls/me")
                        .hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Own projects
                        .requestMatchers("/projects/me")
                        .hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Own tasks
                        .requestMatchers("/tasks/me")
                        .hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Own department
                        .requestMatchers("/departments/me")
                        .hasAnyRole("EMPLOYEE", "HR", "MANAGER")

                        // Own leave requests
                        .requestMatchers(
                                HttpMethod.GET,
                                "/leaves/me"
                        ).hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/leaves"
                        ).hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/leaves/*/withdraw"
                        ).hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")

                        // Leave management
                        .requestMatchers("/leaves/manager/**")
                        .hasRole("MANAGER")

                        .requestMatchers("/leaves/hr/**")
                        .hasRole("HR")

                        // Cross-employee leave administration is Admin-only.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/leaves/*"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/leaves/*"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/leaves",
                                "/leaves/*"
                        ).hasRole("ADMIN")

                        // Employee APIs
                        .requestMatchers("/employees/**")
                        .hasAnyRole("ADMIN", "HR")

                        // Department APIs
                        .requestMatchers("/departments/**")
                        .hasAnyRole("ADMIN", "HR")

                        // Designation APIs
                        .requestMatchers("/designations/**")
                        .hasAnyRole("ADMIN", "HR")

                        // Attendance APIs
                        .requestMatchers("/attendance/**")
                        .hasAnyRole("ADMIN", "HR")

                        // Payroll APIs
                        .requestMatchers("/payrolls/**")
                        .hasAnyRole("ADMIN", "HR")

                        // Admin creates projects. Manager writes are restricted by ProjectService to owned projects.
                        // Employees use /projects/me.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/projects/*/members/*"
                        ).hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/projects/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/projects/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/projects/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/projects/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/projects/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // HR does not manage tasks; Admin and Manager do.
                        .requestMatchers("/tasks/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // Remaining leave administration endpoints are Admin-only.
                        .requestMatchers("/leaves/**")
                        .hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/user-login")
                        .deleteCookies("jwt")
                        .invalidateHttpSession(true)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        cookieOriginValidationFilter,
                        JwtAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterServletRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CookieOriginValidationFilter> cookieOriginFilterServletRegistration(
            CookieOriginValidationFilter filter) {
        FilterRegistrationBean<CookieOriginValidationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}