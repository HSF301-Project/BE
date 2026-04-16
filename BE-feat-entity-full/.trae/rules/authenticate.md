package hsf302.rent_house.modules.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}"

"package hsf302.rent_house.modules.service;

import hsf302.rent_house.modules.dto.auth.request.*;

public interface AuthService {
    void register(RegisterRequestDTO request);
    void activateAccount(VerifyOtpRequestDTO request);
    void forgotPassword(ForgotPasswordRequestDTO request);
    void verifyOtp(VerifyOtpRequestDTO request);
    void resetPassword(ResetPasswordRequestDTO request);
    // Hàm changePassword dành cho user đã đăng nhập đổi pass trong Profile, tạm thời chưa cần ở file AuthController này
}"

"package hsf302.rent_house.modules.service.impl;

import hsf302.rent_house.common.exception.ApiException;
import hsf302.rent_house.common.exception.ErrorCode;
import hsf302.rent_house.common.security.OtpRedis;
import hsf302.rent_house.modules.dto.auth.request.*;
import hsf302.rent_house.modules.entity.AccountEntity;
import hsf302.rent_house.modules.entity.AccountRoleEntity;
import hsf302.rent_house.modules.entity.RoleEntity;
import hsf302.rent_house.modules.enumType.StatusEnum;
import hsf302.rent_house.modules.mapper.AuthMapper;
import hsf302.rent_house.modules.repository.AccountRepository;
import hsf302.rent_house.modules.repository.AccountRoleRepository;
import hsf302.rent_house.modules.repository.RoleRepository;
import hsf302.rent_house.modules.service.AuthService;
import hsf302.rent_house.modules.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final AuthMapper authMapper;
    private final OtpRedis otpRedis;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Mật khẩu xác nhận không khớp");
        }
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Email đã tồn tại");
        }

        AccountEntity account = authMapper.toAccountEntity(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(StatusEnum.PENDING); // Chờ kích hoạt
        account = accountRepository.save(account);

        RoleEntity userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT, "Không tìm thấy Role mặc định"));
        accountRoleRepository.save(AccountRoleEntity.builder().account(account).role(userRole).build());

        sendOtpLogic(request.getEmail());
    }

    @Override
    @Transactional
    public void activateAccount(VerifyOtpRequestDTO request) {
        verifyOtp(request); // Tái sử dụng hàm check OTP
        AccountEntity account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản"));
        account.setStatus(StatusEnum.ACTIVE);
        accountRepository.save(account);
        otpRedis.deleteOtp(request.getEmail());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        AccountEntity account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Email không tồn tại"));
        if (account.getStatus() != StatusEnum.ACTIVE) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Tài khoản đang bị khóa hoặc chưa kích hoạt");
        }
        sendOtpLogic(request.getEmail());
    }

    @Override
    public void verifyOtp(VerifyOtpRequestDTO request) {
        String storedOtp = otpRedis.getOtp(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT, "Mã OTP đã hết hạn hoặc không tồn tại"));
        if (!storedOtp.equals(request.getOtp())) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Mã OTP không chính xác");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Mật khẩu xác nhận không khớp");
        }
        verifyOtp(new VerifyOtpRequestDTO() {{ setEmail(request.getEmail()); setOtp(request.getOtp()); }});
        
        AccountEntity account = accountRepository.findByEmail(request.getEmail()).orElseThrow();
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        otpRedis.deleteOtp(request.getEmail());
    }

    private void sendOtpLogic(String email) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        otpRedis.storeOtp(email, otp, Duration.ofMinutes(5));
        mailService.sendOtpEmail(email, otp);
    }
}"

"package hsf302.rent_house.modules.controller;

