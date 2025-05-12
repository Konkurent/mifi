package skillfactory.specialinstruments.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import skillfactory.specialinstruments.dao.entity.OTPConfiguration;
import skillfactory.specialinstruments.dto.admin.CreateOTPRequest;
import skillfactory.specialinstruments.dto.admin.UpdateOtpConfigurationRequest;
import skillfactory.specialinstruments.dto.admin.VerifyOTPRequest;
import skillfactory.specialinstruments.services.admin.AdminService;
import skillfactory.specialinstruments.services.otp.OTPService;
import skillfactory.specialinstruments.util.converters.UserDTOConverter;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final OTPService otpService;
    private final OTPConfiguration otpConfiguration;

    @PutMapping("/otp_conf")
    public void updateOtpConfiguration(@RequestBody UpdateOtpConfigurationRequest request) {
        adminService.updateConfiguration(request);
    }

    @GetMapping("/otp_conf")
    public OTPConfiguration getConf() {
        return otpConfiguration;
    }

    @PostMapping("/otp")
    public void createOTP(@RequestBody CreateOTPRequest request) {
        otpService.createOTP(request.operation(), request.accountId());
    }

    @PutMapping("/otp")
    public boolean verify(@RequestBody VerifyOTPRequest request) {
        return otpService.verify(request.code(), request.operation(), request.accountId());
    }

    @GetMapping("/users")
    public Page<Object> getPageOfUsers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return adminService.getPageOfUsers(page, size).map(UserDTOConverter::toDTO);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

}
