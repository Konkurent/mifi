package demo.wallet.wallet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryStats {

    private Double credit;
    private Double debit;
    private Double limit;

}
