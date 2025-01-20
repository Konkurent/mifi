package demo.wallet.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.wallet.wallet.constants.OperationType;
import demo.wallet.wallet.dto.CategoryStats;
import demo.wallet.wallet.dto.GeneralStats;
import demo.wallet.wallet.model.Limit;
import demo.wallet.wallet.model.Operation;
import demo.wallet.wallet.model.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final AuthService authService;

    private Map<String, Wallet> walletsBySessions = new ConcurrentHashMap<>();

    @SneakyThrows
    private Wallet loadWallet(String sessionToken) {
        var user = authService.getUserBySession(sessionToken);
        if (user != null) {
            var om = new ObjectMapper();
            return om.readValue(new File("wallet_%s.json".formatted(user.getWalletId())), Wallet.class);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Wallet getWallet(String token) {
        if (!walletsBySessions.containsKey(token)) {
            loadWallet(token);
        }
        if (!walletsBySessions.containsKey(token)) {
            throw new IllegalArgumentException();
        }
        return walletsBySessions.computeIfAbsent(token, this::loadWallet);
    }

    public void createOperation(String token, OperationType type, String category, Double amount) {
        val wallet = getWallet(token);
        if(type == OperationType.CREDITING && category != null){
            val limit = wallet.findLimit(category);
            if(limit != null){
                val sum = wallet.getOperations()
                        .stream()
                        .filter(op -> op.getType() == OperationType.CREDITING && op.getCreditingCategory().equalsIgnoreCase(category))
                        .map(Operation::getAmount)
                        .reduce(Double::sum)
                        .orElse(Double.POSITIVE_INFINITY);
                if(sum + amount > limit.getAmount()){
                    throw new RuntimeException("Limit exceeded");
                }
            }
        }
        wallet.getOperations().add(new Operation(amount, type, category));
    }

    public List<Operation> getOperations(String token) {
        return getWallet(token).getOperations();
    }

        public void createLimit(String token, String category, Double amount) {
        val wallet = getWallet(token);
        var limit = wallet.getLimits().stream().filter(l -> l.getCategory().equalsIgnoreCase(category)).findFirst().orElse(null);
        if (limit == null) {
            limit = new Limit(category, amount);
            wallet.getLimits().add(limit);
        } else {
            limit.setAmount(amount);
        }
    }

    public GeneralStats gatherStatistics(String token) {
        val wallet = getWallet(token);
        val stats = new GeneralStats();
        wallet.getOperations().forEach(op -> {
            switch (op.getType()){
                case DEBITING -> stats.setTotalDebit(stats.getTotalDebit() + op.getAmount());
                case CREDITING -> stats.setTotalCredit(stats.getTotalCredit() + op.getAmount());
            }
            if(op.getType() == OperationType.CREDITING){
                stats.getCreditingCategories().put(
                        op.getCreditingCategory(),
                        stats.getCreditingCategories().computeIfAbsent(op.getCreditingCategory(), c -> 0.0) + op.getAmount()
                );
            }
        });
        return stats;
    }

    public CategoryStats gatherCategoryStats(String token, String category){
        val wallet = getWallet(token);
        val stats = new CategoryStats();
        wallet.getOperations().stream().filter(op -> op.getCreditingCategory().equalsIgnoreCase(category)).forEach(op -> {
            switch (op.getType()){
                case DEBITING ->  stats.setDebit(stats.getDebit() + op.getAmount());
                case CREDITING -> stats.setCredit(stats.getCredit() + op.getAmount());
            }
        });
        val limit = wallet.findLimit(category);
        if(limit != null){
            stats.setLimit(limit.getAmount());
        }
        return stats;
    }

    public String createWallet(String session) {
        Wallet wallet = Wallet.builder().build();
        this.walletsBySessions.put(session, getWallet(session));
        return wallet.getId();
    }
}
