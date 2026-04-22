package sp26.group.busticket.modules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.FinanceService;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminFinanceController {

    private final FinanceService financeService;
    private final AccountRepository accountRepository;

    @GetMapping
    public String listPayments(@RequestParam(required = false) String q,
                               @RequestParam(defaultValue = "1") int page,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
        
        int pageSize = 10;
        var transactionPage = financeService.getTransactions(q, page - 1, pageSize);
        
        model.addAttribute("finance", financeService.getFinanceDashboardData());
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("searchQuery", q != null ? q : "");
        
        int totalPages = transactionPage.getTotalPages();
        Map<String, Object> pagination = new java.util.HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPages", totalPages);
        pagination.put("totalCount", transactionPage.getTotalElements());
        pagination.put("startItem", (page - 1) * pageSize + 1);
        pagination.put("endItem", Math.min(page * pageSize, transactionPage.getTotalElements()));
        pagination.put("hasNext", transactionPage.hasNext());
        pagination.put("pageNumbers", IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList()));
        model.addAttribute("pagination", pagination);
        model.addAttribute("activePage", "payments");
        
        return "Admin/finance_logs";
    }
}
