package sp26.group.busticket.modules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sp26.group.busticket.modules.service.AccountService;
import sp26.group.busticket.modules.service.ReportService;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final AccountService accountService;

    @GetMapping
    public String showReports(@org.springframework.web.bind.annotation.RequestParam(name = "year", required = false) Integer year,
                              @org.springframework.web.bind.annotation.RequestParam(name = "month", required = false) Integer month,
                              @AuthenticationPrincipal UserDetails userDetails, Model model) {
        java.time.LocalDate now = java.time.LocalDate.now();
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();
        model.addAttribute("adminUser", accountService.getAccountByEmail(userDetails.getUsername()));
        model.addAttribute("reports", reportService.getGeneralReport(year, month));
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("title", "Báo cáo thống kê");
        model.addAttribute("activePage", "reports");
        return "Admin/reports";
    }
}
