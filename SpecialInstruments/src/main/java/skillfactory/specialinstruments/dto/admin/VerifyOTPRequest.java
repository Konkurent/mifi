package skillfactory.specialinstruments.dto.admin;

public record VerifyOTPRequest(
        Integer code,
        String operation,
        Long accountId
) {}
