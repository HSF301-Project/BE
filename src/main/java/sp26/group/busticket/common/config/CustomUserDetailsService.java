package sp26.group.busticket.common.config;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sp26.group.busticket.entity.Account;
import sp26.group.busticket.enumType.StatusEnum;
import sp26.group.busticket.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Account account = accountRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với số điện thoại: " + phone));

        if (account.getStatus() == StatusEnum.BLOCKED) {
            throw new org.springframework.security.authentication.LockedException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        
        if (account.getStatus() != StatusEnum.ACTIVE) {
            throw new org.springframework.security.authentication.DisabledException("Tài khoản chưa được kích hoạt.");
        }

        return new User(
                account.getEmail(),
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole()))
        );
    }
}
