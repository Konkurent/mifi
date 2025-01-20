package demo.wallet.wallet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * Stats by categories
 */
@Data
@NoArgsConstructor
public class GeneralStats {

    private Double totalDebit;
    private Double totalCredit;
    private HashMap<String, Double> creditingCategories;

}


