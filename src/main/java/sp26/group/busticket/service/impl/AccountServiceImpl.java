package sp26.group.busticket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.dto.account.response.AccountResponseDTO;
import sp26.group.busticket.entity.Account;
import sp26.group.busticket.enumType.StatusEnum;
import sp26.group.busticket.repository.AccountRepository;
import sp26.group.busticket.service.AccountService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountResponseDTO> getAllAccounts(String search, String role) {
        return accountRepository.findAll().stream()
                .filter(account -> {
                    String r = account.getRole();
                    // 1. Role Filter
                    if ("USER".equals(role)) {
                        if (!"USER".equalsIgnoreCase(r)) return false;
                    } else if (role == null || role.trim().isEmpty() || "ALL".equals(role)) {
                        // Default staff list: exclude USER and ADMIN
                        if ("USER".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r)) return false;
                    } else {
                        // Specific staff role selected (STAFF, DRIVER, etc.)
                        if (!role.equalsIgnoreCase(r)) return false;
                    }

                    // 2. Search Filter
                    if (search == null || search.trim().isEmpty()) return true;
                    String s = search.trim().toLowerCase();
                    
                    String name = (account.getFullName() != null) ? account.getFullName().toLowerCase() : "";
                    String email = (account.getEmail() != null) ? account.getEmail().toLowerCase() : "";
                    String phone = (account.getPhone() != null) ? account.getPhone() : "";
                    
                    return name.contains(s) || email.contains(s) || phone.contains(s);
                })
                .map(account -> AccountResponseDTO.builder()
                        .id(account.getId())
                        .email(account.getEmail())
                        .fullName(account.getFullName())
                        .phone(account.getPhone())
                        .role(account.getRole())
                        .status(account.getStatus())
                        .createdAt(account.getCreatedAt())
                        .updatedAt(account.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String changeStatus(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        if (account.getStatus().equals(StatusEnum.ACTIVE)) {
            account.setStatus(StatusEnum.BLOCKED);
        } else {
            account.setStatus(StatusEnum.ACTIVE);
        }
        
        accountRepository.save(account);
        return account.getRole();
    }

    @Override
    public AccountResponseDTO getAccountById(UUID id) {
        return accountRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public AccountResponseDTO getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    public List<AccountResponseDTO> getAccountsByRoleAndStatus(String role, StatusEnum status) {
        return accountRepository.findByRoleAndStatusOrderByFullNameAsc(role, status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AccountResponseDTO mapToDTO(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .role(account.getRole())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
