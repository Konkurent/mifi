package demo.wallet.wallet.model;

import demo.wallet.wallet.constants.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation {

    private Double amount;
    private OperationType type;
    private String creditingCategory;

}
