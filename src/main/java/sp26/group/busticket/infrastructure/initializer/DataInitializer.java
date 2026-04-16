package sp26.group.busticket.infrastructure.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.repository.AccountRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!accountRepository.existsByEmail("admin@gmail.com")) {
            Account admin = Account.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .phone("0123456789")
                    .status(StatusEnum.ACTIVE)
                    .role("ADMIN")
                    .build();
            accountRepository.save(admin);
        }
    }
}
