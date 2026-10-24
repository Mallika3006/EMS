package com.mallika.EmployeeManagementSystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class PasswordRecoveryService {
    private static final Logger log=LoggerFactory.getLogger(PasswordRecoveryService.class);
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final HttpClient HTTP=HttpClient.newHttpClient();
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    @Value("${app.recovery.email-enabled:false}") private boolean emailEnabled;
    @Value("${app.recovery.from:no-reply@ems.local}") private String fromAddress;
    @Value("${app.recovery.sms.enabled:false}") private boolean smsEnabled;
    @Value("${app.recovery.sms.account-sid:}") private String twilioAccountSid;
    @Value("${app.recovery.sms.auth-token:}") private String twilioAuthToken;
    @Value("${app.recovery.sms.from:}") private String twilioFrom;

    public PasswordRecoveryService(JdbcTemplate jdbc,PasswordEncoder encoder,ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.jdbc=jdbc; this.encoder=encoder; this.mailSenderProvider=mailSenderProvider;
    }

    public void requestOtp(String username,String channel) {
        List<RecoveryAccount> accounts=jdbc.query("""
            SELECT u.user_id,u.company_id,u.is_active,r.role_name,
                   e.email AS employee_email,e.phone AS employee_phone,c.recovery_email,c.recovery_phone
            FROM public.users u JOIN public.roles r ON r.role_id=u.role_id
            LEFT JOIN public.employees e ON e.employee_id=u.employee_id
            LEFT JOIN public.companies c ON c.company_id=u.company_id
            WHERE LOWER(u.username)=LOWER(?) ORDER BY u.user_id LIMIT 2
            """,(rs,row)->new RecoveryAccount(rs.getInt("user_id"),rs.getLong("company_id"),
            rs.getBoolean("is_active"),rs.getString("role_name"),rs.getString("employee_email"),
            rs.getString("employee_phone"),rs.getString("recovery_email"),rs.getString("recovery_phone")),username);
        if(accounts.size()!=1 || !accounts.get(0).active()) return;
        RecoveryAccount a=accounts.get(0);
        boolean admin="ADMIN".equalsIgnoreCase(a.roleName());
        String destination="EMAIL".equals(channel)?(admin?a.recoveryEmail():a.employeeEmail()):(admin?a.recoveryPhone():a.employeePhone());
        if(destination==null || destination.isBlank()) return;
        if("EMAIL".equals(channel) && (!emailEnabled || mailSenderProvider.getIfAvailable()==null)) return;
        if("SMS".equals(channel) && (!smsEnabled || blank(twilioAccountSid) || blank(twilioAuthToken) || blank(twilioFrom))) return;
        Integer recent=jdbc.queryForObject("""
            SELECT COUNT(*) FROM password_reset_requests WHERE user_id=? AND channel=?
            AND created_at>CURRENT_TIMESTAMP-INTERVAL '15 minutes'
            """,Integer.class,a.userId(),channel);
        if(recent!=null && recent>=3) return;
        String otp=String.format("%06d",RANDOM.nextInt(1_000_000));
        jdbc.update("UPDATE password_reset_requests SET used_at=CURRENT_TIMESTAMP WHERE user_id=? AND used_at IS NULL",a.userId());
        jdbc.update("""
            INSERT INTO password_reset_requests(user_id,company_id,channel,otp_hash,expires_at,attempts,created_at)
            VALUES(?,?,?,?,CURRENT_TIMESTAMP+INTERVAL '10 minutes',0,CURRENT_TIMESTAMP)
            """,a.userId(),a.companyId(),channel,encoder.encode(otp));
        try { if("EMAIL".equals(channel)) sendEmail(destination,otp); else sendSms(destination,otp); }
        catch(Exception ex) {
            jdbc.update("UPDATE password_reset_requests SET used_at=CURRENT_TIMESTAMP WHERE user_id=? AND used_at IS NULL",a.userId());
            log.error("Password recovery delivery failed for user id {} via {}. Cause: {}",
                a.userId(), channel, ex.getMessage(), ex);
        }
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public String verifyOtp(String username,String channel,String otp) {
        if(!"EMAIL".equals(channel) && !"SMS".equals(channel)) throw new IllegalArgumentException("Choose email or SMS.");
        List<OtpChallenge> rows=jdbc.query("""
            SELECT p.request_id,p.user_id,p.company_id,p.otp_hash,p.attempts,p.expires_at,p.used_at
            FROM password_reset_requests p JOIN public.users u ON u.user_id=p.user_id AND u.company_id=p.company_id
            WHERE LOWER(u.username)=LOWER(?) AND u.is_active=TRUE AND p.channel=? ORDER BY p.created_at DESC LIMIT 1
            """,(rs,row)->new OtpChallenge(rs.getLong("request_id"),rs.getInt("user_id"),rs.getLong("company_id"),
            rs.getString("otp_hash"),rs.getInt("attempts"),rs.getTimestamp("expires_at").toInstant(),
            rs.getTimestamp("used_at")==null?null:rs.getTimestamp("used_at").toInstant()),username,channel);
        if(rows.isEmpty()) throw new IllegalArgumentException("OTP is invalid or expired. Request a new one.");
        OtpChallenge ch=rows.get(0);
        if(ch.usedAt()!=null || ch.expiresAt().isBefore(Instant.now()) || ch.attempts()>=5 || ch.otpHash()==null)
            throw new IllegalArgumentException("OTP is invalid or expired. Request a new one.");
        if(!encoder.matches(otp,ch.otpHash())) {
            jdbc.update("UPDATE password_reset_requests SET attempts=attempts+1 WHERE request_id=? AND attempts<5",ch.requestId());
            throw new IllegalArgumentException("OTP is invalid or expired. Request a new one.");
        }
        byte[] bytes=new byte[32]; RANDOM.nextBytes(bytes);
        String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbc.update("""
            UPDATE password_reset_requests SET otp_hash=NULL,verified=TRUE,reset_token_hash=?,
            reset_token_expires_at=CURRENT_TIMESTAMP+INTERVAL '10 minutes'
            WHERE request_id=? AND used_at IS NULL AND attempts<5
            """,sha256(token),ch.requestId());
        return token;
    }

    @Transactional
    public void resetPassword(String resetToken,String newPassword,String confirmPassword) {
        if(newPassword.length()<10 || newPassword.length()>72)
            throw new IllegalArgumentException("Password must be between 10 and 72 characters.");
        if(!newPassword.equals(confirmPassword)) throw new IllegalArgumentException("Passwords do not match.");
        List<ResetGrant> grants=jdbc.query("""
            SELECT request_id,user_id,company_id FROM password_reset_requests
            WHERE reset_token_hash=? AND verified=TRUE AND used_at IS NULL
            AND reset_token_expires_at>CURRENT_TIMESTAMP ORDER BY created_at DESC LIMIT 1
            """,(rs,row)->new ResetGrant(rs.getLong("request_id"),rs.getInt("user_id"),rs.getLong("company_id")),sha256(resetToken));
        if(grants.isEmpty()) throw new IllegalArgumentException("Password reset session is invalid or expired. Verify a new OTP.");
        ResetGrant g=grants.get(0);
        int changed=jdbc.update("UPDATE public.users SET password=? WHERE user_id=? AND company_id=? AND is_active=TRUE",
            encoder.encode(newPassword),g.userId(),g.companyId());
        if(changed!=1) throw new IllegalArgumentException("This account cannot reset its password. Contact your organization administrator.");
        jdbc.update("UPDATE password_reset_requests SET used_at=CURRENT_TIMESTAMP,reset_token_hash=NULL WHERE user_id=? AND used_at IS NULL",g.userId());
    }

    private void sendEmail(String to,String otp) {
        JavaMailSender sender=mailSenderProvider.getIfAvailable();
        if(sender==null) throw new IllegalStateException("Email sender is not configured");
        SimpleMailMessage m=new SimpleMailMessage(); m.setFrom(fromAddress); m.setTo(to);
        m.setSubject("Your EMS password reset code");
        m.setText("Your EMS password reset OTP is: "+otp+"\n\nIt expires in 10 minutes. If you did not request this, ignore this email.");
        sender.send(m);
    }
    private void sendSms(String destination,String otp) throws Exception {
        String to=normalizePhone(destination);
        if(to==null) throw new IllegalArgumentException("Recovery phone must use E.164 format or a 10-digit Indian mobile number.");
        String form="To="+enc(to)+"&From="+enc(twilioFrom)+"&Body="+enc("Your EMS password reset OTP is "+otp+". It expires in 10 minutes.");
        String auth=Base64.getEncoder().encodeToString((twilioAccountSid+":"+twilioAuthToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest req=HttpRequest.newBuilder().uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/"+twilioAccountSid+"/Messages.json"))
            .header("Authorization","Basic "+auth).header("Content-Type","application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> response=HTTP.send(req,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()<200 || response.statusCode()>=300) throw new IllegalStateException("SMS provider rejected the request.");
    }
    private static String normalizePhone(String value) {
        String v=value.replaceAll("[\\s().-]","");
        if(v.matches("\\d{10}")) return "+91"+v;
        return v.matches("\\+[1-9]\\d{7,14}")?v:null;
    }
    private static String enc(String v){return URLEncoder.encode(v,StandardCharsets.UTF_8);}
    private static String sha256(String v) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8))); }
        catch(Exception ex){throw new IllegalStateException("Could not hash recovery token.",ex);}
    }
    private static boolean blank(String s){return s==null || s.isBlank();}
    private record RecoveryAccount(int userId,long companyId,boolean active,String roleName,String employeeEmail,String employeePhone,String recoveryEmail,String recoveryPhone){}
    private record OtpChallenge(long requestId,int userId,long companyId,String otpHash,int attempts,Instant expiresAt,Instant usedAt){}
    private record ResetGrant(long requestId,int userId,long companyId){}
}
