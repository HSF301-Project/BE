package sp26.group.busticket.modules.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.auth.request.RegisterRequestDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.mapper.AuthMapper;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Account account = authMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Mặc định là ACTIVE vì người dùng yêu cầu bỏ OTP
        account.setStatus(StatusEnum.ACTIVE);
        account.setRole("USER");

        accountRepository.save(account);
    }
}
