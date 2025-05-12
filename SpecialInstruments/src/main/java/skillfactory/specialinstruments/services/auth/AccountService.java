package skillfactory.specialinstruments.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.constants.Role;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dao.repositories.account.AccountRepository;
import skillfactory.specialinstruments.dto.security.SignUpRequest;
import skillfactory.specialinstruments.exception.AccountExistException;
import skillfactory.specialinstruments.exception.AccountNotFoundException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountEntity findById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("Error! Account not found!"));
    }

    public AccountEntity createUser(SignUpRequest signUpRequest) {
        if (accountRepository.existsByLoginIgnoreCase(signUpRequest.login()))
            throw new AccountExistException();
        return accountRepository.save(
                AccountEntity.builder()
                        .login(signUpRequest.login())
                        .password(passwordEncoder.encode(signUpRequest.password()))
                        .email(signUpRequest.email())
                        .phone(signUpRequest.phone())
                        .role(Role.USER)
                        .build()
        );
    }

    public AccountEntity createAdmin(SignUpRequest signUpRequest) {
        if (accountRepository.existsByRole(Role.ADMIN)
                || accountRepository.existsByLoginIgnoreCase(signUpRequest.login()))
            throw new AccountExistException();
        return accountRepository.save(
                AccountEntity.builder()
                        .login(signUpRequest.login())
                        .password(passwordEncoder.encode(signUpRequest.password()))
                        .email(signUpRequest.email())
                        .phone(signUpRequest.phone())
                        .role(Role.ADMIN)
                        .build()
        );
    }

}
