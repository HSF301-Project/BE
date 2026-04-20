package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.account.response.AccountResponseDTO;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountResponseDTO> getAllAccounts(String role, String keyword);
    void changeStatus(UUID id);
}
