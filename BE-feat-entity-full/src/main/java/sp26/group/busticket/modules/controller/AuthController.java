package sp26.group.busticket.modules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sp26.group.busticket.modules.dto.auth.request.RegisterRequestDTO;
import sp26.group.busticket.modules.service.AuthService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String login(jakarta.servlet.http.HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return "redirect:/home";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                          @RequestParam String email,
                          @RequestParam String phone,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          Model model) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mat khau xac nhan khong khop!");
            return "auth/register";
        }
        
        try {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setEmail(email);
            request.setPassword(password);
            request.setFullName(fullName);
            request.setPhone(phone);
            authService.register(request);
            model.addAttribute("successMessage", "Dang ky thanh cong! Vui long dang nhap.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        
        return "auth/register";
    }

    @GetMapping("/home")
    public String home(Authentication auth) {
        if (auth != null && auth.getAuthorities() != null) {
            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("ROLE_USER");
            
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_STAFF")) {
                return "redirect:/staff/dashboard";
            }
        }
        return "home";
    }
}
