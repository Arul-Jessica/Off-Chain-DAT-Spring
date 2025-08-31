package com.tokenization.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SeedCashRequest {
    private Long partyId;
    private String currency;
    private BigDecimal amount;
}