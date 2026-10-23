package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.dto.LoginRequest;
import com.mallika.EmployeeManagementSystem.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.security.cookie-secure:false}")
    private boolean secureCookie;

    public LoginController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Public entry points for the shared sign-in page.
    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login/login";
    }

    // ==========================================
    // ADMIN LOGIN PAGE
    // ==========================================

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "login/admin-login";
    }

    // ==========================================
    // USER LOGIN PAGE
    // ==========================================

    @GetMapping("/user-login")
    public String userLoginPage() {
        return "login/user-login";
    }

    // ==========================================
    // AUTHENTICATE USER
    // ==========================================

    @PostMapping("/login")
    @ResponseBody
    public Map<String, String> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        // Authenticate username and password
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // Generate JWT
        String token =
                jwtService.generateToken(
                        userDetails.getUsername()
                );

        // Get the authenticated user's role
        String role =
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority();

        // Create JWT cookie
        ResponseCookie jwtCookie = ResponseCookie
                .from("jwt", token)
                .httpOnly(true)
                .secure(secureCookie) // Enable in HTTPS deployments
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .build();

        // Send cookie to browser
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                jwtCookie.toString()
        );

        // The JWT is sent only as an HttpOnly cookie; JavaScript receives the role only.
        return Map.of("role", role);
    }
}