package skillfactory.specialinstruments.services.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import skillfactory.specialinstruments.dao.entity.OTPConfiguration;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class OTPGenerator {

    private final OTPConfiguration otpConfiguration;
    private final SecureRandom random = new SecureRandom();

    public Integer generateOtp() {
        StringBuilder sb = new StringBuilder(otpConfiguration.getLength());
        for (int i = 0; i < otpConfiguration.getLength(); i++) {
            sb.append(random.nextInt(10));
        }
        return Integer.parseInt(sb.toString());
    }

}
