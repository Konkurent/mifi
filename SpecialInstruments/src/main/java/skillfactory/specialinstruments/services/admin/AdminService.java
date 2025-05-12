package skillfactory.specialinstruments.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dao.entity.OTPConfiguration;
import skillfactory.specialinstruments.dao.repositories.OTPRepository;
import skillfactory.specialinstruments.dao.repositories.account.AccountRepository;
import skillfactory.specialinstruments.dto.accounts.AccountFilter;
import skillfactory.specialinstruments.dto.admin.UpdateOtpConfigurationRequest;
import skillfactory.specialinstruments.exception.AccountNotFoundException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OTPConfiguration otpConfiguration;
    private final AccountRepository accountRepository;
    private final OTPRepository otpRepository;

    public void updateConfiguration(UpdateOtpConfigurationRequest request) {
        Optional.ofNullable(request.duration()).ifPresent(otpConfiguration::setDuration);
        Optional.ofNullable(request.length()).ifPresent(otpConfiguration::setLength);
    }

    public Page<AccountEntity> getPageOfUsers(int page, int size) {
        return accountRepository.getPageByFilter(
                AccountFilter.builder()
                        .page(page)
                        .size(size)
                        .build()
        );
    }


    public void deleteUser(Long id) {
        AccountEntity account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("Error! Account not fount!"));
        otpRepository.deleteAllInBatch(otpRepository.findAllByAccount(account));
    }
}
