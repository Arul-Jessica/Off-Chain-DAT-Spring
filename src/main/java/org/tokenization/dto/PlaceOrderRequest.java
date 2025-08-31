package com.tokenization.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {
    private Long partyId; // The ID of the party placing the order
    private Long assetId;
    private String orderType; // "BUY" or "SELL"
    private BigDecimal quantity;
    private BigDecimal price;
}