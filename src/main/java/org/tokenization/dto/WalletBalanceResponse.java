package com.tokenization.dto;

import lombok.Data;
import java.util.List;

@Data
public class WalletBalanceResponse {
    private List<CashBalance> cashBalances;
    private List<TokenBalance> tokenBalances;

    @Data
    public static class CashBalance {
        private Long walletId;
        private String currency;
        private java.math.BigDecimal amount;
    }

    @Data
    public static class TokenBalance {
        private Long walletId;
        private Long assetId;
        private String assetSymbol;
        private java.math.BigDecimal quantity;
    }
}