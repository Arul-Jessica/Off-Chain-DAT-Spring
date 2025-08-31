package com.tokenization.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private Long fromPartyId; // The ID of the party sending tokens
    private Long toPartyId;   // The ID of the party receiving tokens
    private Long assetId;
    private BigDecimal quantity;
}