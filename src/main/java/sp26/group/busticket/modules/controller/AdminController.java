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
import sp26.group.busticket.modules.service.CoachService;
import sp26.group.busticket.modules.service.TripService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountRepository accountRepository;
    private final CoachService coachService;
    // Assume we might need other services for dashboard stats

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
        
        // Basic stats
        model.addAttribute("totalCoaches", coachService.getAllCoaches().size());
        // Add more stats as needed
        
        return "Admin/dashboard";
    }
    @GetMapping("/trips")
    public String trip() {
        return "Admin/trip";
    }
}
