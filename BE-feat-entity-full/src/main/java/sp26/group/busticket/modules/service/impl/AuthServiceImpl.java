package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.auth.request.RegisterRequestDTO;
import sp26.group.busticket.modules.entity.AccountEntity;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;

    @Override
    public void register(RegisterRequestDTO request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        AccountEntity account = AccountEntity.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(StatusEnum.ACTIVE)
                .role("USER")
                .build();

        accountRepository.save(account);
    }
}
