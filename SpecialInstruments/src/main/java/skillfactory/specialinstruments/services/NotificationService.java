package skillfactory.specialinstruments.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.dao.entity.OTPEntity;
import skillfactory.specialinstruments.services.notification.EmailService;
import skillfactory.specialinstruments.services.notification.OTPNotificator;
import skillfactory.specialinstruments.services.notification.SMSEmulator;
import skillfactory.specialinstruments.services.notification.TelegramBot;

@Service
@Primary
@RequiredArgsConstructor
public class NotificationService implements OTPNotificator {

    private final SMSEmulator smsEmulator;
    private final EmailService emailService;
    private final TelegramBot telegramBot;

    @Override
    public void send(OTPEntity otp) {
        smsEmulator.send(otp);
        emailService.send(otp);
        telegramBot.send(otp);
    }
}
