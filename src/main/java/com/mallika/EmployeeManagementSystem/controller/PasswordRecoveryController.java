package com.mallika.EmployeeManagementSystem.controller;
import com.mallika.EmployeeManagementSystem.service.PasswordRecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/password-recovery")
public class PasswordRecoveryController {
    private final PasswordRecoveryService recoveryService;
    public PasswordRecoveryController(PasswordRecoveryService recoveryService) { this.recoveryService = recoveryService; }
    @PostMapping("/request")
    public ResponseEntity<Map<String,String>> requestOtp(@RequestBody RequestOtp request) {
        if (request == null || request.username() == null || request.channel() == null)
            return ResponseEntity.badRequest().body(Map.of("message","Enter your username and choose email or SMS."));
        String channel=request.channel().trim().toUpperCase();
        if (!channel.equals("EMAIL") && !channel.equals("SMS"))
            return ResponseEntity.badRequest().body(Map.of("message","Choose email or SMS."));
        recoveryService.requestOtp(request.username().trim(),channel);
        return ResponseEntity.ok(Map.of("message","If the account is active, has a registered recovery contact, and delivery is configured, an OTP will be sent."));
    }
    @PostMapping("/verify")
    public ResponseEntity<Map<String,String>> verifyOtp(@RequestBody VerifyOtp request) {
        if (request == null || request.username()==null || request.channel()==null || request.otp()==null || request.otp().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message","Enter your username and the OTP."));
        try {
            String token=recoveryService.verifyOtp(request.username().trim(),request.channel().trim().toUpperCase(),request.otp().trim());
            return ResponseEntity.ok(Map.of("resetToken",token,"message","OTP verified. Set your new password."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",ex.getMessage()));
        }
    }
    @PostMapping("/reset")
    public ResponseEntity<Map<String,String>> resetPassword(@RequestBody ResetPassword request) {
        if (request==null || request.resetToken()==null || request.newPassword()==null || request.confirmPassword()==null)
            return ResponseEntity.badRequest().body(Map.of("message","Complete all password fields."));
        try {
            recoveryService.resetPassword(request.resetToken(),request.newPassword(),request.confirmPassword());
            return ResponseEntity.ok(Map.of("message","Password reset successfully. You can now sign in."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message",ex.getMessage()));
        }
    }
    public record RequestOtp(String username,String channel) {}
    public record VerifyOtp(String username,String channel,String otp) {}
    public record ResetPassword(String resetToken,String newPassword,String confirmPassword) {}
}
