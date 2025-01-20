package demo.wallet.wallet.controller;

import demo.wallet.wallet.constants.OperationType;
import demo.wallet.wallet.dto.CategoryStats;
import demo.wallet.wallet.dto.GeneralStats;
import demo.wallet.wallet.model.Operation;
import demo.wallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/operations")
    public void createOperation(@RequestBody CreateOperationPayload payload) {
        walletService.createOperation(payload.token(), payload.type(), payload.category(), payload.amount());
    }

    @PostMapping("/wallet")
    public String createWallet(@RequestParam String session) {
        return walletService.createWallet(session);
    }

    @GetMapping("/operations")
    public List<Operation> getOperations(@RequestParam String token) {
        return walletService.getOperations(token);
    }

    @PostMapping("/limits")
    public void createLimit(@RequestBody CreateLimitPayload payload) {
        walletService.createLimit(payload.token(), payload.category(), payload.amount());
    }

    @GetMapping("/generalStatistics")
    public GeneralStats getGeneralStatistics(@RequestParam String token) {
        return walletService.gatherStatistics(token);
    }

    @GetMapping("/gatherCategoryStats")
    public CategoryStats getGatherCategoryStats(@RequestParam String token, @RequestParam String category) {
        return walletService.gatherCategoryStats(token, category);
    }

    public record CreateLimitPayload(
            String token,
            String category,
            Double amount
    ){}

    public record CreateOperationPayload(
            String token,
            OperationType type,
            String category,
            Double amount
    ) {}

}
