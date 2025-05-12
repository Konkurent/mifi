package skillfactory.specialinstruments.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.dao.repositories.account.AccountRepository;
import skillfactory.specialinstruments.dto.security.OtpUserDetails;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsProvider implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByLoginIgnoreCase(username.trim())
                .map(account -> OtpUserDetails.builder()
                        .accountId(account.getId())
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())))
                        .login(account.getLogin())
                        .password(account.getPassword())
                        .build()
                ).orElseThrow(() -> new UsernameNotFoundException("Error! The user with signIn " + username + " not found"));
    }
}
