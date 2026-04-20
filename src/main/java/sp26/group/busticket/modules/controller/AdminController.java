package sp26.group.busticket.modules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.AccountService;
import sp26.group.busticket.modules.service.CoachService;
import sp26.group.busticket.modules.service.TripService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountRepository accountRepository;
    private final CoachService coachService;
    private final AccountService accountService;

    @GetMapping("/users")
    public String listUsers(@org.springframework.web.bind.annotation.RequestParam(required = false) String role,
                            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
                            Model model) {
        model.addAttribute("users", accountService.getAllAccounts(role, keyword));
        model.addAttribute("currentRole", role);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("title", "Quản lý Người dùng");
        return "Admin/user-list";
    }

    @PostMapping("/users/status/{id}")
    public String changeUserStatus(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        accountService.changeStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái người dùng thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
        
        // Basic stats
        model.addAttribute("totalCoaches", coachService.getAllCoaches().size());
        // Add more stats as needed
        
        return "Admin/dashboard";
    }

    @GetMapping("/trips/detail/{id}")
    public String getTripDetail(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id, Model model) {
        model.addAttribute("trip", coachService.getAdminTripDetail(id));
        model.addAttribute("title", "Sơ đồ hành khách");
        return "Admin/trip-details";
    }
}
