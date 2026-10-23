package com.mallika.EmployeeManagementSystem.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CookieOriginValidationFilter cookieOriginValidationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CookieOriginValidationFilter cookieOriginValidationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.cookieOriginValidationFilter = cookieOriginValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/", "/login", "/admin-login", "/user-login", "/register-company").permitAll()
                .requestMatchers(HttpMethod.POST, "/login", "/register-company").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/style.css", "/company-registration.css", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                .requestMatchers("/admin-dashboard").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/hr-dashboard").hasRole("HR")
                .requestMatchers("/manager-dashboard").hasRole("MANAGER")
                .requestMatchers("/employee-dashboard").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/hr/**").hasRole("HR")
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/employees/me", "/employees/me/profile-details", "/employees/me/photo").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers("/employees/manager/me", "/employees/manager/my-team").hasRole("MANAGER")
                .requestMatchers("/employees/hr/me").hasRole("HR")
                .requestMatchers("/attendance/me").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers("/attendance/manager/team").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/attendance/manager").hasRole("MANAGER")
                .requestMatchers("/payrolls/me").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers("/projects/me").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers("/tasks/me").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers("/departments/me").hasAnyRole("EMPLOYEE", "HR", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/leaves/me").hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/leaves").hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/leaves/*/withdraw").hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")
                .requestMatchers("/leaves/manager/**").hasRole("MANAGER")
                .requestMatchers("/leaves/hr/**").hasRole("HR")
                .requestMatchers(HttpMethod.PUT, "/leaves/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/leaves/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/leaves", "/leaves/*").hasRole("ADMIN")
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/departments/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/designations/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/attendance/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/payrolls/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.POST, "/projects/*/members/*").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/projects/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/projects/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/projects/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/projects/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/tasks/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/leaves/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"message\":\"Authentication required. Please sign in again.\"}");
                })
                .accessDeniedHandler((request, response, exception) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    String username = authentication == null ? "anonymous" : authentication.getName();
                    String authorities = authentication == null ? "[]" : authentication.getAuthorities().toString();
                    log.warn("Forbidden request: method={} uri={} user={} authorities={}",
                            request.getMethod(), request.getRequestURI(), username, authorities);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"message\":\"Access denied. Your account does not have permission for this action.\"}");
                }))
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/user-login").deleteCookies("jwt").invalidateHttpSession(true))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(cookieOriginValidationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterServletRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CookieOriginValidationFilter> cookieOriginFilterServletRegistration(CookieOriginValidationFilter filter) {
        FilterRegistrationBean<CookieOriginValidationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
