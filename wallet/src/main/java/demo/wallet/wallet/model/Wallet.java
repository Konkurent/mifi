package demo.wallet.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    private String id = UUID.randomUUID().toString();
    private Double balance = 0.0;
    private List<Operation> operations = new ArrayList<>();
    private List<Limit> limits = new ArrayList<>();

    public Limit findLimit(String category) {
        return limits.stream()
                .filter(limit -> limit.getCategory().equalsIgnoreCase(category))
                .findFirst()
                .orElse(null);
    }

}
