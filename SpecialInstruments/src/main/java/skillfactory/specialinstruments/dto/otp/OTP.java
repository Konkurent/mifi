package skillfactory.specialinstruments.dto.otp;

public record OTP(
        String operation,
        Integer code,
        Long expirationLimit
) {
}