import hsf302.rent_house.common.exception.ApiException;
import hsf302.rent_house.modules.dto.auth.request.*;
import hsf302.rent_house.modules.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // --- 1. ĐĂNG NHẬP (Spring Security xử lý POST) ---
    @GetMapping("/login")
    public String login() { return "auth/login"; }

    // --- 2. ĐĂNG KÝ & KÍCH HOẠT ---
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerDTO", new RegisterRequestDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDTO") RegisterRequestDTO dto,
                                  BindingResult bindingResult, Model model, HttpSession session) {
        if (bindingResult.hasErrors()) return "auth/register";
        try {
            authService.register(dto);
            session.setAttribute("tempEmail", dto.getEmail()); // Lưu tạm email vào session
            return "redirect:/auth/activate";
        } catch (ApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/activate")
    public String showActivate(HttpSession session, Model model) {
        if (session.getAttribute("tempEmail") == null) return "redirect:/auth/register";
        model.addAttribute("verifyOtpDTO", new VerifyOtpRequestDTO());
        return "auth/activate";
    }

    @PostMapping("/activate")
    public String processActivate(@Valid @ModelAttribute("verifyOtpDTO") VerifyOtpRequestDTO dto,
                                  BindingResult bindingResult, HttpSession session, 
                                  Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "auth/activate";
        try {
            dto.setEmail((String) session.getAttribute("tempEmail"));
            authService.activateAccount(dto);
            session.removeAttribute("tempEmail");
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt thành công, vui lòng đăng nhập!");
            return "redirect:/auth/login";
        } catch (ApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/activate";
        }
    }

    // --- 3. QUÊN MẬT KHẨU ---
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("forgotDTO", new ForgotPasswordRequestDTO());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotDTO") ForgotPasswordRequestDTO dto,
                                        BindingResult bindingResult, HttpSession session, Model model) {
        if (bindingResult.hasErrors()) return "auth/forgot-password";
        try {
            authService.forgotPassword(dto);
            session.setAttribute("resetEmail", dto.getEmail());
            return "redirect:/auth/reset-password";
        } catch (ApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPassword(HttpSession session, Model model) {
        if (session.getAttribute("resetEmail") == null) return "redirect:/auth/forgot-password";
        model.addAttribute("resetDTO", new ResetPasswordRequestDTO());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetDTO") ResetPasswordRequestDTO dto,
                                       BindingResult bindingResult, HttpSession session, 
                                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "auth/reset-password";
        try {
            dto.setEmail((String) session.getAttribute("resetEmail"));
            authService.resetPassword(dto);
            session.removeAttribute("resetEmail");
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/auth/login";
        } catch (ApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }
}"

"package hsf302.rent_house.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpRedis {
    private static final String OTP_PREFIX = "otp:";
    private final StringRedisTemplate redis;

    public void storeOtp(String email, String otp, Duration ttl) {
        redis.opsForValue().set(OTP_PREFIX + email, otp, ttl);
    }

    public Optional<String> getOtp(String email) {
        return Optional.ofNullable(redis.opsForValue().get(OTP_PREFIX + email));
    }

    public void deleteOtp(String email) {
        redis.delete(OTP_PREFIX + email);
    }
}"

"package hsf302.rent_house.modules.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("otp", otp);
            
            // Đã cập nhật đường dẫn: Tìm file otp-email.html trong thư mục templates/mail/
            String html = templateEngine.process("mail/otp-email", context);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Mã xác thực quên mật khẩu - Rent House App");
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Sent OTP email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }
}"

"<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Mã OTP của bạn - Rent House App</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f7f6;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 600px;
            margin: 20px auto;
            background-color: #ffffff;
            border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        .header {
            background-color: #2c3e50;
            padding: 20px;
            text-align: center;
            color: #ffffff;
        }
        .header h1 {
            margin: 0;
            font-size: 24px;
        }
        .content {
            padding: 30px;
            line-height: 1.6;
            color: #333333;
        }
        .otp-box {
            background-color: #ecf0f1;
            padding: 20px;
            text-align: center;
            font-size: 32px;
            font-weight: bold;
            letter-spacing: 5px;
            color: #e67e22;
            border-radius: 4px;
            margin: 20px 0;
        }
        .footer {
            background-color: #f9f9f9;
            padding: 15px;
            text-align: center;
            font-size: 12px;
            color: #777777;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Rent House App</h1>
        </div>
        <div class="content">
            <p>Xin chào,</p>
            <p>Chúng tôi nhận được yêu cầu xác thực tài khoản từ bạn. Vui lòng sử dụng mã OTP dưới đây để hoàn tất quá trình:</p>
            <div class="otp-box" th:text="${otp}">123456</div>
            <p>Mã này sẽ hết hạn sau <strong>5 phút</strong>. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
            <p>Trân trọng,<br>Đội ngũ Rent House</p>
        </div>
        <div class="footer">
            &copy; 2024 Rent House Application. All rights reserved.
        </div>
    </div>
</body>
</html>"

"# --- CẤU HÌNH REDIS ---
spring.data.redis.host=localhost
spring.data.redis.port=6379

# --- CẤU HÌNH GỬI MAIL (Dùng Gmail App Password) ---
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=email_cua_ban@gmail.com
spring.mail.password=app_password_cua_ban
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true"

"<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>"