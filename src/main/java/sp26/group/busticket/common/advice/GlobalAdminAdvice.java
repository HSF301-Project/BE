package sp26.group.busticket.common.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.repository.AccountRepository;

@ControllerAdvice(basePackages = "sp26.group.busticket.modules.controller")
@RequiredArgsConstructor
public class GlobalAdminAdvice {

    private final AccountRepository accountRepository;

    @ModelAttribute
    public void addAdminUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            accountRepository.findByEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("adminUser", admin);
            });
        }
    }
}
