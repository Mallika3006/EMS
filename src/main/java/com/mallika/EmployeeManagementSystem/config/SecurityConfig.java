package com.mallika.EmployeeManagementSystem.config;

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

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // ==========================================
                // CSRF
                // ==========================================

                .csrf(csrf -> csrf.disable())


                // ==========================================
                // AUTHORIZATION
                // ==========================================

                .authorizeHttpRequests(auth -> auth


                        // ==========================================
                        // LOGIN
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin-login",
                                "/user-login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/login"
                        ).permitAll()


                        // ==========================================
                        // STATIC FILES
                        // ==========================================

                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/style.css",
                                "/favicon.ico"
                        )
                        .permitAll()


                        // ==========================================
                        // FIRST USER CREATION
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/users"
                        ).permitAll()


                        // ==========================================
                        // DASHBOARDS
                        // ==========================================

                        .requestMatchers("/admin-dashboard")
                        .hasRole("ADMIN")

                        .requestMatchers("/hr-dashboard")
                        .hasRole("HR")

                        .requestMatchers("/manager-dashboard")
                        .hasRole("MANAGER")

                        .requestMatchers("/employee-dashboard")
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )

                                // ==========================================
                                // MANAGER PAGES
                                // ==========================================

                                .requestMatchers("/manager/**")
                                .hasRole("MANAGER")

                        // ==========================================
                        // HR PAGES
                        // ==========================================

                        .requestMatchers("/hr/**")
                        .hasRole("HR")


                        // ==========================================
                        // USERS
                        // ==========================================

                        .requestMatchers("/users/**")
                        .hasRole("ADMIN")


                        // ==========================================
                        // OWN EMPLOYEE DATA
                        // ==========================================

                        .requestMatchers(
                                "/employees/me",
                                "/employees/me/profile-details",
                                "/employees/me/photo"
                        )
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // OWN ATTENDANCE
                        // ==========================================

                        .requestMatchers("/attendance/me")
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // OWN PAYROLL
                        // ==========================================

                        .requestMatchers("/payrolls/me")
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // OWN PROJECTS
                        // ==========================================

                        .requestMatchers("/projects/me")
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // OWN TASKS
                        // ==========================================

                        .requestMatchers("/tasks/me")
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // OWN DEPARTMENT
                        // ==========================================

                        .requestMatchers("/departments/me")
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // MANAGER - OWN EMPLOYEES
                        // ==========================================

                        .requestMatchers("/employees/manager/me")
                        .hasRole("MANAGER")


                        // ==========================================
                        // HR - OWN EMPLOYEES
                        // ==========================================

                        .requestMatchers("/employees/hr/me")
                        .hasRole("HR")


                        // ==========================================
                        // LEAVES - OWN
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/leaves/me"
                        )
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/leaves"
                        )
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/leaves/*/withdraw"
                        )
                        .hasAnyRole(
                                "EMPLOYEE",
                                "HR",
                                "MANAGER",
                                "ADMIN"
                        )


                        // ==========================================
                        // LEAVES - MANAGEMENT
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/leaves/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/leaves/*"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // ADMIN + HR
                        // ==========================================

                        .requestMatchers("/employees/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )

                        .requestMatchers("/departments/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )

                        .requestMatchers("/designations/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )

                        .requestMatchers("/attendance/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )

                        .requestMatchers("/payrolls/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )


                        // ==========================================
                        // ADMIN + HR + MANAGER
                        // ==========================================

                        .requestMatchers("/projects/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )

                        .requestMatchers("/tasks/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )

                        .requestMatchers("/leaves/**")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )


                        // ==========================================
                        // EVERYTHING ELSE
                        // ==========================================

                        .anyRequest().authenticated()
                )


                // ==========================================
                // LOGOUT
                // ==========================================

                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl("/user-login")

                        .deleteCookies("jwt")

                        .invalidateHttpSession(true)
                )


                // ==========================================
                // JWT FILTER
                // ==========================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // ==========================================
    // AUTHENTICATION MANAGER
    // ==========================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}