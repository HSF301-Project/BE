package sp26.group.busticket.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sp26.group.busticket.modules.entity.AccountEntity;
import sp26.group.busticket.modules.repository.AccountRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AccountEntity account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String role = account.getRole() != null ? account.getRole() : "USER";
        
        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
                .build();
    }
}
