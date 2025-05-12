package skillfactory.specialinstruments.services.otp;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.constants.OTPStatus;
import skillfactory.specialinstruments.dao.entity.OTPConfiguration;
import skillfactory.specialinstruments.dao.entity.OTPEntity;
import skillfactory.specialinstruments.dao.repositories.OTPRepository;
import skillfactory.specialinstruments.dto.otp.CreateOTPRequest;
import skillfactory.specialinstruments.dto.otp.VerifyOTPRequest;
import skillfactory.specialinstruments.dto.security.OtpUserDetails;
import skillfactory.specialinstruments.services.auth.AccountService;
import skillfactory.specialinstruments.services.notification.OTPNotificator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OTPService {

    private final AccountService accountService;
    private final OTPConfiguration otpConfiguration;
    private final OTPRepository otpRepository;
    private final OTPNotificator otpNotificator;
    private final OTPGenerator otpGenerator;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        expire();
    }

    public void createOTP(CreateOTPRequest request, OtpUserDetails userDetails) {
        createOTP(request.operation(), userDetails.accountId());
    }


    public void createOTP(@NonNull String operation, Long accountId) {
        Integer code = otpGenerator.generateOtp();
        OTPEntity otp = otpRepository.save(
                OTPEntity.builder()
                        .account(accountService.findById(accountId))
                        .expirationDateTime(LocalDateTime.now().plusSeconds(otpConfiguration.getDuration()))
                        .code(code)
                        .operation(operation)
                        .build()
        );
        otpNotificator.send(otp);
        log.info("OTP code {} has been successfully created", otp.getCode());
    }

    public boolean verify(VerifyOTPRequest request, OtpUserDetails userDetails) {
        return verify(request.code(), request.operation(), userDetails.accountId());
    }

    public boolean verify(Integer code, String operation, Long accountId) {
        OTPEntity otp = otpRepository.findByAccountIdAndCode(accountId, code)
                .orElseThrow(() -> new IllegalArgumentException("Error! Invalid verify request."));
        try {
            if (otp.isExpired()) {
                otp.setStatus(OTPStatus.EXPIRED);
                return false;
            };
            if (otp.getOperation().equalsIgnoreCase(operation.trim())) {
                otp.setStatus(OTPStatus.USED);
                return true;
            } else {
                return false;
            }
        } finally {
            otpRepository.save(otp);
        }
    }

    public void expire() {
        log.debug("====================== [Start expire otp codes] ======================");
        List<OTPEntity> otpList = otpRepository.findAllByStatusAndExpirationDateTimeBefore(OTPStatus.ACTIVE, LocalDateTime.now());
        otpList.forEach(otp -> otp.setStatus(OTPStatus.EXPIRED));
        otpRepository.saveAll(otpList);
        log.debug("====================== [{} otp codes for expire were found] ======================", otpList.size());
        renew(otpList);
        log.debug("====================== [End expire otp codes] ======================");
        scheduledExecutorService.schedule(this::expire, 10, TimeUnit.SECONDS);
    }

    public void renew(List<OTPEntity> otpList) {
        if (otpList.isEmpty()) return;
        log.debug("     ====================== [Start renew otp codes] ======================");
        List<OTPEntity> reNews = otpList.stream().map(it -> OTPEntity.builder()
                .code(otpGenerator.generateOtp())
                .account(it.getAccount())
                .operation(it.getOperation())
                .expirationDateTime(LocalDateTime.now().plusSeconds(otpConfiguration.getDuration()))
                .build()
        ).collect(Collectors.toList());
        reNews.forEach(otpNotificator::send);
        otpRepository.saveAll(reNews);
        log.debug("     ====================== [{} otp codes for renew were found] ======================", otpList.size());
        log.debug("     ====================== [End renew otp codes] ======================");
    }
}
