package skillfactory.specialinstruments.services.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dao.entity.OTPEntity;

import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements OTPNotificator{

    private final JavaMailSender mailSender;

    @Override
    public void send(OTPEntity otp) {
        Optional.ofNullable(otp.getAccount())
                .map(AccountEntity::getEmail)
                .filter(Predicate.not(String::isBlank))
                .ifPresent(mail -> {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(mail);
                    msg.setSubject("Your otp code");
                    msg.setText("Your verification code is: " + otp.getCode());
                    mailSender.send(msg);
                    log.info("[EMAIL_EMULATOR] To: {}; Message: {}", mail, "Your verification code is: " + otp.getCode());
                });

    }
}
