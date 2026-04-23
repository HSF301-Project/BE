package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.account.request.StaffCreateRequestDTO;
import sp26.group.busticket.modules.dto.account.request.StaffUpdateRequestDTO;
import sp26.group.busticket.modules.dto.account.response.StaffResponseDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.mapper.StaffMapper;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.StaffService;

import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private static final String STAFF_ROLE = "STAFF";
    private static final Set<String> MANAGEABLE_ROLES = Set.of("STAFF", "ASSISTANT", "DRIVER");

    private final AccountRepository accountRepository;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StaffResponseDTO createStaff(StaffCreateRequestDTO request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Account staff = staffMapper.toAccount(request);
        staff.setRole(normalizeCreateRole(request.getRole()));
        staff.setStatus(request.getStatus() == null ? StatusEnum.ACTIVE : request.getStatus());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));

        return staffMapper.toStaffResponseDTO(accountRepository.save(staff));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffResponseDTO> getStaffPage(String keyword, String role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        String normalizedRole = normalizeRole(role);
        return accountRepository.searchStaffByKeyword(normalizedRole, keyword, pageable)
                .map(staffMapper::toStaffResponseDTO);
    }

    @Override
    @Transactional
    public void toggleStatus(UUID staffId) {
        Account staff = accountRepository.findById(staffId)
                .filter(this::isManageableRole)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        staff.setStatus(staff.getStatus() == StatusEnum.ACTIVE ? StatusEnum.BLOCKED : StatusEnum.ACTIVE);
        accountRepository.save(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponseDTO> getAllStaff() {
        return accountRepository.findAll().stream()
                .filter(this::isManageableRole)
                .sorted(Comparator.comparing(Account::getCreatedAt).reversed())
                .map(staffMapper::toStaffResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffResponseDTO> getStaffById(UUID staffId) {
        return accountRepository.findById(staffId)
                .filter(this::isManageableRole)
                .map(staffMapper::toStaffResponseDTO);
    }

    @Override
    @Transactional
    public Optional<StaffResponseDTO> updateStaff(UUID staffId, StaffUpdateRequestDTO request) {
        return accountRepository.findById(staffId)
                .filter(this::isManageableRole)
                .map(existingStaff -> {
                    Optional<Account> accountWithEmail = accountRepository.findByEmail(request.getEmail());
                    if (accountWithEmail.isPresent() && !accountWithEmail.get().getId().equals(staffId)) {
                        throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
                    }

                    staffMapper.updateAccountFromDto(request, existingStaff);
                    if (request.getStatus() != null) {
                        existingStaff.setStatus(request.getStatus());
                    }
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        existingStaff.setPassword(passwordEncoder.encode(request.getPassword()));
                    }

                    return staffMapper.toStaffResponseDTO(accountRepository.save(existingStaff));
                });
    }

    @Override
    @Transactional
    public boolean deleteStaff(UUID staffId) {
        Optional<Account> staff = accountRepository.findById(staffId).filter(this::isManageableRole);
        if (staff.isEmpty()) {
            return false;
        }

        accountRepository.delete(staff.get());
        return true;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ALL";
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return MANAGEABLE_ROLES.contains(normalized) ? normalized : "ALL";
    }

    private boolean isManageableRole(Account account) {
        return account.getRole() != null && MANAGEABLE_ROLES.contains(account.getRole().toUpperCase(Locale.ROOT));
    }

    private String normalizeCreateRole(String role) {
        if (role == null || role.isBlank()) {
            return STAFF_ROLE;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return MANAGEABLE_ROLES.contains(normalized) ? normalized : STAFF_ROLE;
    }
}

