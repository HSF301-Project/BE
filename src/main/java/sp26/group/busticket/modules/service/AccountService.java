package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.account.response.AccountResponseDTO;
import sp26.group.busticket.modules.entity.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    List<AccountResponseDTO> getAllAccounts(String search, String role);
    void changeStatus(UUID id);
    Account getAccountByEmail(String email);
    List<Account> getAssistants();
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Account saveAccount(Account account);
    Optional<Account> findById(UUID id);
}
