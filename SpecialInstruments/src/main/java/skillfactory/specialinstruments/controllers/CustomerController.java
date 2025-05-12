package skillfactory.specialinstruments.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import skillfactory.specialinstruments.dto.otp.CreateOTPRequest;
import skillfactory.specialinstruments.dto.otp.VerifyOTPRequest;
import skillfactory.specialinstruments.dto.security.OtpUserDetails;
import skillfactory.specialinstruments.services.otp.OTPService;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class CustomerController {

    private final OTPService otpService;

    @PostMapping
    public void create(@RequestBody CreateOTPRequest request, Authentication authentication) {
        otpService.createOTP(request, (OtpUserDetails) authentication.getPrincipal());
    }

    @PutMapping
    public boolean verify(@RequestBody VerifyOTPRequest request, Authentication authentication) {
        return otpService.verify(request, (OtpUserDetails) authentication.getPrincipal());
    }

}
