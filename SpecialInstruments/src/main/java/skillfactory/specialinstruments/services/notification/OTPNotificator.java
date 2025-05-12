package skillfactory.specialinstruments.services.notification;

import skillfactory.specialinstruments.dao.entity.OTPEntity;

public interface OTPNotificator {

    void send(OTPEntity otp);

}
