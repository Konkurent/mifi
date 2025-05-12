package skillfactory.specialinstruments.dto.admin;

public record CreateOTPRequest(
      String operation,
      Long accountId
) {
}
