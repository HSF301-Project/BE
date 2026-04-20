package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.account.response.AccountResponseDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.AccountService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountResponseDTO> getAllAccounts(String role, String keyword) {
        return accountRepository.findAll().stream()
                .filter(account -> account.getRole() != null && !account.getRole().equalsIgnoreCase("ADMIN"))
                .filter(account -> {
                    if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("ALL")) {
                        return account.getRole().trim().equalsIgnoreCase(role.trim());
                    }
                    return true;
                })
                .filter(account -> {
                    if (keyword != null && !keyword.isEmpty()) {
                        String k = keyword.toLowerCase();
                        return account.getFullName().toLowerCase().contains(k) ||
                               account.getEmail().toLowerCase().contains(k) ||
                               account.getPhone().contains(k);
                    }
                    return true;
                })
                .map(account -> AccountResponseDTO.builder()
                        .id(account.getId())
                        .email(account.getEmail())
                        .fullName(account.getFullName())
                        .phone(account.getPhone())
                        .role(account.getRole())
                        .status(account.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void changeStatus(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        if (account.getStatus().equals(StatusEnum.ACTIVE)) {
            account.setStatus(StatusEnum.BLOCKED);
        } else {
            account.setStatus(StatusEnum.ACTIVE);
        }
        
        accountRepository.save(account);
    }
}
