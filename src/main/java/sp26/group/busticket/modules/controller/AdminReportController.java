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
import sp26.group.busticket.modules.service.ReportService;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final AccountRepository accountRepository;

    @GetMapping
    public String showReports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
        model.addAttribute("reports", reportService.getGeneralReport());
        model.addAttribute("title", "Báo cáo thống kê");
        model.addAttribute("activePage", "reports");
        return "Admin/reports";
    }
}
