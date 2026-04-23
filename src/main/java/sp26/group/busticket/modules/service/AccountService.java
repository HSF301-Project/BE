package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.account.response.AccountResponseDTO;
import sp26.group.busticket.modules.enumType.StatusEnum;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountResponseDTO> getAllAccounts(String search, String role);
    String changeStatus(UUID id);
    AccountResponseDTO getAccountById(UUID id);
    AccountResponseDTO getAccountByEmail(String email);
    List<AccountResponseDTO> getAccountsByRoleAndStatus(String role, StatusEnum status);
}
