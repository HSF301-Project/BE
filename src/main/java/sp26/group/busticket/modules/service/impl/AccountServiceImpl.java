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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountResponseDTO> getAllAccounts(String search, String role) {
        List<Account> accounts;
        if (role != null && !role.isBlank()) {
            if (search != null && !search.isBlank()) {
                accounts = accountRepository.findByRoleAndFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(role, search, search);
            } else {
                accounts = accountRepository.findByRole(role);
            }
        } else {
            if (search != null && !search.isBlank()) {
                accounts = accountRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
            } else {
                accounts = accountRepository.findAll();
            }
        }

        return accounts.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void changeStatus(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        account.setStatus(account.getStatus() == StatusEnum.ACTIVE ? StatusEnum.INACTIVE : StatusEnum.ACTIVE);
        accountRepository.save(account);
    }

    @Override
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<Account> getAssistants() {
        return accountRepository.findByRoleAndStatusOrderByFullNameAsc("STAFF", StatusEnum.ACTIVE);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return accountRepository.existsByPhone(phone);
    }

    @Override
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }

    private AccountResponseDTO mapToResponseDTO(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole())
                .status(account.getStatus())
                .build();
    }
}
